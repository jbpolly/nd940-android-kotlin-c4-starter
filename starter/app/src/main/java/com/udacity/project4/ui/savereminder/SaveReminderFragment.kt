package com.udacity.project4.ui.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.core.base.BaseFragment
import com.udacity.project4.core.base.NavigationCommand
import com.udacity.project4.core.geofence.GEOFENCE_TAG
import com.udacity.project4.core.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.core.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.ui.locationreminders.reminderslist.ReminderDataItem
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.concurrent.TimeUnit

@SuppressLint("UnspecifiedImmutableFlag")
class SaveReminderFragment : BaseFragment() {

    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(5)

    // A PendingIntent for the Broadcast Receiver that handles geofence transitions.
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        binding.viewModel = _viewModel

        setDisplayHomeAsUpEnabled(true)
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            navigateToSelectLocation()
        }

        binding.saveReminder.setOnClickListener {
            saveReminder()
        }
    }

    private fun saveReminder() {
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value

        val reminder = ReminderDataItem(title, description, location, latitude, longitude)
        if (reminder.latitude != null && reminder.longitude != null) {
            checkDeviceLocationSettings(reminder)

        } else {
            addReminderToDb(reminder)
        }
    }

    private fun addReminderToDb(reminderDataItem: ReminderDataItem) {
        _viewModel.validateAndSaveReminder(reminderDataItem)
    }

    /*
    *  Uses the Location Client to check the current state of location settings, and gives the user
    *  the opportunity to turn on location services within our app.
    */

    private val locationSettingsResult = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()){result->
        Toast.makeText(requireContext(), "Resolved location settings", Toast.LENGTH_SHORT).show()
        saveReminder()
    }

    private fun checkDeviceLocationSettings(
        reminderDataItem: ReminderDataItem,
        resolve: Boolean = true
    ) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    locationSettingsResult.launch(intentSenderRequest)

                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(
                        GEOFENCE_TAG, "Error getting location settings resolution: " + sendEx.message
                    )
                }
            } else {
                Snackbar.make(
                    binding.saveReminderRoot,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings(reminderDataItem)
                }.show()
            }
        }

        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                checkGeoFencePermissions(reminderDataItem)
                //Toast.makeText(requireContext(), "Checked location success", Toast.LENGTH_SHORT).show()
            }
        }
    }



    @SuppressLint("MissingPermission")
    private fun addGeofenceForReminder(reminderDataItem: ReminderDataItem) {

        //Build the geofence using the geofence builder
        val geofence = Geofence.Builder()
            .setRequestId(reminderDataItem.id)
            .setCircularRegion(reminderDataItem.latitude ?: 0.0, reminderDataItem.longitude ?: 0.0, GEOFENCE_RADIUS_IN_METERS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        //Build the geofence request.
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                Toast.makeText(requireContext(), R.string.geofences_added, Toast.LENGTH_SHORT)
                    .show()
                Log.e(GEOFENCE_TAG, geofence.requestId)
                addReminderToDb(reminderDataItem)
            }
            addOnFailureListener {
                // Failed to add geofences.
                Toast.makeText(requireContext(), R.string.geofences_not_added, Toast.LENGTH_SHORT).show()
                if ((it.message != null)) {
                    Log.w(GEOFENCE_TAG, it.message ?: "")
                }
                addReminderToDb(reminderDataItem)
            }
        }

    }

    private fun navigateToSelectLocation() {
        //            Navigate to another fragment to get the user location
        _viewModel.navigationCommand.value =
            NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
    }

    private fun checkGeoFencePermissions(reminderDataItem: ReminderDataItem) {
        val backgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && backgroundLocation
        ) {
            // Have all the necessary permission
            addGeofenceForReminder(reminderDataItem)
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    displayFineLocationPermissionRationale()
                } else {
                    requestFineLocationPermissionLaunch.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            } else {
                if (!backgroundLocation) {
                    //always display rationale to explain the need for background location
                    displayBackgroundLocationPermissionRationale()
                }
            }
        }
    }

    private val requestFineLocationPermissionLaunch =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                saveReminder()
            }
        }

    private val requestBackgroundLocationPermissionLaunch =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                saveReminder()
            }
        }

    private fun displayFineLocationPermissionRationale() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.fine_location_title))
            .setMessage(getString(R.string.widget_background_location_description))
            .setPositiveButton(getString(R.string.text_continue)) { _, _ ->
                requestFineLocationPermissionLaunch.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    @SuppressLint("InlinedApi")
    private fun displayBackgroundLocationPermissionRationale() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.background_location_title))
            .setMessage(getString(R.string.widget_background_location_description))
            .setPositiveButton(getString(R.string.text_continue)) { _, _ ->
                requestBackgroundLocationPermissionLaunch.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            .setNegativeButton(android.R.string.cancel) {dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    companion object {
        const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
        const val GEOFENCE_RADIUS_IN_METERS = 100f
        const val ACTION_GEOFENCE_EVENT = "reminder_geofence_event"


    }
}
