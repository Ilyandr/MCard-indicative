package com.example.mcard.UserLocation.ResponseAPIEntity

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class Viewport
@JvmOverloads constructor(
    @field:SerializedName("southwest")
    var southwest: Location? = null
    , @field:SerializedName("northeast")
    var northeast: Location? = null
)