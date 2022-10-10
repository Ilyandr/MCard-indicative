package com.example.mcard.repository.models.other

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class HistoryEntity(
 @SerializedName("shopName") val shopName: String
 , @SerializedName("shopAddress") val shopAddress: String
 , @SerializedName("timeAdd") val timeAdd: String)