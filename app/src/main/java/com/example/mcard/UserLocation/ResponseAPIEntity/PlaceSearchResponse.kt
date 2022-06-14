package com.example.mcard.UserLocation.ResponseAPIEntity

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class PlaceSearchResponse
@JvmOverloads constructor(
    @field:SerializedName("status")
    var status: String? = null,

    @field:SerializedName("next_page_token")
    var nextPageToken: String? = null,

    @field:SerializedName("results")
    var result: List<Result>? = null
)