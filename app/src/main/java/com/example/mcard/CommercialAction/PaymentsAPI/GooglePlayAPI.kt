package com.example.mcard.CommercialAction.PaymentsAPI

import android.annotation.SuppressLint
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.Keep
import com.android.billingclient.api.*
import com.example.mcard.CommercialAction.SubscribeManagerActivity
import com.example.mcard.CommercialAction.SubscribeManagerActivity.Companion.finalCountPrice
import com.example.mcard.R
import com.example.mcard.SideFunctionality.CustomAppDialog
import com.google.android.vending.licensing.AESObfuscator
import com.google.android.vending.licensing.LicenseChecker
import com.google.android.vending.licensing.LicenseCheckerCallback
import com.google.android.vending.licensing.ServerManagedPolicy
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import retrofit2.http.Query
import java.lang.Runnable
import kotlin.random.Random


@Suppress("DEPRECATION")
@DelicateCoroutinesApi
internal class GooglePlayAPI(
    private val appCompatActivity: SubscribeManagerActivity)
{
    companion object
    {
        private const val LINK_CONNECTION_SUCCESS_PAYMENT =
            "this is indicative project"
        private const val LICENCE_KEY =
            "this is indicative project"
    }

    private lateinit var billingClient: BillingClient
    private val customAppDialog =
        CustomAppDialog(this.appCompatActivity)
    private val listGlobalProductsID = listOf("subscribe_1"
        , "subscribe_2"
        , "subscribe_3"
        , "subscribe_4"
        , "subscribe_5"
        , "subscribe_6"
        , "subscribe_7"
        , "subscribe_8"
        , "subscribe_9"
        , "subscribe_10"
        , "subscribe_11"
        , "subscribe_12")

    internal fun callGooglePlayAPI() = CheckedLicenceAPI {
        this@GooglePlayAPI.billingClient = BillingClient
            .newBuilder(appCompatActivity)
            .enablePendingPurchases()
            .setListener { billingResult, mutableList ->
                if (billingResult.responseCode
                    == BillingClient.BillingResponseCode.OK)
                        mutableList?.let { mutableListNonNull ->
                            mutableListNonNull.forEach { singleItem ->
                                if (singleItem.purchaseState
                                    == Purchase.PurchaseState.PURCHASED
                                    && !singleItem.isAcknowledged)
                                    {
                                    CustomPaymentConnectionAPI()
                                        .openConnectionForVerifyToken(
                                            singleActivePurchase = singleItem)
                                    this.customAppDialog.showLoadingDialog(
                                        true, "Обработка платежа...")
                                }
                            }
                        }
            }.build()
        innerConnectGooglePlay()
    }.buildAndRun()

    private fun innerConnectGooglePlay()
    {
        this@GooglePlayAPI.billingClient
            .startConnection(object: BillingClientStateListener
            {
                override fun onBillingServiceDisconnected() =
                    innerConnectGooglePlay()

                override fun onBillingSetupFinished(billingResult: BillingResult) =
                    if (billingResult.responseCode
                        == BillingClient.BillingResponseCode.OK)
                        getAllProductsAndDetails()
                    else
                        Toast.makeText(appCompatActivity
                            , R.string.errorActionBuySubscribe
                            , Toast.LENGTH_LONG)
                            .show()
            })
    }

    private fun getAllProductsAndDetails()
    {
        this.billingClient.querySkuDetailsAsync(SkuDetailsParams
            .newBuilder()
            .setSkusList(listGlobalProductsID)
            .setType(BillingClient.SkuType.INAPP)
            .build())
        { billingResult, skuDeatils ->
            if (billingResult.responseCode
                == BillingClient.BillingResponseCode.OK)
                skuDeatils?.let {
                    this.billingClient.launchBillingFlow(
                        this.appCompatActivity
                        , BillingFlowParams.newBuilder()
                            .setSkuDetails(
                                it.sortedBy { singleElement ->
                                    singleElement
                                        .price
                                        .split(",")[0]
                                        .toInt() }[finalCountPrice - 1])
                            .build())
                }
        }
    }

    private fun asyncPaymentsAction(paymentCallResponseBody: PaymentCallResponseBody?) =
        paymentCallResponseBody?.let { singleBillingResponse ->
            singleBillingResponse.isValid?.let { actionValid ->
                if (actionValid)
                    singleBillingResponse.purchaseToken?.let { actionToken ->
                        this.billingClient.consumeAsync(
                            ConsumeParams
                                .newBuilder()
                                .setPurchaseToken(actionToken)
                                .build())
                        { billingResult, _ ->
                            if (billingResult.responseCode
                                == BillingClient.BillingResponseCode.OK)
                                this@GooglePlayAPI.appCompatActivity
                                    .showSuccessCharge(customAppDialog)
                        }
                    }
            } ?: errorMessage()
        }  ?: errorMessage()

    private fun errorMessage() =
        this@GooglePlayAPI.appCompatActivity.showError(
            appCompatActivity.getString(
                R.string.errorBuySubscribe)
            , this.customAppDialog)

    private inner class CustomPaymentConnectionAPI
    {
        fun openConnectionForVerifyToken(singleActivePurchase: Purchase) =
            Retrofit.Builder()
                .baseUrl(LINK_CONNECTION_SUCCESS_PAYMENT)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CallSinglePaymentAPI::class.java)
                .pushCompleteTokenData(
                    singleActivePurchase.purchaseToken
                    , singleActivePurchase.orderId
                    , singleActivePurchase.purchaseTime)
                .enqueue(object: Callback<PaymentCallResponseBody?>
                {
                    override fun onResponse(
                        call: Call<PaymentCallResponseBody?>
                        , response: Response<PaymentCallResponseBody?>)
                    { asyncPaymentsAction(response.body()) }

                    override fun onFailure(call: Call<PaymentCallResponseBody?>, t: Throwable)
                    {
                        customAppDialog.showLoadingDialog(false, null)
                        Toast.makeText(appCompatActivity
                            , R.string.errorActionBuySubscribe
                            , Toast.LENGTH_LONG)
                            .show()
                    }
                })
    }

    @Keep
    private data class PaymentCallResponseBody @JvmOverloads constructor(
        @field:SerializedName("purchaseToken")
        var purchaseToken: String? = null
        ,
        @field:SerializedName("orderID")
        var orderID: String? = null
        ,
        @field:SerializedName("purchaseTime")
        var purchaseTime: String? = null
        ,
        @field:SerializedName("isValid")
        var isValid: Boolean? = null
    )

    private sealed interface CallSinglePaymentAPI: LicenseCheckerCallback
    {
        @POST("/verifyPurchases")
        fun pushCompleteTokenData(@Query("purchaseToken") purchaseToken: String
         , @Query("orderID") orderID: String
         , @Query("purchaseTime") purchaseTime: Long)
        : Call<PaymentCallResponseBody>
    }

    private inner class CheckedLicenceAPI(
        private val successCheck: Runnable): LicenseCheckerCallback
    {
        @SuppressLint("HardwareIds")
        fun buildAndRun() = LicenseChecker(
            appCompatActivity,
            ServerManagedPolicy(
                appCompatActivity,
                AESObfuscator(
                    Random.nextBytes(20)
                    , appCompatActivity.packageName
                    , Settings.Secure.getString(
                        appCompatActivity.contentResolver
                        , Settings.Secure.ANDROID_ID))), LICENCE_KEY
        )
            .checkAccess(this)

        override fun allow(reason: Int)
        {
           GlobalScope.launch(Dispatchers.Main)
           { successCheck.run() }.start()
        }

        override fun dontAllow(reason: Int) =
            Toast.makeText(appCompatActivity
                , R.string.errorActionBuySubscribe
                , Toast.LENGTH_LONG)
                .show()

        override fun applicationError(errorCode: Int) =
            Toast.makeText(appCompatActivity
                , R.string.errorActionBuySubscribe
                , Toast.LENGTH_LONG)
                .show()
    }
}