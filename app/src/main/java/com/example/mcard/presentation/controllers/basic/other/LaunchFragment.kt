package com.example.mcard.presentation.controllers.basic.other

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.mcard.domain.models.basic.other.LaunchModel
import com.example.mcard.domain.viewModels.basic.other.LaunchViewModel
import com.example.mcard.repository.source.architecture.view.LiveFragment
import com.example.mcard.databinding.FragmentStartAppBinding
import com.example.mcard.repository.features.changeVisibleBottomNavBar
import com.example.mcard.repository.features.navigateTo
import com.example.mcard.repository.features.requireDrawerLayout

internal class LaunchFragment : LiveFragment<LaunchModel>() {

    override val viewBinding: FragmentStartAppBinding by viewBinding(
        CreateMethod.INFLATE
    )

    override val viewModel: LaunchViewModel by viewModels()

    override fun onAttach(context: Context) {
        appComponent inject viewModel
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

        requireActivity().run {
            changeVisibleBottomNavBar(false)
            requireDrawerLayout()?.lockDrawer()
        }

        viewBinding.appCompatImageView2.start()
        registrationOfInteraction(viewLifecycleOwner)
        viewModel.launchControlAction()
    }

    override fun registrationOfInteraction(lifecycleOwner: LifecycleOwner) =
        viewModel.liveDataState.observe(lifecycleOwner) {
            when (it) {
                is LaunchModel.LaunchState ->
                    viewBinding.root.navigateTo(
                        it.navigationId
                    )

                else -> return@observe
            }
            viewModel.actionСompleted()
        }
}