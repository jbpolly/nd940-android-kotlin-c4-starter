package com.udacity.project4.ui.savereminder.selectreminderlocation


import android.annotation.SuppressLint
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.core.base.BaseFragment
import com.udacity.project4.core.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.ui.savereminder.SaveReminderViewModel
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(readyMap: GoogleMap) {
        map = readyMap
        val zoomLevel = 13f
        setMapStyle(map)
        map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                location?.let {
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                it.latitude,
                                it.longitude
                            ), zoomLevel
                        )
                    )
                }
            }
        setMapLongClick(map)
        setPoiClick(map)

    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )

            val marker = map.addMarker(
                MarkerOptions().position(latLng).title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            marker?.title = snippet
            requestPositionConfirmation(latLng, snippet, null)
        }

    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
            requestPositionConfirmation(poi.latLng, poi.name, poi)
        }
    }

    private fun requestPositionConfirmation(latLong: LatLng, title: String, poi: PointOfInterest?){
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.reminder_location))
            .setMessage(getString(R.string.are_you_sure_location_reminder, title))
            .setPositiveButton(getString(R.string.text_continue)) { _, _ ->
                onLocationSelected(latLong, title, poi)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                // dismiss dialog
            }
            .show()
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            //Customize the styling of the base map using a JSON object defined in a raw resource file
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            )
            if (!success) {
                Toast.makeText(
                    requireContext(),
                    "Could not load custom map style",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Resources.NotFoundException) {
            Toast.makeText(requireContext(), "Could not find custom map style", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun onLocationSelected(latLong: LatLng, title: String, poi: PointOfInterest?) {
        _viewModel.setLocationInfo(latLong, title, poi)
        findNavController().popBackStack()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


}
