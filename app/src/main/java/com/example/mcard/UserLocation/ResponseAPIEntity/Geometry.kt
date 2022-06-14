package com.example.mcard.UserLocation.ResponseAPIEntity

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class Geometry
@JvmOverloads constructor(
  @field:SerializedName("location") var location: Location? = null
  , @field:SerializedName("viewport") var viewport: Viewport? = null)
