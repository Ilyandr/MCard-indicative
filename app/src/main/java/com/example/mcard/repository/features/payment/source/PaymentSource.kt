package com.example.mcard.repository.features.payment.source

import com.example.mcard.repository.models.other.PaymentModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

internal interface PaymentSource {

    @POST(value = "payments")
    fun sendTokenizeData(
        @Header("Content-Type") contentType: String,
        @Header(value = "Authorization") headerAuth: String,
        @Header(value = "Idempotence-Key") uniqueOperationKey: String,
        @Body sendPaymentDataEntity: PaymentModel,
    ): Call<Any?>

    @GET("payments/{id}")
    fun getPaymentResult(
        @Header("Content-Type") contentType: String,
        @Header(value = "Authorization") headerAuth: String,
        @Path(value = "id") singlePaymentID: String,
    ): Call<Any?>
}