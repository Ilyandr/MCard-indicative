package com.example.mcard.repository.models.other

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class DataAccountEntity(
 @SerializedName("login") val login: String = ""
 , @SerializedName("password") val password: String = "")
