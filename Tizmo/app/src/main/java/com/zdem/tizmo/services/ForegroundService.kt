package com.zdem.tizmo.services

import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.zdem.tizmo.R
import com.zdem.tizmo.utils.DefaultLocationClient.getLocationUpdates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ForegroundService : LifecycleService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        RUNNING = true
    }

    private fun start() {
        val notification =
            NotificationCompat.Builder(applicationContext, getString(R.string.location_channel_id))
                .apply {
                    setSmallIcon(R.drawable.ic_launcher_foreground)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        setContentTitle("On Air")
                        setContentText("Running...")
                    } else {
                        val remoteView = RemoteViews(
                            packageName, R.layout.custom_notification
                        )

                        remoteView.apply {
                            setTextViewText(R.id.customNotificationTitle, "On Air")
                            setTextViewText(R.id.customNotificationDesc, "Running...")
                        }
                        setCustomContentView(remoteView)
                    }
                    setOngoing(true)
                }

        serviceScope.launch {
            var list = listOf<LatLng>()
            getLocationUpdates(
                applicationContext,
                LocationServices.getFusedLocationProviderClient(applicationContext)
            ).catch { e -> e.printStackTrace() }.collect {
                val latLng = LatLng(it.latitude, it.longitude)
                list = list + latLng

                if (LiveLocation.hasObservers()) {
                    LiveLocation.postValue(list)
                }
            }
        }

        startForeground(1, notification.build())
    }

    private fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        RUNNING = false
        serviceScope.cancel()
    }

    companion object {
        var RUNNING = false
        val LiveLocation = MutableLiveData<List<LatLng>>()
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }

}