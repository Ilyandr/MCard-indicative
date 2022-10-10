package com.example.mcard.presentation.controllers.features

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.mcard.R
import com.example.mcard.databinding.AdditionallyFragmentBinding
import com.example.mcard.domain.factories.viewModels.SupportAdditionallyViewModelFactory
import com.example.mcard.domain.models.features.AdditionallyModel
import com.example.mcard.domain.viewModels.features.AdditionallyViewModel
import com.example.mcard.presentation.support.setAdditionallyFragmentActions
import com.example.mcard.presentation.views.custom.CustomDialogBuilder
import com.example.mcard.presentation.views.other.showMessage
import com.example.mcard.repository.features.requireDrawerLayout
import com.example.mcard.repository.source.architecture.view.LiveFragment
import javax.inject.Inject

internal class AdditionallyFragment : LiveFragment<AdditionallyModel>() {

    @Inject
    lateinit var viewModelFactory: SupportAdditionallyViewModelFactory

    override val viewModel by viewModels<AdditionallyViewModel> {
        viewModelFactory.create(appComponent)
    }

    override val viewBinding by viewBinding(
        viewBindingClass = AdditionallyFragmentBinding::class.java,
        createMethod = CreateMethod.INFLATE
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appComponent inject this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        basicActions()
        return this.viewBinding.root
    }

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        viewModel setPaymentController this
    }

    override fun basicActions() {

        viewBinding.setAdditionallyFragmentActions(
            requireActivity(), viewModel
        )

        registrationOfInteraction(viewLifecycleOwner)
        viewModel.registrationOfInteraction(viewLifecycleOwner)
    }

    override fun registrationOfInteraction(lifecycleOwner: LifecycleOwner) =
        viewModel.liveDataState.observe(lifecycleOwner) { currentState ->
            when (currentState) {

                is AdditionallyModel.FaultPaymentState ->
                    requireContext() showMessage R.string.unknownError

                is AdditionallyModel.SuccessPaymentState ->
                    CustomDialogBuilder(requireActivity())
                        .setTitle(R.string.paymentDialogTitle)
                        .setMessage(currentState.message)
                        .build()
                        .show()

                else -> return@observe
            }

            viewModel.actionСompleted()
        }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.viewDestroyed()
        requireActivity().requireDrawerLayout()?.unlockDrawer()
    }
}