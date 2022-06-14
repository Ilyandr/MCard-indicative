package com.example.mcard.AdapersGroup

import androidx.annotation.Keep
import com.example.mcard.GroupServerActions.GlobalDataFBManager.LINK_FIREBASE_STORAGE
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
internal data class GlobalUserInfoEntity(
 @SerializedName("userGlobalID")val userGlobalID: String
 , @SerializedName("networkActions")var networkActions: String
 , @SerializedName("userGlobalUID")private val userGlobalUID: String): Serializable
{
    fun getUserGlobalUID() = if (
        this.userGlobalUID.contains(LINK_FIREBASE_STORAGE))
            this.userGlobalUID.replace(LINK_FIREBASE_STORAGE, "")
    else this.userGlobalUID
}
