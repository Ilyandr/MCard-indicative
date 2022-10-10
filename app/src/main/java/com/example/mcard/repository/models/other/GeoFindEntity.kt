package com.example.mcard.repository.models.other

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class GeoFindEntity(
 @SerializedName("countUseFunction") val countUseFunction: Int
 , @SerializedName("dateLastUseFunction") val dateLastUseFunction: String)
