package com.w_via.features.home

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaceSuggestion(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) : Parcelable