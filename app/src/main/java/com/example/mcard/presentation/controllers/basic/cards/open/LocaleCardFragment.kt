package com.example.mcard.presentation.controllers.basic.cards.open

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.activity.result.ActivityResultLauncher
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.LifecycleOwner
import com.example.mcard.domain.models.basic.cards.CommonCardModel
import com.example.mcard.domain.factories.viewModels.SupportOpenCardViewModelFactory
import com.example.mcard.domain.viewModels.basic.cards.OpenCardViewModel.Companion.BARCODE_ACTION_RESULT
import com.example.mcard.domain.viewModels.basic.cards.OpenCardViewModel.Companion.REQUEST_BARCODE_ACTION
import com.example.mcard.presentation.adapters.StoriesAdapter
import com.example.mcard.presentation.support.setFragmentLocaleCardActions
import com.example.mcard.R
import com.example.mcard.repository.models.storage.CardWithHistoryEntity
import com.example.mcard.repository.models.storage.BarcodeModel
import com.example.mcard.presentation.views.other.createOptionsExtensionDialog
import com.example.mcard.presentation.views.other.showMessage
import com.example.mcard.repository.features.*
import com.example.mcard.repository.features.navigateTo
import com.example.mcard.repository.features.utils.DesignCardManager.changeCardBarcode
import com.example.mcard.repository.features.utils.DesignCardManager.staticLoadCardImage
import com.example.mcard.repository.features.utils.DesignCardManager.removeCardCache
import com.example.mcard.repository.source.other.ControllerCardSource
import com.google.android.material.bottomsheet.BottomSheetDialog
import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog
import javax.inject.Inject
import javax.inject.Named

internal class LocaleCardFragment : ControllerCardSource<CardWithHistoryEntity>() {

    @Inject
    override lateinit var supportOpenCardViewModelFactory: SupportOpenCardViewModelFactory

    @Inject
    lateinit var colorPickerDialog: ColorPickerDialog

    @Inject
    @Named(value = "animationSelect")
    override lateinit var animationSelect: Animation

    @Inject
    lateinit var storiesAdapter: StoriesAdapter

    private val optionsDialog: BottomSheetDialog by lazy {
        viewBinding.root.createOptionsExtensionDialog(
            viewModel, cardModel
        ) {
            imageFromGalleryLauncher.launch("image/*")
        }
    }

    private val imageFromGalleryLauncher: ActivityResultLauncher<String> =
        requireImageFromGalleryResult { path ->
            viewModel.imageAction(
                requireContext(),
                path,
                cardModel?.cardEntity?.name
            )
        }

    override val cardModel: CardWithHistoryEntity? by lazy {
        arguments extractParcelable TRANSACTION_KEY
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appComponent inject this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initResultListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        basicActions()
        return viewBinding.root
    }

    override fun basicActions() {
        viewBinding.setFragmentLocaleCardActions(
            this,
            cardModel,
            animationSelect,
            storiesAdapter,
            viewModel,
            colorPickerDialog,
            optionsDialog
        )

        registrationOfInteraction(viewLifecycleOwner)
        viewModel registrationOfInteraction viewLifecycleOwner
    }

    private fun initResultListener() {
        setFragmentResultListener(REQUEST_BARCODE_ACTION) { key, bundle ->
            if (key == REQUEST_BARCODE_ACTION)
                bundle.extractParcelable<BarcodeModel>(
                    BARCODE_ACTION_RESULT
                ).apply {
                    viewModel.barcodeReadyChanged(
                        cardModel, this.toString()
                    )
                }
        }
    }

    override fun registrationOfInteraction(lifecycleOwner: LifecycleOwner) =
        viewModel.liveDataState.observe(lifecycleOwner) {
            when (it) {
                is CommonCardModel.WriteHistoryState -> {
                    storiesAdapter submitList cardModel!!.usageHistory
                    requireContext() showMessage R.string.msgSuccessWriteHistory
                    viewModel.actionСompleted()
                }

                is CommonCardModel.ChangeBarcodeState -> {
                    viewBinding.root.navigateTo(
                        R.id.launchCreateCardFragmentWithDataResult,
                        bundleOf(
                            REQUEST_BARCODE_ACTION to true
                        )
                    )
                    viewModel.actionСompleted()
                }

                is CommonCardModel.FinallyChangeBarcodeState -> {
                    viewBinding.barcodeContainer changeCardBarcode it.data
                    requireContext() showMessage R.string.infoSuccessEditBarcode
                }

                is CommonCardModel.ChangeImageState -> {
                    cardModel?.cardEntity?.apply {

                        viewBinding.includeCardForm.cardDesign.staticLoadCardImage(
                            this, false
                        )
                    }
                    viewModel.actionСompleted()
                }

                is CommonCardModel.RemoveState -> {
                    requireContext() removeCardCache
                            cardModel?.cardEntity?.name

                    waitingDialog.hide()
                    requireContext() showMessage R.string.msgLocaleRemoveCard

                    viewModel.actionСompleted()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                is CommonCardModel.LoadingState ->
                    waitingDialog.show()

                is CommonCardModel.MessageState -> {
                    waitingDialog.hide()
                    requireContext() showMessage it.messageId
                    viewModel.actionСompleted()
                }

                else -> return@observe
            }
        }
}