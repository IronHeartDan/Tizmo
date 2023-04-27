package com.zdem.tizmo.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

object Constants {
    private const val SERVER_URL = "http://localhost:3000"
    const val POST_LOCATION = "$SERVER_URL/location"
    val httpClient = HttpClient(Android) {

    }
}