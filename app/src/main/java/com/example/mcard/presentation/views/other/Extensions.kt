package com.example.mcard.presentation.views.other

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.example.mcard.R
import com.example.mcard.databinding.BottomSheetLayoutBinding
import com.example.mcard.databinding.ChildAdditionallyFragmentBinding
import com.example.mcard.databinding.CustomToastLayoutBinding
import com.example.mcard.presentation.adapters.SheetViewAdapter
import com.example.mcard.repository.features.dataConfirmPaymentAction
import com.example.mcard.repository.features.optionally.CardsSorting
import com.example.mcard.repository.features.unitAction
import com.example.mcard.repository.models.other.SheetModel
import com.example.mcard.repository.models.storage.CardWithHistoryEntity
import com.example.mcard.repository.source.usage.ChangeCardSource
import com.example.mcard.repository.source.usage.EntranceUsageSource
import com.google.android.material.bottomsheet.BottomSheetDialog


internal fun View.createOptionsExtensionDialog(
    source: ChangeCardSource,
    data: CardWithHistoryEntity?,
    imageAction: unitAction,
): BottomSheetDialog =
    BottomSheetDialog(
        this.context, R.style.BottomSheetDialogTheme
    ).apply dialog@{

        BottomSheetLayoutBinding.bind(
            LayoutInflater.from(
                this@createOptionsExtensionDialog.context
            ).inflate(
                R.layout.bottom_sheet_layout,
                this.findViewById(
                    R.id.containerBottomSheet
                )
            )
        ).apply content@{

            headerTextView.setText(
                R.string.headerTitleCardOpenFragment
            )

            itemsContainer.adapter =
                SheetViewAdapter(
                    listOf(
                        SheetModel(
                            textId = R.string.menuITitleCardOptions1,
                            iconId = R.drawable.ic_barcode,
                            isCheckable = false,
                            action = source::barcodeAction
                        ),
                        SheetModel(
                            textId = R.string.menuITitleCardOptions2,
                            iconId = R.drawable.ic_image,
                            isCheckable = false,
                            action = imageAction
                        ),
                        SheetModel(
                            textId = R.string.menuITitleCardOptions3,
                            iconId = R.drawable.ic_publish,
                            isCheckable = false,
                        ) {
                            if (source.checkOfflineEntrance())
                                source publishAction data
                            else
                                source showOfferDialogRegistration this@createOptionsExtensionDialog
                        },
                        SheetModel(
                            textId = R.string.menuITitleCardOptions4,
                            iconId = R.drawable.ic_delete,
                            isCheckable = false,
                        ) {
                            source removeAction data
                        },
                    ), this@dialog::dismiss
                )

            setContentView(this@content.root)
        }
    }

internal inline fun View.createSortingExtensionDialog(
    changeCardSourse: EntranceUsageSource,
    crossinline action: unitAction,
): BottomSheetDialog =
    BottomSheetDialog(
        this.context, R.style.BottomSheetDialogTheme
    ).apply dialog@{

        BottomSheetLayoutBinding.bind(
            LayoutInflater.from(
                this@createSortingExtensionDialog.context
            ).inflate(
                R.layout.bottom_sheet_layout,
                this.findViewById(
                    R.id.containerBottomSheet
                )
            )
        ).apply content@{

            headerTextView.setText(
                R.string.headerTitleSorting
            )

            changeCardSourse.userPreferences.sortedCardInfo().apply {

                itemsContainer.adapter =
                    SheetViewAdapter(
                        listOf(
                            SheetModel(
                                textId = R.string.menuSortInfo1,
                                isCheckable = true,
                                singleChecked = (this == CardsSorting.GEO_SORT)
                            ) {
                                if (changeCardSourse.checkOfflineEntrance()) {
                                    changeCardSourse.userPreferences.sortedCardInfo(
                                        CardsSorting.GEO_SORT
                                    )
                                    action.invoke()
                                } else
                                    changeCardSourse showOfferDialogRegistration this@createSortingExtensionDialog
                            },
                            SheetModel(
                                textId = R.string.menuSortInfo2,
                                isCheckable = true,
                                singleChecked = (this == CardsSorting.FREQUENCY_SORT)
                            ) {
                                changeCardSourse.userPreferences.sortedCardInfo(
                                    CardsSorting.FREQUENCY_SORT
                                )
                                action.invoke()
                            },
                            SheetModel(
                                textId = R.string.menuSortInfo3,
                                isCheckable = true,
                                singleChecked = (this == CardsSorting.ABC_SORT)
                            ) {
                                changeCardSourse.userPreferences.sortedCardInfo(
                                    CardsSorting.ABC_SORT
                                )
                                action.invoke()
                            },
                            SheetModel(
                                textId = R.string.menuSortInfo4,
                                isCheckable = true,
                                singleChecked = (this == CardsSorting.DATE_SORT)
                            ) {
                                changeCardSourse.userPreferences.sortedCardInfo(
                                    CardsSorting.DATE_SORT
                                )
                                action.invoke()
                            }
                        ), this@dialog::dismiss
                    )
            }

            setContentView(this@content.root)
        }
    }

internal inline fun Context.createPaymentDialog(
    crossinline actionFault: unitAction,
    crossinline actionSuccess: dataConfirmPaymentAction,
): BottomSheetDialog =
    BottomSheetDialog(
        this, R.style.BottomSheetDialogTheme
    ).apply dialog@{

        ChildAdditionallyFragmentBinding.bind(
            LayoutInflater.from(
                this@createPaymentDialog
            ).inflate(
                R.layout.child_additionally_fragment,
                this.findViewById(
                    R.id.containerBottomSheet
                )
            )
        ).apply content@{

            paymentButton.setOnClickListener {

                try {
                    paymentView.text.toString().toDoubleOrNull()?.run {
                        if (this >= 1.0)
                            actionSuccess.invoke(
                                this@dialog, this
                            )
                        else
                            actionFault.invoke()
                    }
                } catch (ex: Exception) {
                    actionFault.invoke()
                }
            }

            setContentView(this@content.root)
        }
    }

@SuppressLint("InflateParams")
internal inline infix fun <reified Type> Context.showMessage(
    message: Type,
) =
    Toast(this).apply toast@{
        CustomToastLayoutBinding.bind(
            LayoutInflater.from(
                this@showMessage
            ).inflate(
                R.layout.custom_toast_layout, null
            )
        ).apply {

            when (message) {
                is Int -> this.messageView.setText(message)
                is String -> this.messageView.text = message
                else -> return@toast
            }

            @Suppress("DEPRECATION")
            view = root
            duration = Toast.LENGTH_LONG
        }
    }.show()