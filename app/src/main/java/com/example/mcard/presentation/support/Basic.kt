package com.example.mcard.presentation.support

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.animation.Animation
import android.view.inputmethod.EditorInfo
import androidx.activity.addCallback
import androidx.core.view.doOnLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.ui.setupWithNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.mcard.R
import com.example.mcard.databinding.*
import com.example.mcard.domain.viewModels.basic.cards.OpenCardViewModel
import com.example.mcard.domain.viewModels.basic.cards.adding.AutoCardAddViewModel
import com.example.mcard.domain.viewModels.basic.cards.adding.ManuallyCardAddViewModel
import com.example.mcard.domain.viewModels.basic.diverse.GlobalPlatformViewModel
import com.example.mcard.presentation.adapters.GlobalCardsAdapter
import com.example.mcard.presentation.adapters.StoriesAdapter
import com.example.mcard.presentation.adapters.options.recyclerCardViewOptions
import com.example.mcard.presentation.controllers.HostActivity
import com.example.mcard.presentation.controllers.basic.cards.adding.AutoCardAddFragment
import com.example.mcard.presentation.controllers.basic.diverse.DrawerController
import com.example.mcard.presentation.views.custom.CustomDialogBuilder
import com.example.mcard.presentation.views.other.*
import com.example.mcard.presentation.views.other.initViewToolBar
import com.example.mcard.presentation.views.other.setCardDataChangeListener
import com.example.mcard.presentation.views.other.setPaddingByBottomNavBar
import com.example.mcard.presentation.views.other.showMessage
import com.example.mcard.repository.features.*
import com.example.mcard.repository.features.optionally.TextChangedListener
import com.example.mcard.repository.features.utils.DesignCardManager
import com.example.mcard.repository.features.utils.DesignCardManager.staticLoadCardImage
import com.example.mcard.repository.features.utils.DesignCardManager.requireCardImageFile
import com.example.mcard.repository.models.storage.CardEntity
import com.example.mcard.repository.models.storage.CardWithHistoryEntity
import com.example.mcard.repository.source.architecture.view.LiveFragment
import com.example.mcard.repository.source.usage.EntranceUsageSource
import com.example.mcard.repository.source.usage.ProcessorSource
import com.example.mcard.repository.source.usage.UsageDialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.Lazy
import jp.wasabeef.blurry.Blurry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog

internal fun BasicFragmentCoordinatorBinding.setBasicFragmentActions(
    fragmentActivity: FragmentActivity,
    lifecycleOwner: LifecycleOwner,
    refreshListener: SwipeRefreshLayout.OnRefreshListener,
    bottomSheetSortingDialog: BottomSheetDialog,
) {
    fragmentActivity.onBackPressedDispatcher
        .addCallback(lifecycleOwner) {
            fragmentActivity.finish()
        }

    fragmentActivity changeVisibleBottomNavBar true
    content.swipeLayout.setPaddingByBottomNavBar()

    fragmentActivity.requireDrawerLayout()?.apply {
        this.unlockDrawer()

        topAppBar.setNavigationOnClickListener {
            this.show()
        }
    }

    this.topAppBar.setOnMenuItemClickListener {
        if (it.itemId == R.id.sortTypeItem)
            MainScope().launch(Dispatchers.Main) {
                bottomSheetSortingDialog.show()
            }
        true
    }

    this.content.swipeLayout.setOnRefreshListener(refreshListener)
}

internal fun FragmentAutoCardAddBinding.setAutoCardAddFragmentActions(
    fragment: AutoCardAddFragment,
    viewModel: AutoCardAddViewModel,
    externalSingleAction: Boolean,
    processorController: ProcessorSource,
): Job {
    fragment.requireActivity() changeVisibleBottomNavBar true

    if (externalSingleAction) {
        additionalDataCardBtn.visibility = View.INVISIBLE
        manuallyCardAddBtn.visibility = View.INVISIBLE

        description.firstDescriptionCardAdd.setText(
            R.string.secondDescriptionAutoCardAdd
        )

        description.secondDescriptionCardAdd.setText(
            R.string.secondDescriptionCardAddBySingle
        )

        dataStatusView.setText(
            R.string.dataStatusViewThirdDescription
        )

    } else {
        additionalDataCardBtn.setOnClickListener {
            processorController.barcodeAvailability =
                !processorController.barcodeAvailability

            additionalDataCardBtn.setHint(
                if (processorController.barcodeAvailability)
                    R.string.additionalDataCardBtnOffText
                else
                    R.string.additionalDataCardBtnOnText
            )
        }

        manuallyCardAddBtn.setOnClickListener {
            viewModel.saveAddingCard(null)
        }
    }

    fragment.checkPermissions(faultState = {

        fragment.requireContext() showMessage
                if (!externalSingleAction)
                    R.string.permissionToastWarning
                else
                    R.string.permissionError

        if (externalSingleAction)
            fragment.requireActivity()
                .onBackPressedDispatcher.onBackPressed()
        else
            viewModel saveAddingCard null
    }) {
        processorController.updateHolder(
            cameraPreview
        )
        fragment.startStream()
    }.run {

        return CoroutineScope(Dispatchers.IO).launch {

            delay(3000)

            withContext(Dispatchers.Main) {

                launch(viewModel.getArrayPermission())
            }
        }
    }
}

internal fun FragmentManuallyCardAddBinding.setManuallyCardAddFragmentActions(
    fragment: LiveFragment<*>,
    colorPickerDialog: Lazy<ColorPickerDialog>,
) {
    fragment.requireActivity() changeVisibleBottomNavBar true

    (fragment.viewModel as? ManuallyCardAddViewModel)?.apply {
        this.dataExtractionAction(
            manuallyInputData, fragment.arguments
        ).let { cardEntity ->

            fragmentTopBar.toolbarView.initViewToolBar(
                R.menu.manually_card_add_menu,
                Pair(R.id.btnComplete) {
                    this.cardAddAction(
                        CardEntity(
                            number = manuallyInputData.inputNumber.text.toString(),
                            name = manuallyInputData.inputName.text.toString(),
                            dateAddCard = getTimeNow(),
                            barcode = cardEntity?.barcode,
                            color = manuallyInputData.inputColor.imageTintList?.defaultColor,
                            uniqueIdentifier = setUniqueIdentifier()
                        )
                    ) { messageId ->
                        fragment.requireActivity() showMessage messageId
                    }
                }
            )

            manuallyInputData.inputColor.setOnClickListener {

                colorPickerDialog.get().run {
                    withListener { _, color ->
                        manuallyInputData.inputColor.imageTintList =
                            ColorStateList.valueOf(
                                color
                            )
                    }

                    show(
                        fragment.childFragmentManager, ""
                    )
                }
            }
        }
    }
}

internal fun HostActivityBinding.setHostActivityActions(
    entranceUsageSource: EntranceUsageSource,
    navController: NavController,
    drawerView: DrawerController,
) {
    navController.apply {
        content.bottomNavBar.setupWithNavController(this)
        content.bottomNavBar.background = null
    }

    root.addDrawerListener(drawerView)
    this.drawerContent.root.setNavigationItemSelectedListener(drawerView)
    drawerView.lockDrawer()

    content.bottomNavBar.setOnItemSelectedListener {

        navController.backQueue.run {
            if (size > 1) {
                if (get(size - 2).destination.id == it.itemId) {
                    navController.popBackStack()
                    return@run
                }
            }

            content.fragmentContainerView.navigateTo(
                when (it.itemId) {
                    R.id.basicFragment ->
                        R.id.basicFragment

                    R.id.createCardFragment ->
                        R.id.createCardFragment

                    R.id.globalPlatformFragment -> {

                        if (!entranceUsageSource.checkOfflineEntrance())
                            entranceUsageSource showOfferDialogRegistration navController

                        R.id.globalPlatformFragment
                    }

                    else ->
                        return@setOnItemSelectedListener false
                }
            )
        }

        return@setOnItemSelectedListener true
    }

    content.bottomNavBar.setOnItemReselectedListener reselectedListener@{

    }
}

internal fun FragmentOpenBinding.setFragmentLocaleCardActions(
    liveFragment: LiveFragment<*>,
    dataCard: CardWithHistoryEntity?,
    animation: Animation,
    storiesAdapter: StoriesAdapter,
    viewModel: OpenCardViewModel,
    colorPickerDialog: ColorPickerDialog,
    sheetDialog: BottomSheetDialog,
) {
    liveFragment.requireActivity() changeVisibleBottomNavBar true

    layoutTopBar.toolbarView.initViewToolBar(
        R.menu.top_bar_open_card_fragment,
        R.id.settingsCardItem to sheetDialog::show
    )

    liveFragment.requireActivity().run {

        onBackPressedDispatcher
            .addCallback(liveFragment) {
                (this@run as? HostActivity)
                    ?.actionBackStackCurrentFragment()
            }

        if (requireCardImageFile(
                dataCard?.apply {
                    DesignCardManager.manageCardData(
                        includeDataCard,
                        barcodeContainer,
                        cardEntity
                    )

                    layoutTopBar.toolbarView.title = cardEntity.name

                    includeCardForm.cardDesign.staticLoadCardImage(
                        cardEntity, false
                    )
                }?.cardEntity?.name!!
            ).exists()
        ) {
            includeDataCard.colorTableRow.visibility =
                View.GONE
            includeDataCard.colorTableRowDelimeter.visibility =
                View.GONE
        }

        DesignCardManager.setCardSize(
            includeCardForm.root
        )

        DesignCardManager.setCardSize(
            barcodeContainer.root,
            scaleSizePercent = 0.5f,
            withItemParams = true
        )

        historyContainer.storiesRecyclerView.recyclerCardViewOptions(
            storiesAdapter, null
        )

        storiesAdapter.submitList(dataCard.usageHistory)

        historyContainer.createNewHistoryBtn.setOnClickListener {
            it.startAnimation(animation)

            CustomDialogBuilder(this)
                .setTitle(R.string.titleDialogHistory)
                .setMessage(R.string.msgDialogProposalWriteHistory)
                .setNegativeAction { }
                .setPositiveAction {
                    viewModel.saveNewHistoryUsage(
                        dataCard
                    )
                }.build().show()
        }

        includeDataCard.apply {

            colorPickerDialog.withListener { _, color ->
                inputColor.imageTintList =
                    ColorStateList.valueOf(color)

                viewModel.changeInformation(
                    inputColor.id, dataCard, color.toString()
                )?.apply {
                    includeCardForm.cardDesign.staticLoadCardImage(
                        cardEntity, false
                    )
                }
            }

            inputColor.setOnClickListener {
                colorPickerDialog.show(
                    liveFragment.childFragmentManager, ""
                )
            }

            inputName.setCardDataChangeListener(
                confirmEditNameView,
                dataCard,
                layoutTopBar.toolbarView::setTitle,
                viewModel::changeInformation
            )

            inputNumber.setCardDataChangeListener(
                confirmEditNumberView,
                dataCard,
                action = viewModel::changeInformation
            )
        }
    }
}

internal fun FragmentOpenBinding.setFragmentGlobaleCardActions(
    fragmentActivity: FragmentActivity,
    dataCard: CardEntity?,
    clickAnimation: Animation,
    acceptImportDialog: UsageDialogFragment,
) {
    fragmentActivity changeVisibleBottomNavBar true
    layoutTopBar.toolbarView.initViewToolBar()

    if (fragmentActivity.requireCardImageFile(
            dataCard?.apply {
                DesignCardManager.manageCardData(
                    includeDataCard,
                    barcodeContainer,
                    this
                )

                layoutTopBar.toolbarView.title = name

                includeCardForm.cardDesign.staticLoadCardImage(
                    this, false
                )
            }?.name!!
        ).exists()
    ) {
        includeDataCard.colorTableRow.visibility =
            View.GONE
        includeDataCard.colorTableRowDelimeter.visibility =
            View.GONE
    }

    includeDataCard.run {
        inputColor.isEnabled = false
        inputName.isEnabled = false
        inputNumber.isEnabled = false
    }

    DesignCardManager.setCardSize(
        includeCardForm.root
    )

    historyContainer.root setVisible false
    importButton setVisible true

    importButton.setOnClickListener {
        it.startAnimation(clickAnimation)
        acceptImportDialog.show()
    }

    fragmentActivity.lifecycleScope.launch(
        Dispatchers.Main
    ) {
        cardDataView.doOnLayout {
            Blurry.with(fragmentActivity)
                .radius(24)
                .sampling(2)
                .color(Color.parseColor("#80202020"))
                .onto(cardDataView)
        }
    }
}

@SuppressLint("NotifyDataSetChanged")
internal fun FragmentGlobalCardsBinding.setGlobalFragmentActions(
    viewModel: GlobalPlatformViewModel,
    dialogFaq: UsageDialogFragment,
    adapter: GlobalCardsAdapter,
) {
    (root.context as? FragmentActivity)?.changeVisibleBottomNavBar(true)

    content.emptyImage setVisible false
    content.emptyView setVisible false

    topAppBar.initViewToolBar(
        null,
        R.id.infoItem to dialogFaq::show,
        R.id.searchItem to {
            searchBarContainer.inputView setKeyboardFocus true
            searchBarContainer.root setVisible true
            topBarLayout setVisible false
        }
    )

    searchBarContainer.inputView.run {

        addTextChangedListener(object : TextChangedListener {
            override fun onTextChanged(data: String) {
                viewModel actionSearch data
            }
        })

        setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel actionSearch view.text.toString()
                false
            } else
                true
        }

        searchBarContainer.backButton.setOnClickListener {
            content.recyclerView.removeAllViews()
            text = null
            this setKeyboardFocus false

            searchBarContainer.root setVisible false
            topBarLayout setVisible true
            adapter.notifyDataSetChanged()
        }
    }

    content.run {
        swipeLayout.isEnabled = false
    }
}