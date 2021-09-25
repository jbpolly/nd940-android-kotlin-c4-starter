package com.udacity.project4.core.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.core.utils.sendNotification
import com.udacity.project4.data.local.LocalDB
import com.udacity.project4.ui.savereminder.SaveReminderFragment.Companion.ACTION_GEOFENCE_EVENT
import kotlinx.coroutines.*

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

const val GEOFENCE_TAG = "GeofenceReceiver"

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.v(GEOFENCE_TAG, "received")
        try {
            GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
        } catch (e : Exception) {
            Log.e(GEOFENCE_TAG, e.message ?: "")
        }
    }
}