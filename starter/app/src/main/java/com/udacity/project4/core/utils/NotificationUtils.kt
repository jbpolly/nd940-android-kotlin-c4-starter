package com.udacity.project4.core.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.ui.locationreminders.RemindersActivity
import com.udacity.project4.ui.locationreminders.reminderslist.ReminderDataItem

private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"
const val REMINDER_KEY = "reminder"

fun sendNotification(context: Context, reminderDataItem: ReminderDataItem) {

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null
    ) {
        val name = context.getString(R.string.app_name)
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    val intent = NavDeepLinkBuilder(context)
        .setComponentName(RemindersActivity::class.java)
        .setGraph(R.navigation.nav_graph)
        .setDestination(R.id.reminderDetailsFragment)
        .setArguments(bundleOf(REMINDER_KEY to reminderDataItem))
        .createPendingIntent()


//    build the notification object with the data to be shown
    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(reminderDataItem.title)
        .setContentText(reminderDataItem.location)
        .setContentIntent(intent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(getUniqueId(), notification)
}

private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())