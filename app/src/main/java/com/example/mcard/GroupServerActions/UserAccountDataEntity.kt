package com.example.mcard.GroupServerActions

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class UserAccountDataEntity(
 @SerializedName("login") val login: String
 , @SerializedName("password") val password: String)
