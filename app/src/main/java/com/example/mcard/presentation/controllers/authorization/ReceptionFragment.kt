package com.example.mcard.presentation.controllers.authorization

import android.os.Bundle
import com.example.mcard.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.mcard.domain.models.authorization.ReceptionModel
import com.example.mcard.domain.viewModels.authorization.ReceptionViewModel
import com.example.mcard.presentation.support.setRegistrationActions
import com.example.mcard.presentation.views.custom.CustomDialogBuilder
import com.example.mcard.repository.features.navigateTo
import com.example.mcard.repository.source.architecture.view.LiveFragment
import com.example.mcard.databinding.ActivityReceptionBinding
import com.example.mcard.presentation.views.other.showMessage
import com.example.mcard.repository.source.usage.UsageDialogFragment
import javax.inject.Inject
import javax.inject.Named

internal class ReceptionFragment : LiveFragment<ReceptionModel>() {

    @Inject
    @Named(value = "animationSelect")
    lateinit var animSelected: Animation

    override val viewModel: ReceptionViewModel by viewModels()

    override val viewBinding: ActivityReceptionBinding by viewBinding(
        createMethod = CreateMethod.INFLATE
    )

    private val customDialog: UsageDialogFragment by lazy {
        CustomDialogBuilder(
            requireContext()
        ).setWaitingDialog().build()
    }

    override fun onAttach(context: Context) {
        appComponent.let {
            it.inject(this)
            it.inject(viewModel)
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
        viewBinding.setRegistrationActions(
            this, viewModel
        )
        registrationOfInteraction(viewLifecycleOwner)
        viewModel.registrationOfInteraction(viewLifecycleOwner)
    }

    override fun registrationOfInteraction(lifecycleOwner: LifecycleOwner) =
        this.viewModel.liveDataState.observe(lifecycleOwner) {
            when (it) {
                is ReceptionModel.LoadingState ->
                    customDialog.show()

                is ReceptionModel.FaultState -> {
                    customDialog.hide()
                    requireContext() showMessage it.messageId
                    viewModel.actionСompleted()
                }

                is ReceptionModel.SuccessState -> {
                    customDialog.hide()

                    viewBinding.root.navigateTo(
                        R.id.launchBasicFragmentFromReceptionFragment
                    )

                    viewModel.actionСompleted()
                }

                is ReceptionModel.NotAuthorizedState -> {
                    requireContext() showMessage R.string.infoSuccessEntrance
                    viewBinding.root.navigateTo(R.id.launchBasicFragmentFromReceptionFragment)
                    viewModel.actionСompleted()
                }

                else -> return@observe
            }
        }
}