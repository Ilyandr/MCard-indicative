package com.example.mcard.presentation.controllers.authorization

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.example.mcard.R
import android.view.View
import android.view.animation.Animation
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.mcard.domain.models.authorization.RegistrationModel
import com.example.mcard.domain.viewModels.authorization.RegistrationViewModel
import com.example.mcard.presentation.support.setAuthorizationActions
import com.example.mcard.presentation.views.custom.CustomDialogBuilder
import com.example.mcard.repository.features.getModuleAppComponent
import com.example.mcard.repository.features.navigateTo
import com.example.mcard.repository.source.architecture.view.LiveFragment
import com.example.mcard.databinding.FragmentRegisterBinding
import com.example.mcard.presentation.views.other.showMessage
import com.example.mcard.repository.features.TRANSACTION_KEY
import com.example.mcard.repository.source.usage.UsageDialogFragment
import javax.inject.Inject
import javax.inject.Named

internal class RegistrationFragment : LiveFragment<RegistrationModel>() {

    override val viewBinding: FragmentRegisterBinding by viewBinding(
        createMethod = CreateMethod.INFLATE
    )

    override val viewModel: RegistrationViewModel by viewModels()

    @Inject
    @Named(value = "animationSelect")
    lateinit var animSelected: Animation

    private val isNotPrimaryAction: Boolean by lazy {
        arguments?.getBoolean(TRANSACTION_KEY) ?: false
    }

    private val customDialog: UsageDialogFragment by lazy {
        CustomDialogBuilder(
            requireContext()
        ).setWaitingDialog().build()
    }

    override fun onAttach(context: Context) {
        requireActivity()
            .getModuleAppComponent()
            .let {
                it.inject(this)
                it.inject(viewModel)
            }
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        basicActions()
        return this.viewBinding.root
    }

    override fun basicActions() {
        this.viewBinding.setAuthorizationActions(
            viewModel, this
        )

        viewModel.registrationOfInteraction(viewLifecycleOwner)
        registrationOfInteraction(viewLifecycleOwner)
    }

    override fun registrationOfInteraction(lifecycleOwner: LifecycleOwner) =
        this.viewModel.liveDataState.observe(lifecycleOwner) {
            when (it) {
                is RegistrationModel.LoadingState ->
                    customDialog.show()

                is RegistrationModel.FaultState -> {
                    requireContext() showMessage it.messageId
                    customDialog.hide()
                    viewModel.actionСompleted()
                }

                is RegistrationModel.SuccessState -> {
                    customDialog.hide()

                    if (isNotPrimaryAction)
                        requireActivity().onBackPressedDispatcher.onBackPressed()

                    else
                        viewBinding.root.navigateTo(
                            R.id.launchBasicFragmentFromRegisterFragment
                        )

                    viewModel.actionСompleted()
                }

                else -> return@observe
            }
        }
}