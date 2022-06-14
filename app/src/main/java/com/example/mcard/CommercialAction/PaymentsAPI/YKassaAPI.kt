package com.example.mcard.CommercialAction.PaymentsAPI

import android.content.Context
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import com.example.mcard.FunctionalInterfaces.DelegateVoidInterface
import com.example.mcard.GroupServerActions.SubscribeController.Companion.realDate
import com.example.mcard.R
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Credentials
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.yoomoney.sdk.kassa.payments.Checkout.createTokenizeIntent
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import java.math.BigDecimal
import java.util.*

internal object YKassaAPI
{
    internal const val REQUEST_CODE_TOKENIZE = 20222020
    internal const val REQUEST_CODE_3DSECURE = 1787773
    private var savedSinglePaymentData: Pair<Int, String>? = null

    @JvmStatic
    @Suppress("DEPRECATION")
    internal infix fun AppCompatActivity.startTokenize(
        inputSinglePaymentData: Pair<Int, String>)
    {
        savedSinglePaymentData = inputSinglePaymentData
        this.startActivityForResult(
            createTokenizeIntent(
                this, PaymentParameters(
                    amount = Amount(
                        BigDecimal.valueOf(
                            inputSinglePaymentData.first.toDouble())
                        , Currency.getInstance(getString(R.string.CURRENCY_APP_RU))),
                    title = getString(R.string.GENERAL_PAYMENT_TITLE),
                    subtitle = inputSinglePaymentData.second,
                    clientApplicationKey = getString(R.string.GENERAL_SHOP_API_PASSWORD),
                    shopId = this.getString(R.string.GENERAL_SHOP_ID),
                    savePaymentMethod = SavePaymentMethod.OFF,
                    paymentMethodTypes = setOf(
                        PaymentMethodType.BANK_CARD, PaymentMethodType.SBERBANK),
                    authCenterClientId = getString(R.string.GENERAL_CLIENT_API_ID)
                )), REQUEST_CODE_TOKENIZE
        )
    }

    @DelicateCoroutinesApi
    internal object YKassaPaymentAPI : Callback<Any?>
    {
        private lateinit var actionsAfterPayment: PaymentActionEntity
        private var singlePaymentID: String? = null

        private sealed interface CallYKassaAPI
        {
            @POST("payments/")
            fun sendTokenizeData(
                @Header("Authorization") headerAuth: String
                , @Header("Idempotence-Key") uniqueOperationKey: String
                , @Header("Content-Type") contentType: String
                , @Body sendPaymentDataEntity: SendPaymentDataEntity
            ): Call<Any?>

            @GET("payments/{paymentID}")
            fun getPaymentResult(
                @Header("Authorization") headerAuth: String
                , @Header("Content-Type") contentType: String
                , @Path("paymentID") singlePaymentID: String
            ): Call<Any?>
        }

        internal data class PaymentActionEntity(
            val redirectAction: DelegateVoidInterface
            , val actionSuccessPayment: Runnable
            , val actionFailPayment: Runnable)

        @Keep
        private class SendPaymentDataEntity @JvmOverloads constructor(
            @field:SerializedName("payment_token") var payment_token: String? = null
            , @field:SerializedName("amount") var AmountObject: Amount? = null
            , @field:SerializedName("capture") var capture: Boolean = false
            , @field:SerializedName("description") var description: String? = null)

        @Keep
        private data class Amount @JvmOverloads constructor(
            @field:SerializedName("value") var value: String? = null
            , @field:SerializedName("currency") var currency: String? = null)

        internal fun Context.launchConnect(
            inputToken: String
            , actionsAfterPayment: PaymentActionEntity) =
            savedSinglePaymentData?.let {
                YKassaPaymentAPI.actionsAfterPayment = actionsAfterPayment
                Retrofit.Builder()
                    .baseUrl(this.getString(R.string.MAIN_YKASSA_LINK))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(CallYKassaAPI::class.java)
                    .sendTokenizeData(
                        headerAuth = Credentials.basic(
                            this.getString(R.string.GENERAL_SHOP_ID),
                            this.getString(R.string.GENERAL_SHOP_PASSWORD)),
                        uniqueOperationKey = "${realDate().hashCode() + 52}" +
                                "${Random().nextInt(10000810) + 12}",
                        contentType = this.getString(R.string.YKASSA_FORMAT_TYPE),
                        sendPaymentDataEntity = SendPaymentDataEntity(
                            inputToken, Amount(
                                BigDecimal.valueOf(
                                    it.first.toDouble()).toString()
                                , Currency.getInstance(this.getString(R.string.CURRENCY_APP_RU)).toString())
                            , true
                            , it.second)
                    ).enqueue(this@YKassaPaymentAPI)
            }

        internal fun Context.confirmPayment() =
            this@YKassaPaymentAPI.singlePaymentID?.let {
                Retrofit.Builder()
                    .baseUrl(this.getString(R.string.MAIN_YKASSA_LINK))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(CallYKassaAPI::class.java)
                    .getPaymentResult(headerAuth = Credentials.basic(
                        this.getString(R.string.GENERAL_SHOP_ID),
                        this.getString(R.string.GENERAL_SHOP_PASSWORD)),
                        contentType = getString(R.string.YKASSA_FORMAT_TYPE)
                        , singlePaymentID = it)
                    .enqueue(this@YKassaPaymentAPI)
            }

        override fun onResponse(call: Call<Any?>, response: Response<Any?>) =
            if (response.isSuccessful)
                checkedPaymentResult(
                    JSONObject(Gson().toJson(
                        response.body())))
            else
                this.actionsAfterPayment.actionFailPayment.run()

        private fun checkedPaymentResult(allJsonObject: JSONObject?)
        {
            allJsonObject?.let {
                GlobalScope.launch(Dispatchers.Main)
                {
                    if (it.getString("status").equals("succeeded")
                        && !it.getBoolean("test")
                        && it.getBoolean("paid"))
                        actionsAfterPayment.actionSuccessPayment.run()
                    else if (it.getString("status").equals("pending"))
                    {
                        this@YKassaPaymentAPI.singlePaymentID =
                            allJsonObject.getString("id")
                        this@YKassaPaymentAPI.actionsAfterPayment
                            .redirectAction
                            .delegateFunction(
                                allJsonObject.getJSONObject("confirmation")
                                    .getString("confirmation_url"))
                    }
                    else actionsAfterPayment.actionFailPayment.run()
                }.start()
            }
        }

        override fun onFailure(call: Call<Any?>, t: Throwable) =
            this.actionsAfterPayment.actionFailPayment.run()
    }
}