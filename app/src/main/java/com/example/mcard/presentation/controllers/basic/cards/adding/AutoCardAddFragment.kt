package com.example.mcard.presentation.controllers.basic.cards.adding

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.mcard.domain.models.basic.cards.adding.AutoCardAddingModel
import com.example.mcard.domain.viewModels.basic.cards.adding.AutoCardAddViewModel
import com.example.mcard.domain.viewModels.basic.cards.OpenCardViewModel.Companion.BARCODE_ACTION_RESULT
import com.example.mcard.domain.viewModels.basic.cards.OpenCardViewModel.Companion.REQUEST_BARCODE_ACTION
import com.example.mcard.presentation.support.setAutoCardAddFragmentActions
import com.example.mcard.R
import com.example.mcard.repository.features.camera.ProcessorDataSource
import com.example.mcard.repository.features.TRANSACTION_KEY
import com.example.mcard.repository.features.navigateTo
import com.example.mcard.repository.source.usage.CameraUsageSource
import com.example.mcard.repository.source.architecture.view.LiveFragment
import com.example.mcard.repository.source.usage.ProcessorSource
import com.example.mcard.databinding.FragmentAutoCardAddBinding
import com.example.mcard.repository.features.setVisible
import kotlinx.coroutines.Job

internal class AutoCardAddFragment : LiveFragment<AutoCardAddingModel>(), CameraUsageSource {

    override val viewModel: AutoCardAddViewModel by viewModels()

    override val viewBinding: FragmentAutoCardAddBinding by viewBinding(
        createMethod = CreateMethod.INFLATE
    )

    private val processorControllerDataSource: ProcessorSource by lazy {
        ProcessorDataSource(viewBinding.cameraPreview) {
            viewModel saveAddingCard it
        }
    }

    private val externalSingleAction: Boolean by lazy {
        arguments?.getBoolean(REQUEST_BARCODE_ACTION) ?: false
    }

    private var cameraJobStarter: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        basicActions()
        return viewBinding.root
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun basicActions() {
        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        registrationOfInteraction(viewLifecycleOwner)

        this.cameraJobStarter =
            viewBinding.setAutoCardAddFragmentActions(
                this,
                viewModel,
                externalSingleAction,
                processorControllerDataSource
            )
    }

    override fun registrationOfInteraction(lifecycleOwner: LifecycleOwner) =
        this.viewModel.liveDataState.observe(lifecycleOwner) { currentState ->
            when (currentState) {
                is AutoCardAddingModel.DefaultState -> {
                    viewBinding.cardView setVisible false
                    viewBinding.dataStatusView setVisible false
                    viewBinding.description.root setVisible true
                }

                is AutoCardAddingModel.FinalAddingState -> {
                    stopStream()
                    viewModel.actionСompleted()

                    viewBinding.root.navigateTo(
                        R.id.launchManuallyCardAdd,
                        bundleOf(TRANSACTION_KEY to currentState.cardEntity)
                    )
                }

                is AutoCardAddingModel.ExternalUseBarcode -> {
                    setFragmentResult(
                        REQUEST_BARCODE_ACTION,
                        bundleOf(
                            BARCODE_ACTION_RESULT to currentState.barcodeModel
                        )
                    )
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                is AutoCardAddingModel.ProcessState -> {
                    viewBinding.description.root setVisible false
                    viewBinding.dataStatusView setVisible true
                    viewBinding.cardView setVisible true
                }

                else -> return@observe
            }
        }

    override fun stopStream() {
        viewBinding.cameraPreview.holder.apply {
            this.surface.release()
            processorControllerDataSource.surfaceDestroyed(this)
        }
    }

    override fun startStream() {
        viewModel.launchProcessAddingState(
            externalSingleAction,
            processorControllerDataSource
        ) { textId ->
            viewBinding.dataStatusView.setText(
                textId
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraJobStarter?.cancel()
        viewModel.actionСompleted()

        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_FULL_USER
    }
}