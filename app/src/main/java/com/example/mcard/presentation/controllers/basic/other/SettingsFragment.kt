package com.example.mcard.presentation.controllers.basic.other

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import com.example.mcard.R
import com.example.mcard.domain.factories.viewModels.SupportSettingsViewModelFactory
import com.example.mcard.domain.models.basic.other.SettingsModel
import com.example.mcard.domain.viewModels.basic.other.SettingsViewModel
import com.example.mcard.presentation.controllers.HostActivity
import com.example.mcard.presentation.support.setSettingsFragmentActions
import com.example.mcard.presentation.views.custom.CustomDialogBuilder
import com.example.mcard.presentation.views.other.initViewToolBar
import com.example.mcard.presentation.views.other.showMessage
import com.example.mcard.repository.features.navigateTo
import com.example.mcard.repository.features.requireDrawerLayout
import com.example.mcard.repository.features.utils.DesignCardManager.removeCacheApp
import com.example.mcard.repository.source.architecture.view.LivePreferenceFragment
import com.example.mcard.repository.source.usage.UsageDialogFragment
import com.google.android.material.appbar.MaterialToolbar
import javax.inject.Inject

internal class SettingsFragment : LivePreferenceFragment<SettingsModel>() {

    @Inject
    lateinit var supportSettingsViewModelFactory: SupportSettingsViewModelFactory

    override val viewModel by viewModels<SettingsViewModel> {
        supportSettingsViewModelFactory.create(appComponent)
    }

    private val waitingDialog: UsageDialogFragment by lazy {
        CustomDialogBuilder(requireActivity())
            .setWaitingDialog()
            .build()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appComponent inject this
    }

    override fun onCreatePreferences(
        savedInstanceState: Bundle?, rootKey: String?,
    ) {
        setPreferencesFromResource(
            R.xml.settings_preferences, rootKey
        )
    }

    override fun basicActions() {
        setSettingsFragmentActions(
            requireView(), requireActivity(), viewModel
        )

        registrationOfInteraction(viewLifecycleOwner)
        viewModel.registrationOfInteraction(viewLifecycleOwner)
    }

    override fun registrationOfInteraction(lifecycleOwner: LifecycleOwner) =
        viewModel.liveDataState.observe(lifecycleOwner) { currentState ->
            when (currentState) {

                is SettingsModel.WaitingState ->
                    waitingDialog.show()

                is SettingsModel.MessageState -> {
                    waitingDialog.hide()
                    requireContext() showMessage currentState.messageId
                }

                is SettingsModel.RemoveCacheState ->
                    requireContext().run {
                        removeCacheApp()
                        this showMessage R.string.successClearCacheApp
                    }

                is SettingsModel.DeleteAccountState ->
                    (requireActivity() as? HostActivity)?.run {
                        waitingDialog.hide()
                        navController.navigateTo(R.id.launchFragment)
                        this showMessage R.string.msgSuccessRemoveAccount
                    }

                else -> return@observe
            }

            viewModel.actionСompleted()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<MaterialToolbar>(R.id.layoutBar).initViewToolBar()
        basicActions()
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().requireDrawerLayout()?.unlockDrawer()
    }
}