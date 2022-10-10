package com.example.mcard.repository.source.usage

import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.example.mcard.R
import com.example.mcard.presentation.views.custom.CustomDialogBuilder
import com.example.mcard.repository.features.TRANSACTION_KEY
import com.example.mcard.repository.features.navigateTo
import com.example.mcard.repository.features.storage.preferences.UserPreferences
import kotlinx.coroutines.*

internal interface EntranceUsageSource {

    var userPreferences: UserPreferences

    fun checkOfflineEntrance() =
        userPreferences.setUserData(
            null, null
        ) != "- -"

    infix fun showOfferDialogRegistration(
        anchor: View,
    ) =
        CoroutineScope(Dispatchers.Main).launch {
            CustomDialogBuilder(anchor.context)
                .setTitle(R.string.registerTitle)
                .setMessage(R.string.waningMessageRegister)
                .setNegativeAction { }
                .setPositiveAction {
                    anchor.navigateTo(
                        R.id.registrationFragment,
                        bundleOf(
                            TRANSACTION_KEY to true
                        )
                    )
                }.build().show()
        }

    infix fun showOfferDialogRegistration(
        controller: NavController,
    ) =
        CoroutineScope(Dispatchers.Main).launch {
            CustomDialogBuilder(controller.context)
                .setTitle(R.string.registerTitle)
                .setMessage(R.string.waningMessageRegister)
                .setNegativeAction { }
                .setPositiveAction {
                    controller.navigateTo(
                        R.id.registrationFragment,
                        bundleOf(
                            TRANSACTION_KEY to true
                        )
                    )
                }.build().show()
        }
}