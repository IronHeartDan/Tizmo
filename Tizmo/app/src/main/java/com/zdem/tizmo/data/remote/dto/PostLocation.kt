package com.zdem.tizmo.data.remote.dto

import kotlinx.serialization.Serializable


@Serializable
data class PostLocation(
    val user_id: Int,
    val latitude: Double,
    val longitude: Double
)