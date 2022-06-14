package com.example.mcard.GroupServerActions

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class GeoFindEntity(
 @SerializedName("countUseFunction") val countUseFunction: Int
 , @SerializedName("dateLastUseFunction") val dateLastUseFunction: String)
