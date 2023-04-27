package com.zdem.tizmo.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

object DefaultLocationClient {

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(client: FusedLocationProviderClient): Task<Location> {
        val currentLocationRequest =
            CurrentLocationRequest.Builder().setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
        return client.getCurrentLocation(currentLocationRequest, null)
    }

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(context: Context, client: FusedLocationProviderClient): Flow<Location> {
        return callbackFlow {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled && !isNetworkEnabled) {
                throw Exception("GPS is disabled")
            }

            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L).build()

            val locationCallBack = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    result.locations.lastOrNull()?.let {
                        launch {
                            send(it)
                        }
                    }
                }
            }

            client.requestLocationUpdates(request, locationCallBack, Looper.getMainLooper())

            awaitClose {
                client.removeLocationUpdates(locationCallBack)
            }
        }
    }
}