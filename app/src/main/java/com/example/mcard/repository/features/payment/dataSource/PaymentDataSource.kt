package com.example.mcard.repository.features.payment.dataSource


import com.example.mcard.repository.features.payment.source.PaymentSource
import com.example.mcard.repository.features.textAction
import com.example.mcard.repository.features.unitAction
import com.example.mcard.repository.features.utils.CONTENT_TYPE
import com.example.mcard.repository.models.other.PaymentModel
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


internal class PaymentDataSource(
    private val source: PaymentSource,
) {
    inline fun sendTokenizeData(
        headerAuth: String,
        uniqueOperationKey: String,
        sendPaymentDataEntity: PaymentModel,
        crossinline faultAction: unitAction,
        crossinline successAction: textAction,
    ) =
        source.sendTokenizeData(
            CONTENT_TYPE,
            headerAuth,
            uniqueOperationKey,
            sendPaymentDataEntity
        ).enqueue(object : Callback<Any?> {

            override fun onResponse(
                call: Call<Any?>, response: Response<Any?>,
            ) {
                if (response.isSuccessful)
                    response.body()?.run {
                        successAction.invoke(
                            Gson().toJson(
                                this
                            )
                        )
                    }
                else
                    faultAction.invoke()
            }

            override fun onFailure(
                call: Call<Any?>, t: Throwable,
            ) =
                faultAction.invoke()
        })

    inline fun getPaymentResult(
        headerAuth: String,
        singlePaymentID: String,
        crossinline faultAction: unitAction,
        crossinline successAction: textAction,
    ) =
        source.getPaymentResult(
            CONTENT_TYPE, headerAuth, singlePaymentID
        ).enqueue(object : Callback<Any?> {

            override fun onResponse(
                call: Call<Any?>, response: Response<Any?>,
            ) {
                if (response.isSuccessful)
                    response.body()?.run {
                        successAction.invoke(
                            Gson().toJson(
                                this
                            )
                        )
                    }
                else
                    faultAction.invoke()
            }

            override fun onFailure(
                call: Call<Any?>, t: Throwable,
            ) =
                faultAction.invoke()
        })
}