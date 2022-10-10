package com.example.mcard.repository.models.other

import androidx.annotation.Keep
import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.LINK_FIREBASE_STORAGE
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
data class GlobalUserEntity(
 @SerializedName("userGlobalID")val userGlobalID: String
 , @SerializedName("networkActions")var networkActions: String
 , @SerializedName("userGlobalUID")private val userGlobalUID: String): Serializable
{
    fun getUserGlobalUID() = if (
        this.userGlobalUID.contains(LINK_FIREBASE_STORAGE))
            this.userGlobalUID.replace(LINK_FIREBASE_STORAGE, "")
    else this.userGlobalUID
}
