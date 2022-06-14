package com.example.mcard.AdapersGroup

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class HistoryUseInfoEntity(
 @SerializedName("shopName") val shopName: String
 , @SerializedName("shopAddress") val shopAddress: String
 , @SerializedName("timeAdd") val timeAdd: String)