package com.example.mcard.repository.source.other

import android.os.Parcelable
import android.view.animation.Animation
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.mcard.databinding.FragmentOpenBinding
import com.example.mcard.domain.models.basic.cards.CommonCardModel
import com.example.mcard.domain.factories.viewModels.SupportOpenCardViewModelFactory
import com.example.mcard.domain.viewModels.basic.cards.OpenCardViewModel
import com.example.mcard.presentation.views.custom.CustomDialogBuilder
import com.example.mcard.repository.source.architecture.view.LiveFragment
import com.example.mcard.repository.source.usage.UsageDialogFragment

internal abstract class ControllerCardSource<out UsageEntity : Parcelable> :
    LiveFragment<CommonCardModel>() {

    abstract var supportOpenCardViewModelFactory: SupportOpenCardViewModelFactory

    abstract val cardModel: UsageEntity?

    abstract var animationSelect: Animation

    override val viewModel: OpenCardViewModel by viewModels {
        supportOpenCardViewModelFactory.create(appComponent)
    }

    override val viewBinding: FragmentOpenBinding by viewBinding(
        createMethod = CreateMethod.INFLATE
    )

    val waitingDialog: UsageDialogFragment by lazy {
        CustomDialogBuilder(
            requireContext()
        ).setWaitingDialog().build()
    }
}