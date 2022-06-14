package com.example.mcard.UserLocation.ResponseAPIEntity

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class OpeningHours
@JvmOverloads constructor(
    @field:SerializedName("open_now")
    var openNow: String? = null
)