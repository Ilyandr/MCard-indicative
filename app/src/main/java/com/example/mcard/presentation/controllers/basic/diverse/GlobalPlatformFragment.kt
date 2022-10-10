package com.example.mcard.presentation.controllers.basic.diverse

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.paging.LoadState
import com.example.mcard.R
import com.example.mcard.databinding.FragmentGlobalCardsBinding
import com.example.mcard.domain.factories.other.PagerCardRepository
import com.example.mcard.domain.factories.viewModels.SupportGlobalPlatformViewModelFactory
import com.example.mcard.domain.viewModels.basic.diverse.GlobalPlatformViewModel
import com.example.mcard.presentation.adapters.CardsLoaderStateAdapter
import com.example.mcard.presentation.adapters.GlobalCardsAdapter
import com.example.mcard.presentation.adapters.options.configRecyclerCardViewChanged
import com.example.mcard.presentation.adapters.options.recyclerCardViewOptions
import com.example.mcard.presentation.support.setGlobalFragmentActions
import com.example.mcard.presentation.views.custom.CustomDialogBuilder
import com.example.mcard.presentation.views.other.setPaddingByBottomNavBar
import com.example.mcard.presentation.views.other.showMessage
import com.example.mcard.repository.features.TRANSACTION_KEY
import com.example.mcard.repository.features.connection.NetworkListener.Companion.statusNetwork
import com.example.mcard.repository.features.currentScreenPositionIsVertical
import com.example.mcard.repository.features.getModuleAppComponent
import com.example.mcard.repository.features.navigateTo
import com.example.mcard.repository.features.setVisible
import com.example.mcard.repository.source.architecture.view.StructView
import com.example.mcard.repository.source.usage.UsageDialogFragment
import javax.inject.Inject


internal class GlobalPlatformFragment : Fragment(), StructView {

    @Inject
    lateinit var viewModelFactory: SupportGlobalPlatformViewModelFactory

    @Inject
    lateinit var pagerCardRepository: PagerCardRepository

    private lateinit var viewBinding: FragmentGlobalCardsBinding

    private val viewModel by viewModels<GlobalPlatformViewModel> {
        viewModelFactory.create(
            pagerCardRepository
        )
    }

    private val dialogFaq: UsageDialogFragment by lazy {
        CustomDialogBuilder(requireActivity())
            .setTitle(R.string.infoGlobalTitle)
            .setMessage(R.string.messageGlobalFaq)
            .setPositiveAction { }
            .build()
    }

    private val cardsAdapter: GlobalCardsAdapter by lazy {
        viewBinding.content.recyclerView.recyclerCardViewOptions(
            GlobalCardsAdapter { data ->

                viewBinding.root.navigateTo(
                    R.id.launchGlobalCardFragment,
                    bundleOf(TRANSACTION_KEY to data)
                )
            }, false
        ).apply {
            withLoadStateHeaderAndFooter(
                header = CardsLoaderStateAdapter(),
                footer = CardsLoaderStateAdapter()
            )

            addLoadStateListener { combinedState ->
                (combinedState.refresh is LoadState.Loading).run {
                    viewBinding.content.progressBar setVisible this
                    viewBinding.content.recyclerView setVisible !this
                }

                if (combinedState.refresh is LoadState.Error && !statusNetwork) {
                    viewBinding.content.progressBar setVisible true
                    requireContext() showMessage R.string.offlineNetworkMSG
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().getModuleAppComponent() inject this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding =
            FragmentGlobalCardsBinding.inflate(layoutInflater)
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

        setConfigViewPadding()

        viewBinding.setGlobalFragmentActions(
            viewModel, dialogFaq, cardsAdapter
        )

        viewModel.listData.observe(viewLifecycleOwner) {
            cardsAdapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setConfigViewPadding()

        this.viewBinding.topBarLayout.setExpanded(true, true)
        viewBinding.content.recyclerView.configRecyclerCardViewChanged(false)
    }


    private fun setConfigViewPadding() =
        viewBinding.content.swipeLayout.setPaddingByBottomNavBar(
            requireContext().currentScreenPositionIsVertical()
        )

    override fun onResume() {
        super.onResume()
        viewBinding.content.recyclerView.configRecyclerCardViewChanged(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        cardsAdapter.removeOnPagesUpdatedListener { }
        cardsAdapter.removeLoadStateListener { }
    }
}