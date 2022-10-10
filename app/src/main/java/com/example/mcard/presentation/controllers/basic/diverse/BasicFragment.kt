package com.example.mcard.presentation.controllers.basic.diverse

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.mcard.R
import com.example.mcard.domain.models.basic.diverse.BasicModel
import com.example.mcard.domain.factories.viewModels.SupportBasicViewModelFactory
import com.example.mcard.domain.viewModels.basic.diverse.BasicViewModel
import com.example.mcard.presentation.support.setBasicFragmentActions
import com.example.mcard.presentation.views.other.setPaddingByBottomNavBar
import com.example.mcard.repository.features.location.dataSource.LocationListenerDataSourse
import com.example.mcard.repository.source.architecture.view.LiveFragment
import com.example.mcard.databinding.BasicFragmentCoordinatorBinding
import com.example.mcard.presentation.adapters.LocaleCardsAdapter
import com.example.mcard.presentation.adapters.options.configRecyclerCardViewChanged
import com.example.mcard.presentation.adapters.options.recyclerCardViewOptions
import com.example.mcard.presentation.views.other.createSortingExtensionDialog
import com.example.mcard.presentation.views.other.showMessage
import com.example.mcard.repository.features.TRANSACTION_KEY
import com.example.mcard.repository.features.navigateTo
import com.example.mcard.repository.features.setVisible
import com.example.mcard.repository.features.storage.preferences.UserPreferences
import com.example.mcard.repository.source.usage.EntranceUsageSource
import com.google.android.material.bottomsheet.BottomSheetDialog
import javax.inject.Inject

internal class BasicFragment : LiveFragment<BasicModel>(), EntranceUsageSource {

    @Inject
    lateinit var basicViewModelFactory: SupportBasicViewModelFactory

    @Inject
    override lateinit var userPreferences: UserPreferences

    override lateinit var viewBinding: BasicFragmentCoordinatorBinding

    override val viewModel: BasicViewModel by viewModels {
        this.basicViewModelFactory.create(
            appComponent
        )
    }

    private val sortingDialog: BottomSheetDialog by lazy {
        viewBinding.root.createSortingExtensionDialog(
            viewModel, viewModel::onRefresh
        )
    }

    private val cardsAdapter: LocaleCardsAdapter by lazy {
        viewBinding.content.recyclerView.recyclerCardViewOptions(
            LocaleCardsAdapter { postiton, data ->
                viewModel.currentScrollPostion = postiton

                viewBinding.root.navigateTo(
                    R.id.launchOpenFragment,
                    bundleOf(TRANSACTION_KEY to data)
                )
            }, true
        ).apply {
            viewModel.observableCardList.run {

                setActionChange { externalData ->

                    externalData.isEmpty().run {
                        viewBinding.content.emptyImage setVisible this
                        viewBinding.content.emptyView setVisible this
                    }

                    this@apply setСomposedData externalData
                }

                setLocationChange {
                    this@apply setСomposedData it
                }
            }
        }
    }

    private lateinit var secureFacilityListener:
            LocationListenerDataSourse.SecureFacilityListener

    override fun onAttach(context: Context) {
        appComponent inject this
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = BasicFragmentCoordinatorBinding.inflate(layoutInflater)
        viewBinding.content.recyclerView.adapter = cardsAdapter

        this.secureFacilityListener =
            LocationListenerDataSourse(
                this.requireContext(),
            ) {
                requireContext() showMessage it
                viewModel.refreshAction(false)
            } requireSecureFacilityListener this

        viewModel.actionCreate()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        basicActions()
        return viewBinding.root
    }

    override fun basicActions() {

        viewBinding.setBasicFragmentActions(
            requireActivity(),
            viewLifecycleOwner,
            viewModel,
            sortingDialog,
        )

        viewBinding.content.swipeLayout.setPaddingByBottomNavBar()
        registrationOfInteraction(this.viewLifecycleOwner)
        viewModel.registrationOfInteraction(viewLifecycleOwner)
    }

    override fun registrationOfInteraction(lifecycleOwner: LifecycleOwner) =
        this.viewModel.liveDataState.observe(lifecycleOwner) {
            when (it) {
                is BasicModel.RefreshState ->
                    viewBinding.content.swipeLayout.isRefreshing =
                        it.isRefreshing

                is BasicModel.SortingByGeolocationState ->
                    if (::secureFacilityListener.isInitialized) {
                        cardsAdapter setСomposedData viewModel.observableCardList
                        viewBinding.content.swipeLayout.isRefreshing = true
                        secureFacilityListener.launch()
                    }

                is BasicModel.EmptyDataState -> {
                    viewBinding.content.apply {
                        emptyView setVisible true
                        emptyImage setVisible true
                        recyclerView setVisible true
                        progressBar setVisible false
                        cardsAdapter.removeAllData()
                        updateManagerList()
                    }
                }

                is BasicModel.SortingListState ->
                    cardsAdapter.setСomposedData(it.list)

                else -> return@observe
            }
            this.viewModel.actionСompleted()
        }

    fun updateManagerList() {
        this.viewBinding.topBarLayout.setExpanded(true, true)

        viewBinding.content.recyclerView.configRecyclerCardViewChanged(
            true, viewModel.currentScrollPostion
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        viewBinding.content.swipeLayout.setPaddingByBottomNavBar()
        updateManagerList()
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launchWhenStarted {
            updateManagerList()
            cardsAdapter.notifyDataSetChanged()
        }
    }
}