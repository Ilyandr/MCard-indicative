package com.example.mcard.repository.models.other

import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class Amount @JvmOverloads constructor(
    @field:SerializedName("value") var value: String? = null,
    @field:SerializedName("currency") var currency: String? = null,
)