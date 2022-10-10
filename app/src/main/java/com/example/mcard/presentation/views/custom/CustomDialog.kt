package com.example.mcard.presentation.views.custom

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.example.mcard.R
import com.example.mcard.databinding.DialogDefaultFormBinding
import com.example.mcard.repository.features.changeDataAction
import com.example.mcard.repository.features.currentScreenPositionIsVertical
import com.example.mcard.repository.features.getDisplaySize
import com.example.mcard.repository.features.setVisible
import com.example.mcard.repository.features.unitAction
import com.example.mcard.repository.source.usage.UsageDialogFragment
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class CustomDialog(
    context: Context,
    private val cancelData: Boolean,
    private val isWaitingCall: Boolean,
    private val isChangePublicDataCall: Boolean,
    private val isChangePrivateDataCall: Boolean,
    private val messageId: Int,
    private var messageText: CharSequence?,
    private var titleId: Int,
    private var positiveAction: unitAction?,
    private var positiveActionWithParams: changeDataAction?,
    private var negativeAction: unitAction?,
    private var additionalAction: unitAction?,
) : Dialog(context), UsageDialogFragment {

    private val dialogBinding: DialogDefaultFormBinding by lazy {
        DialogDefaultFormBinding.inflate(layoutInflater)
    }

    init {
        setContentView(dialogBinding.root)
        updateConfigOptions()
        optionalSettings()
    }

    fun updateConfigOptions() {

        if (isWaitingCall)
            return

        getDisplaySize().run {

            (if (context.currentScreenPositionIsVertical())
                0.90
            else
                0.60)
                .run widthRatio@{

                    window?.setLayout(
                        (first * this@widthRatio).toInt(),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
        }
    }

    private fun optionalSettings() {

        this.dialogBinding.apply {

            window?.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )

            if (isWaitingCall) {
                dialogBinding.loadingBar setVisible true
                dialogBinding.panelSeparator setVisible false

                setOnCancelListener {
                    hide()
                }

                setCancelable(false)
                return@apply
            } else if (isChangePublicDataCall || isChangePrivateDataCall) {
                messageTextView setVisible false
                panelSeparator setVisible false
                monoDialogControlPanel.root setVisible false
                singleDialogControlPanel.root setVisible false

                includeChangePrivateData.run {

                    titleTextView.setText(titleId)
                    root setVisible true

                    panel.btnNegative.setOnClickListener {
                        negativeAction?.invoke()
                        hide()
                    }

                    panel.btnPositive.setOnClickListener {
                        positiveActionWithParams?.invoke(
                            this.dataOldView.text.toString(),
                            this.dataNewView.text.toString()
                        )

                        hide()
                    }

                    if (isChangePrivateDataCall) {
                        dataNewView.inputType =
                            InputType.TYPE_TEXT_VARIATION_PASSWORD

                        dataOldView.inputType =
                            InputType.TYPE_TEXT_VARIATION_PASSWORD

                        dataNew.endIconMode =
                            TextInputLayout.END_ICON_PASSWORD_TOGGLE
                    }

                    return@apply
                }
            }

            titleTextView.setText(titleId)

            messageTextView.movementMethod =
                LinkMovementMethod.getInstance()

            messageTextView.text =
                context.getString(messageId).ifEmpty {
                    messageText ?: ""
                }

            positiveAction.apply {
                monoDialogControlPanel.root setVisible true
                monoDialogControlPanel.btnPositive.setOnClickListener {
                    cancel()
                    this?.invoke()
                }
            }

            negativeAction.apply {
                monoDialogControlPanel.root setVisible true
                monoDialogControlPanel.btnNegative.setOnClickListener {
                    cancel()
                    this?.invoke()
                }
            }

            if (negativeAction == null) {
                monoDialogControlPanel.root setVisible false
                singleDialogControlPanel.root setVisible true

                singleDialogControlPanel.btnAdditional.setOnClickListener {
                    cancel()
                }
            }

            if (titleId == R.string.empty)
                titleTextView.visibility =
                    View.GONE
        }
    }

    override fun show() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                super.show()
            } catch (_: WindowManager.BadTokenException) {
            }
        }
    }

    override fun hide() {
        CoroutineScope(Dispatchers.Main).launch {
            setCancelable(true)
            cancel()
            super.hide()
        }
    }
}

internal class CustomDialogBuilder(
    private val context: Context,
) {
    private var messageId: Int = R.string.empty
    private var messageText: CharSequence = ""
    private var titleId: Int = R.string.empty
    private var isCancelable = true

    private var isWaitingCall = false
    private var isChangePublicDataCall = false
    private var isChangePrivateDataCall = false

    private var positiveAction: unitAction? = null
    private var positiveActionWithParams: changeDataAction? = null
    private var negativeAction: unitAction? = null
    private var additionalAction: unitAction? = null

    fun setTitle(titleId: Int) = apply {
        this.titleId = titleId
    }

    fun setMessage(messageId: Int) = apply {
        this.messageId = messageId
    }

    fun setMessage(messageText: CharSequence) = apply {
        this.messageText = messageText
    }

    fun setCancelable(isCancelable: Boolean) = apply {
        this.isCancelable = isCancelable
    }

    fun setPositiveAction(action: unitAction) = apply {
        this.positiveAction = action
    }

    fun setPositiveActionWithParams(action: changeDataAction) = apply {
        this.positiveActionWithParams = action
    }

    fun setNegativeAction(action: unitAction) = apply {
        this.negativeAction = action
    }

    fun setAdditionalAction(action: unitAction) = apply {
        this.additionalAction = action
    }

    fun setWaitingDialog() = apply {
        isWaitingCall = true
    }

    fun setChangePublicDataDialog() = apply {
        isChangePublicDataCall = true
    }

    fun setChangePrivateDataDialog() = apply {
        isChangePrivateDataCall = true
    }

    fun build(): UsageDialogFragment =
        CustomDialog(
            context = context,
            titleId = titleId,
            cancelData = isCancelable,
            isWaitingCall = isWaitingCall,
            isChangePrivateDataCall = isChangePrivateDataCall,
            isChangePublicDataCall = isChangePublicDataCall,
            messageId = messageId,
            messageText = messageText,
            positiveActionWithParams = positiveActionWithParams,
            positiveAction = positiveAction,
            negativeAction = negativeAction,
            additionalAction = additionalAction
        )
}