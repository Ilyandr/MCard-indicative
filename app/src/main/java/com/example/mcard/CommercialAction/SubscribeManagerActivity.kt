@file:Suppress("DEPRECATION")

package com.example.mcard.CommercialAction

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.AppCompatTextView
import com.example.mcard.BasicAppActivity
import com.example.mcard.CommercialAction.PaymentCardsManager.show
import com.example.mcard.CommercialAction.PaymentsAPI.YKassaAPI
import com.example.mcard.CommercialAction.PaymentsAPI.YKassaAPI.REQUEST_CODE_3DSECURE
import com.example.mcard.CommercialAction.PaymentsAPI.YKassaAPI.REQUEST_CODE_TOKENIZE
import com.example.mcard.CommercialAction.PaymentsAPI.YKassaAPI.YKassaPaymentAPI.confirmPayment
import com.example.mcard.CommercialAction.PaymentsAPI.YKassaAPI.YKassaPaymentAPI.launchConnect
import com.example.mcard.GroupServerActions.BasicFireBaseManager
import com.example.mcard.GroupServerActions.SubscribeController
import com.example.mcard.GroupServerActions.SubscribeController.Companion.realDate
import com.example.mcard.R
import com.example.mcard.SideFunctionality.CustomAppDialog
import com.example.mcard.SideFunctionality.GeneralStructApp
import com.example.mcard.GeneralInterfaceApp.ThemeAppController
import com.example.mcard.StorageAppActions.DataInterfaceCard
import com.example.mcard.StorageAppActions.SharedPreferencesManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.yoomoney.sdk.kassa.payments.Checkout
import ru.yoomoney.sdk.kassa.payments.Checkout.createConfirmationIntent
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType

@DelicateCoroutinesApi
internal open class SubscribeManagerActivity : AppCompatActivity()
 , GeneralStructApp
{
    private lateinit var btnSubscribe: AppCompatButton
    private lateinit var btnBack: AppCompatImageButton
    private lateinit var btnFaq: AppCompatTextView
    private lateinit var btnInfoDate: AppCompatTextView
    private lateinit var seekBarPay: AppCompatSeekBar
    private lateinit var textInfoPay: TextView

    private lateinit var sharedpreferencesManager: SharedPreferencesManager
    private lateinit var connectionmasterFirebase: BasicFireBaseManager
    private lateinit var subscribeServerManager: SubscribeController

    private lateinit var animSelect: Animation
    private var updateSubscribe: Boolean = false
    private lateinit var loadingDialogs: CustomAppDialog

    companion object
    {
        @JvmStatic internal var finalPrice = 49
        @JvmStatic internal var finalCountPrice = 1
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscribe)
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        findObjects()
        drawableView()
        basicWork()
    }

    override fun findObjects()
    {
        this.btnSubscribe = findViewById(R.id.btn_subscribe)
        this.btnBack = findViewById(R.id.btn_back)
        this.seekBarPay = findViewById(R.id.seekbarPay)
        this.textInfoPay = findViewById(R.id.textPayInfo)
        this.btnFaq = findViewById(R.id.btnFAQ)
        this.btnInfoDate = findViewById(R.id.subscribeInfo)

        this.sharedpreferencesManager =
            SharedPreferencesManager(this)
        this.connectionmasterFirebase =
            BasicFireBaseManager(this)
        this.subscribeServerManager =
            SubscribeController(this)
        this.loadingDialogs =
            CustomAppDialog(this)
        this.animSelect = AnimationUtils
            .loadAnimation(this, R.anim.select_object)
    }

    @SuppressLint("SetTextI18n")
    override fun drawableView()
    {
        val controllerThemeApp =
            ThemeAppController(
                DataInterfaceCard(this))

       controllerThemeApp.changeDesignIconBar(
           findViewById(R.id.bar_icon))
       controllerThemeApp.changeDesignDefaultView(
           findViewById(R.id.main_linear_reception))
       controllerThemeApp.settingsText(
           findViewById(R.id.name_fragment)
           , getString(R.string.left_menu_subscribe))

        controllerThemeApp.setOptionsButtons(
            btnBack
            , btnFaq
            , btnInfoDate
            , btnSubscribe)

        controllerThemeApp.settingsText(textInfoPay
            , "Стоимость подписки на 1 месяц - составит $finalPrice рублей.")
    }

    override fun basicWork()
    {
        this.window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        this.loadingDialogs.showLoadingDialog(
            true, getString(R.string.loadingInfo))

        this.subscribeServerManager.outputSubInfo(
            this.loadingDialogs
            , this.btnInfoDate
            , this.seekBarPay
            , this.textInfoPay)

        this.btnSubscribe.setOnClickListener {
            it.startAnimation(this.animSelect)
            btnSubscribeWork(updateSubscribe)
        }
        this.btnFaq.setOnClickListener {
            it.startAnimation(this.animSelect)
            openInfoSubscribe()
        }
        this.btnBack.setOnClickListener {
            it.startAnimation(this.animSelect)
            onBackPressed()
        }
        this.seekBarPay.setOnSeekBarChangeListener(
            SeekBarPayWork(this.textInfoPay))
    }

    internal fun showError(
        message: String, customAppLoadingDialogs: CustomAppDialog) =
        GlobalScope.launch(Dispatchers.Main) {
            customAppLoadingDialogs.showLoadingDialog(
                false, null)
        showOkDialog(message)
    }.start()

    internal fun showSuccessCharge(customAppLoadingDialogs: CustomAppDialog) =
        GlobalScope.launch(Dispatchers.Main) {
            this@SubscribeManagerActivity.subscribeServerManager.checkSubscribe(
                SubscribeController.MODE_POST
                , realDate()
                , textInfoPay.getCountDate()
                , updateSubscribe)
            {
                this@SubscribeManagerActivity
                    .subscribeServerManager
                    .outputSubInfo(
                        dataView = btnInfoDate
                        , seekBarPay = seekBarPay
                        , textSubInfo =  textInfoPay)

                customAppLoadingDialogs.showLoadingDialog(
                    false, null)
                showOkDialog(getString(
                    R.string.infoSuccessBuySubscribe))
            }
        }.start()

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode)
        {
            REQUEST_CODE_TOKENIZE ->
            {
                if (resultCode == RESULT_OK)
                {
                    this.loadingDialogs.showLoadingDialog(
                        true, "Обработка платежа..")
                    launchConnect(
                        Checkout.createTokenizationResult(
                            data ?: return).paymentToken
                        , YKassaAPI.YKassaPaymentAPI.PaymentActionEntity(
                            { redirectAction(it as String) }
                            , { showSuccessCharge(this.loadingDialogs) }
                            , { showMerchantIdNotSet() }
                        )
                    )
                }
            }
           REQUEST_CODE_3DSECURE ->
           GlobalScope.launch(Dispatchers.Main)
               {
                   if (resultCode == RESULT_OK)
                       confirmPayment()
                   else
                       showMerchantIdNotSet()
               }.start()
        }
    }

    private fun redirectAction(confirmUrl: String) =
        startActivityForResult(
            createConfirmationIntent(
                this
                , confirmUrl
                , PaymentMethodType.BANK_CARD)
            , REQUEST_CODE_3DSECURE)

    fun showMerchantIdNotSet()
    {
        this.loadingDialogs.showLoadingDialog(false)
        showOkDialog(getString(R.string.errorPaymentInfo))
    }

    private fun showOkDialog(message: String) =
        CustomAppDialog(this)
            .buildEntityDialog(true)
            .setTitle("Менеджер уведомлений")
            .setMessage(message, 3.5f)
            .setPositiveButton("Понятно")
            .show()

    fun showNetworkErrorRetryPayment(retry: Runnable) =
        CustomAppDialog(this)
            .buildEntityDialog(true)
            .setTitle("Менеджер уведомлений")
            .setMessage(R.string.errorBuySubscribe, 4.5f)
            .setPositiveButton("Повторить") { retry.run() }
            .setNegativeButton("Отмена")
            .show()

    private fun btnSubscribeWork(updateSubscribe: Boolean)
    {
        if (haveSubscribe() && !updateSubscribe)
        {
            if (finalPrice != 0)
            this.subscribeServerManager
                .updateSubscribe(this.textInfoPay.getCountDate()
                        .split("м")[0]
                        .replace(" ", "")
                        .toInt(), ::btnSubscribeWork)
            else
            {
                Toast.makeText(this
                    , getString(R.string.errorSubscribePrice)
                    , Toast.LENGTH_LONG).show()
                return
            }
        }

        if (finalPrice == 0)
        {
            Toast.makeText(this
                , "Ошибка - у вас максимальный срок подписки"
                , Toast.LENGTH_SHORT)
                .show()
            return
        }

        this.updateSubscribe = updateSubscribe
        this show Pair(
            finalPrice
            , this.textInfoPay.getCountDate().replace("-", ""))
    }

    private fun openInfoSubscribe() =
        CustomAppDialog(this)
            .setInfoSubscribeClass()
            .buildEntityDialog(true)
            .setTitle("Подписка MCard+")
            .setMessage(R.string.subscribeInfo, 16.5f)
            .setPositiveButton("Понятно", null)
            .show()

    override fun onBackPressed()
    {
        super.onBackPressed()
        startActivity(Intent(
            this, BasicAppActivity::class.java))
        finish()
    }

   private inner class SeekBarPayWork(private val textView: TextView)
       : SeekBar.OnSeekBarChangeListener
    {
        private val DEFAULT_PRICE_RUB = 49
        private val UPDATE_PRICE_SALE_RUB = 30

        @SuppressLint("SetTextI18n")
        override fun onProgressChanged(seekBar: SeekBar?
        , progress: Int
        , fromUser: Boolean)
        {
            if (finalPrice == 0)
            {
                seekBar?.progress = seekBar?.max!!
                return
            }

            finalCountPrice =
                if (progress == 0) 1 else progress + 1
            finalPrice =
                calculatePrice(finalCountPrice)

            if (subscribeServerManager.actualProgress > 1)
                finalCountPrice += 2

                this.textView.text = "Стоимость${
                    if (subscribeServerManager.actualProgress > 1)
                        " продления" else ""} " +
                        "подписки на $finalCountPrice " +
                        "${setNameTextParamData(finalCountPrice)} " +
                        "составит: " +
                        "${if (finalPrice == 0) "-" 
                        else finalPrice} ${setNameTextParamPrice(finalPrice)}."

                updateSubscribe =
                    (subscribeServerManager.actualProgress > 0)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        private fun setNameTextParamData(progress: Int) = when(progress)
        {
             1 -> "месяц"
             in 2..4 -> "месяца"
             else -> "месяцев"
        }

         fun calculatePrice(progress: Int) = when(progress)
         {
             1 -> 49
             2 -> 79
             3 -> 118
             4 -> 148
             5 -> 217
             6 -> 247
             7 -> 316
             8 -> 346
             9 -> 415
             10 -> 445
             11 -> 479
             12 -> 512
             else -> 0
         }

        fun setNameTextParamPrice(num: Int) =
            if ((num % 100 / 10) == 1) "рублей"
            else when (num % 10)
            {
                1 -> "рубль"
                in 2..4 -> "рубля"
                else -> "рублей"
            }
    }

    private fun TextView.getCountDate() = this.text
        .toString()
        .split("на")[1]
        .split("составит")[0]

    private fun haveSubscribe() = this.btnInfoDate.hint.toString()!=
            getString(R.string.dontHaveSubscribe)
}