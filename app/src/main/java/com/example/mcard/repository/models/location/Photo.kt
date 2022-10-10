package com.example.mcard.repository.models.location

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class Photo
@JvmOverloads constructor(
  @field:SerializedName("photo_reference")
  var photoReference: String? = null

  , @field:SerializedName("width") var width: String? = null
  , @field:SerializedName("height") var height: String? = null

  , @field:SerializedName("html_attribution")
  var htmlAttribution: String? = null
)