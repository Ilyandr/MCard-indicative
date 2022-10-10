package com.example.mcard.repository.features.payment.dataSource

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.mcard.presentation.views.custom.CustomDialogBuilder
import com.example.mcard.repository.features.dataPaymentAction
import com.example.mcard.repository.features.unitAction
import com.example.mcard.repository.features.utils.CURRENCY_APP_RU
import com.example.mcard.repository.features.utils.GENERAL_CLIENT_API_ID
import com.example.mcard.repository.features.utils.GENERAL_PAYMENT_TITLE
import com.example.mcard.repository.features.utils.GENERAL_SHOP_API_PASSWORD
import com.example.mcard.repository.features.utils.GENERAL_SHOP_ID
import com.example.mcard.repository.features.utils.GENERAL_SHOP_PASSWORD
import com.example.mcard.repository.models.other.PaymentModel
import com.example.mcard.repository.source.usage.UsageDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Credentials
import org.json.JSONObject
import ru.yoomoney.sdk.kassa.payments.Checkout
import ru.yoomoney.sdk.kassa.payments.Checkout.createTokenizeIntent
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.UiParameters
import java.math.BigDecimal
import java.util.Currency
import java.util.Random


internal class PaymentController(
    private val dataSource: PaymentDataSource,
    private val resultRegister: Fragment,
    private val successAction: dataPaymentAction,
    private var faultAction: unitAction,
) {
    private var price: Double = 1.0
    private var paymentId: String? = null

    private val loadingDialog: UsageDialogFragment by lazy {
        CustomDialogBuilder(
            resultRegister.requireActivity()
        ).setWaitingDialog().build()
    }

    init {
        faultAction = {
            CoroutineScope(Dispatchers.Main).launch {
                loadingDialog.hide()
                faultAction.invoke()
            }
        }
    }

    private val launchTokenize =
        resultRegister.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == AppCompatActivity.RESULT_OK)
                it.data?.run {
                    loadingDialog.show()

                    sendRequestWithToken(
                        Checkout.createTokenizationResult(
                            this
                        ).paymentToken
                    )
                } ?: run {
                    faultAction.invoke()
                }
        }

    private val redirectAction =
        resultRegister.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                loadingDialog.show()
                sendConfirmPayment()
            } else
                faultAction.invoke()
        }

    infix fun showPaymentDialog(price: Double) {
        this.price = price

        launchTokenize.launch(
            createTokenizeIntent(
                resultRegister.requireActivity(),
                PaymentParameters(
                    amount = Amount(
                        BigDecimal.valueOf(price),
                        Currency.getInstance(CURRENCY_APP_RU)
                    ),
                    title = GENERAL_PAYMENT_TITLE,
                    clientApplicationKey = GENERAL_SHOP_API_PASSWORD,
                    subtitle = "",
                    shopId = GENERAL_SHOP_ID,
                    savePaymentMethod = SavePaymentMethod.OFF,
                    paymentMethodTypes = setOf(
                        PaymentMethodType.BANK_CARD
                    ),
                    authCenterClientId = GENERAL_CLIENT_API_ID
                ),
                uiParameters = UiParameters(
                    showLogo = false
                ),
            )
        )
    }

    private infix fun sendRequestWithToken(
        token: String,
    ) {
        dataSource.sendTokenizeData(
            headerAuth = requireHeaderAuth(),
            uniqueOperationKey = "${Random().nextInt(11000612) + 17}",
            sendPaymentDataEntity = PaymentModel(
                token, com.example.mcard.repository.models.other.Amount(
                    BigDecimal.valueOf(
                        price
                    ).toString(),
                    Currency.getInstance(
                        CURRENCY_APP_RU
                    ).toString()
                ), true
            ), faultAction
        ) { result ->
            checkedPaymentResult(
                JSONObject(
                    result
                )
            )
        }
    }

    private fun requireHeaderAuth() =
        Credentials.basic(
            GENERAL_SHOP_ID,
            GENERAL_SHOP_PASSWORD
        )

    private fun sendConfirmPayment() {

        paymentId?.run {

            dataSource.getPaymentResult(
                headerAuth = requireHeaderAuth(),
                singlePaymentID = this,
                faultAction = faultAction
            ) { result ->
                checkedPaymentResult(
                    JSONObject(result)
                )
            }
        }
    }

    private fun checkedPaymentResult(allJsonObject: JSONObject?) {
        allJsonObject?.let {
            CoroutineScope(Dispatchers.Main).launch {

                if (it.getString("status").equals("succeeded")) {
                    loadingDialog.hide()
                    successAction.invoke(price)
                } else if (
                    it.getString("status").equals("pending")
                ) {
                    paymentId = allJsonObject.getString("id")

                    redirectAction.launch(
                        Checkout.createConfirmationIntent(
                            resultRegister.requireContext(),
                            allJsonObject
                                .getJSONObject("confirmation")
                                .getString("confirmation_url"),
                            PaymentMethodType.BANK_CARD
                        )
                    )
                } else
                    faultAction.invoke()
            }
        }
    }
}
