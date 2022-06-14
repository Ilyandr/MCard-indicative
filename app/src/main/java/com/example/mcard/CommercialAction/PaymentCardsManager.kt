package com.example.mcard.CommercialAction

import android.annotation.SuppressLint
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.view.LayoutInflater
import com.example.mcard.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.example.mcard.CommercialAction.PaymentsAPI.GooglePlayAPI
import com.example.mcard.CommercialAction.PaymentsAPI.YKassaAPI.startTokenize
import com.example.mcard.GroupServerActions.BasicFireBaseManager
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
internal object PaymentCardsManager
{
    private var outputDataForClient: Pair<Int, String>? = null
    private var showing = false
    private const val SHOWING_KEY = "showing"

    @SuppressLint("InflateParams", "SetTextI18n", "UseCompatLoadingForDrawables")
    infix fun SubscribeManagerActivity?.show(
        outputDataForClient: Pair<Int, String>?)
    {
        showing = true
        val dialog = BottomSheetDialog(
            this ?: return)
        val paymentView =
            LayoutInflater.from(this).inflate(
                R.layout.additional_fragment_payment, null)
        val closeButton = paymentView.findViewById<View>(
            R.id.close_sheet_button)
        val payWithGooglePlayButton = paymentView.findViewById<AppCompatButton>(
            R.id.pay_with_google_button)
        val payWithYKassaButton = paymentView.findViewById<AppCompatButton>(
            R.id.pay_with_cards)
        val animationClick = AnimationUtils
            .loadAnimation(this, R.anim.select_object)
        paymentView.findViewById<AppCompatTextView>(
            R.id.subcribeInfoTV).text =
            "Выбранный срок: ${outputDataForClient?.second}"

        paymentView.findViewById<AppCompatTextView>(
            R.id.price).text =
            "${outputDataForClient?.first} ${getString(R.string.CURRENCY_APP_RU)}."

        BasicFireBaseManager(this)
            .avaibleAdditionalPayment(payWithYKassaButton)
            {
                it.startAnimation(animationClick)
                dialog.dismiss()
                showing = false

                outputDataForClient?.let { comletePaymentData ->
                    this startTokenize comletePaymentData
                }
            }

        payWithGooglePlayButton.setOnClickListener {
            it.startAnimation(animationClick)
            dialog.dismiss()
            showing = false

            GooglePlayAPI(this)
                .callGooglePlayAPI()
        }

        payWithGooglePlayButton.setCompoundDrawablesWithIntrinsicBounds(
            this.getDrawable(
                R.drawable.icon_google_play), null,null, null)
        payWithYKassaButton.setCompoundDrawablesWithIntrinsicBounds(
            this.getDrawable(
                R.drawable.icon_bank_card), null,null, null)

        closeButton.setOnClickListener { dialog.dismiss() }
        dialog.setContentView(paymentView)

        val behavior: BottomSheetBehavior<*> =
            BottomSheetBehavior.from(
                paymentView.parent as View)

        dialog.setOnShowListener {
            behavior.setPeekHeight(
                paymentView.height)
        }
        dialog.setOnCancelListener { showing = false }
        dialog.setCancelable(true)
        dialog.show()
    }

    fun onSaveInstanceState(outState: Bundle) =
        outState.putBoolean(
            SHOWING_KEY, showing)

    fun onRestoreInstanceState(
        activity: SubscribeManagerActivity?, savedInstanceState: Bundle)
    {
        if (savedInstanceState.getBoolean(SHOWING_KEY))
            activity show this.outputDataForClient
    }
}