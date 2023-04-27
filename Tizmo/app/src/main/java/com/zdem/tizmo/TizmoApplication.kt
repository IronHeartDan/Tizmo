package com.zdem.tizmo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.zdem.tizmo.utils.isOreoOrAbove

class TizmoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {

        if (isOreoOrAbove()) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channelImportance = NotificationManager.IMPORTANCE_DEFAULT

            // Live Location Channel
            var channelId = getString(R.string.location_channel_id)
            var channelName = getString(R.string.location_channel_name)
            var channelDesc = getString(R.string.location_channel_desc)
            var channel = NotificationChannel(channelId, channelName, channelImportance).apply {
                description = channelDesc
            }
            notificationManager.createNotificationChannel(channel)

            // Near By Channel
            channelId = getString(R.string.near_by_channel_id)
            channelName = getString(R.string.near_by_channel_name)
            channelDesc = getString(R.string.near_by_channel_desc)

            channel = NotificationChannel(channelId, channelName, channelImportance).apply {
                description = channelDesc
            }
            notificationManager.createNotificationChannel(channel)

        }
    }
}