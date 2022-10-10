package com.example.mcard.presentation.controllers.authorization

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.mcard.domain.models.authorization.RestoreModel
import com.example.mcard.domain.viewModels.authorization.RestoreViewModel
import com.example.mcard.presentation.support.setRestoreActions
import com.example.mcard.presentation.views.custom.CustomDialogBuilder
import com.example.mcard.repository.source.architecture.view.LiveFragment
import com.example.mcard.databinding.FragmentRestoreBinding
import com.example.mcard.presentation.views.other.showMessage
import com.example.mcard.repository.source.usage.UsageDialogFragment
import javax.inject.Inject
import javax.inject.Named

internal class RestoreFragment : LiveFragment<RestoreModel>() {

    @Inject
    @Named(value = "animationSelect")
    lateinit var animationSelected: Animation

    override val viewModel: RestoreViewModel by viewModels()

    override val viewBinding: FragmentRestoreBinding by viewBinding(
        createMethod = CreateMethod.INFLATE
    )

    private val customDialog: UsageDialogFragment by lazy {
        CustomDialogBuilder(
            requireContext()
        ).setWaitingDialog().build()
    }

    override fun onAttach(context: Context) {
        appComponent.run {
            inject(this@RestoreFragment)
            inject(viewModel)
        }
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        basicActions()
        return this.viewBinding.root
    }

    override fun basicActions() {
        viewBinding.setRestoreActions(
            viewModel, this
        )
        registrationOfInteraction(viewLifecycleOwner)
        viewModel.registrationOfInteraction(requireActivity())
    }

    override fun registrationOfInteraction(lifecycleOwner: LifecycleOwner) =
        this.viewModel.liveDataState.observe(lifecycleOwner) {
            when (it) {
                is RestoreModel.LoadingState ->
                    customDialog.show()

                is RestoreModel.CompleteState -> {
                    requireContext() showMessage it.messageId
                    customDialog.hide()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                else -> return@observe
            }

            viewModel.actionСompleted()
        }
}