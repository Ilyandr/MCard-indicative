package com.example.mcard.presentation.controllers.basic.cards.open

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleOwner
import com.example.mcard.R
import com.example.mcard.domain.models.basic.cards.CommonCardModel
import com.example.mcard.domain.factories.viewModels.SupportOpenCardViewModelFactory
import com.example.mcard.presentation.support.setFragmentGlobaleCardActions
import com.example.mcard.presentation.views.custom.CustomDialogBuilder
import com.example.mcard.presentation.views.other.showMessage
import com.example.mcard.repository.features.TRANSACTION_KEY
import com.example.mcard.repository.features.extractParcelable
import com.example.mcard.repository.features.navigateTo
import com.example.mcard.repository.models.storage.CardEntity
import com.example.mcard.repository.source.other.ControllerCardSource
import com.example.mcard.repository.source.usage.UsageDialogFragment
import javax.inject.Inject
import javax.inject.Named

internal class GlobalCardFragment : ControllerCardSource<CardEntity>() {

    @Inject
    override lateinit var supportOpenCardViewModelFactory: SupportOpenCardViewModelFactory

    @Inject
    @Named(value = "animationSelect")
    override lateinit var animationSelect: Animation

    override val cardModel: CardEntity? by lazy {
        arguments extractParcelable TRANSACTION_KEY
    }

    private val dialogImportCard: UsageDialogFragment by lazy {
        CustomDialogBuilder(requireContext())
            .setTitle(R.string.firstDescriptionCardAdd)
            .setMessage(R.string.acceptCardAddMessage)
            .setNegativeAction { }
            .setPositiveAction {
                viewModel acceptImportAction cardModel
            }.build()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appComponent inject this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        basicActions()
        return viewBinding.root
    }

    override fun basicActions() {
        viewBinding.setFragmentGlobaleCardActions(
            requireActivity(),
            cardModel,
            animationSelect,
            dialogImportCard
        )

        viewModel.registrationOfInteraction(viewLifecycleOwner)
        registrationOfInteraction(viewLifecycleOwner)
    }

    override fun registrationOfInteraction(lifecycleOwner: LifecycleOwner) =
        viewModel.liveDataState.observe(lifecycleOwner) { currentState ->
            when (currentState) {
                is CommonCardModel.AcceptImportState ->
                    waitingDialog.show()

                is CommonCardModel.CompleteState -> {
                    waitingDialog.hide()
                    requireContext() showMessage R.string.successImportToast

                    viewBinding.root.navigateTo(
                        R.id.launchLocaleCardFragment,
                        bundleOf(TRANSACTION_KEY to currentState.saveData)
                    )
                }

                is CommonCardModel.FailState -> {
                    waitingDialog.hide()
                    requireContext() showMessage R.string.offlineNetworkMSG
                }

                else -> return@observe
            }
            viewModel.actionСompleted()
        }
}