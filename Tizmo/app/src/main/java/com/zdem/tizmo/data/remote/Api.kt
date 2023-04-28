package com.zdem.tizmo.data.remote

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.zdem.tizmo.data.remote.dto.PostLocation
import com.zdem.tizmo.services.ForegroundService
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType

object Api {
    private val httpClient by lazy {
        HttpClient(Android) {
            install(JsonFeature) {
                serializer = KotlinxSerializer()
            }
        }
    }

    private const val SERVER_URL = "http://10.0.2.2:3000"
    private const val POST_LOCATION = "$SERVER_URL/location"


    suspend fun sendLocation(context: Context,latLng: LatLng) {
        try {
            httpClient.post<String>(POST_LOCATION) {
                contentType(ContentType.Application.Json)
                body = PostLocation(1, latLng.latitude, latLng.longitude)
            }
        } catch (e: Exception) {
            Log.d("API", "sendLocation: ${e.message}")
            Intent(
                context, ForegroundService::class.java
            ).apply {
                action = ForegroundService.ACTION_STOP
                context.stopService(this)
            }
        }
    }
}