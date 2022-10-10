package com.example.mcard.repository.models.location

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class Result
@JvmOverloads constructor(
  @field:SerializedName("name") var name: String? = null
  , @field:SerializedName("vicinity") var vicinity: String? = null
  , @field:SerializedName("icon") var icon: String? = null
  , @field:SerializedName("reference") var reference: String? = null
  , @field:SerializedName("geometry") var geometry: Geometry? = null
  , @field:SerializedName("scope") var scope: String? = null

  , @field:SerializedName("place_id")
   var placeID: String? = null
  , @field:SerializedName( "icon_mask_base_uri")
   var iconMaskBaseURI: String? = null
  , @field:SerializedName("plus_code")
   var plusCode: PlusCode? = null
  , @field:SerializedName("icon_background_color")
   var iconBackgroundColor: String? = null
  , @field:SerializedName("business_status")
   var businessStatus: String? = null
  , @field:SerializedName("type")
   var type: List<String>? = null

  , @field:SerializedName("rating")
  var rating: String? = null
  , @field:SerializedName("user_ratings_total")
  var userRatingsTotal: String? = null
  , @field:SerializedName("price_level")
  var priceLevel: String? = null
  , @field:SerializedName("photo")
  var photo: Photo? = null
  , @field:SerializedName("permanently_closed")
  var permanentlyClosed: String? = null
  , @field:SerializedName("opening_hours")
  var openingHours: OpeningHours? = null
)