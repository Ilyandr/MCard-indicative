package com.example.mcard.presentation.controllers.basic.cards.adding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.mcard.domain.models.basic.cards.adding.ManuallyCardAddModel
import com.example.mcard.domain.factories.viewModels.SupportManuallyCardAddViewModelFactory
import com.example.mcard.domain.viewModels.basic.cards.adding.ManuallyCardAddViewModel
import com.example.mcard.presentation.support.setManuallyCardAddFragmentActions
import com.example.mcard.presentation.views.custom.CustomDialogBuilder
import com.example.mcard.R
import com.example.mcard.repository.features.navigateTo
import com.example.mcard.repository.source.architecture.view.LiveFragment
import com.example.mcard.databinding.FragmentManuallyCardAddBinding
import com.example.mcard.repository.source.usage.UsageDialogFragment
import dagger.Lazy
import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog
import javax.inject.Inject

internal class ManuallyCardAddFragment : LiveFragment<ManuallyCardAddModel>() {

    @Inject
    lateinit var supportManuallyCardAddModel: SupportManuallyCardAddViewModelFactory

    @Inject
    lateinit var colorPickerDialog: Lazy<ColorPickerDialog>

    override val viewBinding: FragmentManuallyCardAddBinding by viewBinding(
        createMethod = CreateMethod.INFLATE
    )

    override val viewModel: ManuallyCardAddViewModel by viewModels {
        this.supportManuallyCardAddModel.create(
            appComponent
        )
    }

    private val customDialog: UsageDialogFragment by lazy {
        CustomDialogBuilder(
            requireContext()
        ).setWaitingDialog().build()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appComponent inject this
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        basicActions()
        return viewBinding.root
    }

    override fun basicActions() {
        viewBinding.setManuallyCardAddFragmentActions(
            this, colorPickerDialog
        )
        registrationOfInteraction(viewLifecycleOwner)
        viewModel.registrationOfInteraction(viewLifecycleOwner)
    }

    override fun registrationOfInteraction(lifecycleOwner: LifecycleOwner) =
        viewModel.liveDataState.observe(lifecycleOwner) {
            when (it) {
                is ManuallyCardAddModel.SavingDataState ->
                    customDialog.show()

                is ManuallyCardAddModel.SavedDataState -> {
                    customDialog.hide()

                    viewBinding.root.navigateTo(
                        R.id.launchBasicFragment
                    )

                    viewModel.actionСompleted()
                }

                else -> return@observe
            }
        }
}