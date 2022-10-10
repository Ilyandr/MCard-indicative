package com.example.mcard.presentation.support

import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.preference.Preference
import com.example.mcard.R
import com.example.mcard.databinding.AdditionallyFragmentBinding
import com.example.mcard.domain.viewModels.basic.other.SettingsViewModel
import com.example.mcard.domain.viewModels.features.AdditionallyViewModel
import com.example.mcard.presentation.controllers.basic.other.SettingsFragment
import com.example.mcard.presentation.views.custom.CustomDialogBuilder
import com.example.mcard.presentation.views.other.createPaymentDialog
import com.example.mcard.presentation.views.other.initViewToolBar
import com.example.mcard.presentation.views.other.showMessage
import com.example.mcard.repository.features.changeVisibleBottomNavBar
import com.example.mcard.repository.features.requireDrawerLayout
import com.example.mcard.repository.features.sewLinkToString
import com.example.mcard.repository.models.other.SpannableData

internal fun SettingsFragment.setSettingsFragmentActions(
    anchorView: View,
    host: FragmentActivity,
    viewModel: SettingsViewModel,
) {
    host changeVisibleBottomNavBar true
    requireActivity().requireDrawerLayout()?.lockDrawer()

    findPreference<Preference>(
        getString(
            R.string.loginPreferencesKey
        )
    )?.setOnPreferenceClickListener {

        if (viewModel.checkOfflineEntrance())
            CustomDialogBuilder(host)
                .setChangePublicDataDialog()
                .setPositiveActionWithParams { oldData, newData ->
                    viewModel.changeLogin(
                        oldData, newData
                    )
                }.setTitle(R.string.loginPreferencesTitle).build().show()
        else
            viewModel showOfferDialogRegistration anchorView

        false
    }

    findPreference<Preference>(
        getString(
            R.string.passwordPreferencesKey
        )
    )?.setOnPreferenceClickListener {

        if (viewModel.checkOfflineEntrance())
            CustomDialogBuilder(host)
                .setChangePrivateDataDialog()
                .setPositiveActionWithParams { oldData, newData ->
                    viewModel.changePassword(
                        oldData, newData
                    )
                }.setTitle(R.string.passwordPreferencesTitle).build().show()
        else
            viewModel showOfferDialogRegistration anchorView

        false
    }

    findPreference<Preference>(
        getString(
            R.string.deleteAccountPreferencesKey
        )
    )?.setOnPreferenceClickListener {

        if (viewModel.checkOfflineEntrance())
            CustomDialogBuilder(host)
                .setTitle(R.string.deleteAccountPreferencesTitle)
                .setMessage(R.string.deleteAccountPreferencesMessage)
                .setNegativeAction { }
                .setPositiveAction {
                    viewModel.deleteAccount()
                }.build().show()
        else
            viewModel showOfferDialogRegistration anchorView

        false
    }

    findPreference<Preference>(
        getString(
            R.string.faqPreferencesKey
        )
    )?.setOnPreferenceClickListener {

        CustomDialogBuilder(host)
            .setTitle(R.string.faqPreferencesTitle)
            .setMessage(
                getString(
                    R.string.faqPreferencesMessageHeader
                ).sewLinkToString(
                    host, SpannableData(
                        R.string.authorLink, 67, 78
                    ), SpannableData(
                        R.string.githubLink, 79, 90
                    ),
                    SpannableData(
                        R.string.policyLink, 91, 118
                    )
                )
            ).build().show()

        false
    }

    findPreference<Preference>(
        getString(
            R.string.cachePreferencesKey
        )
    )?.setOnPreferenceClickListener {

        CustomDialogBuilder(host)
            .setTitle(R.string.cachePreferencesTitle)
            .setMessage(R.string.cachePreferencesMessage)
            .setNegativeAction { }
            .setPositiveAction {
                viewModel.clearCache()
            }.build().show()

        false
    }
}

internal fun AdditionallyFragmentBinding.setAdditionallyFragmentActions(
    host: FragmentActivity,
    viewModel: AdditionallyViewModel,
) =
    host.run {

        this changeVisibleBottomNavBar true
        requireDrawerLayout()?.lockDrawer()

        barLayout.toolbarView.run {
            initViewToolBar()
            setTitle(R.string.headerTitleCardOpenFragment)
        }

        paymentButton.setOnClickListener {

            createPaymentDialog(actionFault = {
                this showMessage R.string.paymentError
            }) { dialog, price ->
                dialog.dismiss()
                viewModel.paymentController?.showPaymentDialog(price)
            }.show()
        }
    }