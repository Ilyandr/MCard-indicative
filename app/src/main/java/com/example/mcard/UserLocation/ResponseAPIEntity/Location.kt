package com.example.mcard.UserLocation.ResponseAPIEntity

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class Location
@JvmOverloads constructor(
 @field:SerializedName("lat") var lat: String? = null
 , @field:SerializedName("lng") var lng: String? = null )