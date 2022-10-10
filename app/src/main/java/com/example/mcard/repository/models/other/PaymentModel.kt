package com.example.mcard.repository.models.other

import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class PaymentModel @JvmOverloads constructor(
    @field:SerializedName("payment_token") var payment_token: String? = null,
    @field:SerializedName("amount") var amountObject: Amount? = null,
    @field:SerializedName("capture") var capture: Boolean = false,
    @field:SerializedName("description") var description: String? = null,
)
