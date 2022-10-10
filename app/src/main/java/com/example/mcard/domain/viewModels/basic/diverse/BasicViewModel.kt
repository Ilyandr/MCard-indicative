package com.example.mcard.domain.viewModels.basic.diverse

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.mcard.domain.models.basic.diverse.BasicModel
import com.example.mcard.presentation.views.observers.ObservableList
import com.example.mcard.repository.di.AppComponent
import com.example.mcard.repository.features.optionally.CardsSorting.DATE_SORT
import com.example.mcard.repository.features.optionally.CardsSorting.FREQUENCY_SORT
import com.example.mcard.repository.features.optionally.CardsSorting.GEO_SORT
import com.example.mcard.repository.features.optionally.CardsSorting.sortByABC
import com.example.mcard.repository.features.optionally.CardsSorting.sortByDate
import com.example.mcard.repository.features.optionally.CardsSorting.sortByFrequency
import com.example.mcard.repository.features.rest.firebase.AdditionAuthFirebase
import com.example.mcard.repository.features.rest.firebase.CaretakerCardsFirebase
import com.example.mcard.repository.models.storage.CardWithHistoryEntity
import com.example.mcard.repository.features.storage.database.source.QuestLocaleDataSource
import com.example.mcard.repository.features.storage.preferences.UserPreferences
import com.example.mcard.repository.features.SupportLiveData.default
import com.example.mcard.repository.features.SupportLiveData.set
import com.example.mcard.repository.source.architecture.viewModels.LiveViewModel
import com.example.mcard.repository.source.usage.EntranceUsageSource
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Named

internal class BasicViewModel(
    appComponent: AppComponent,
) : LiveViewModel<BasicModel>(), SwipeRefreshLayout.OnRefreshListener, EntranceUsageSource {

    @Inject
    @Named("localeObservableList")
    lateinit var observableCardList: ObservableList<CardWithHistoryEntity>

    @Inject
    lateinit var additionAuthFirebase: AdditionAuthFirebase

    @Inject
    lateinit var questLocaleDataSource: QuestLocaleDataSource

    @Inject
    lateinit var caretakerCardsFirebase: CaretakerCardsFirebase

    @Inject
    override lateinit var userPreferences: UserPreferences

    private val mutableLiveDataState =
        MutableLiveData<BasicModel>()
            .default(BasicModel.PrimaryInitState)

    override val liveDataState: LiveData<BasicModel> =
        this.mutableLiveDataState

    internal var currentScrollPostion = 0

    internal var viewModelCreate = false

    init {
        appComponent inject this
    }

    override fun registrationOfInteraction(lifecycleOwner: LifecycleOwner) {}

    fun actionCreate() {

        this.caretakerCardsFirebase.launchCaretaker(
            emptyListAction = {
                mutableLiveDataState.set(
                    BasicModel.EmptyDataState
                )
            }, updateListAction = ::refreshList
        )

        refreshList()
        actionСompleted()
    }

    private fun refreshList() {
        CoroutineScope(Dispatchers.Main).launch {
            observableCardList.addAll(
                withContext(
                    coroutineContext + Dispatchers.IO
                ) {
                    questLocaleDataSource.loadCards()
                }
            )
        }
    }

    override fun actionСompleted() {
        this.mutableLiveDataState.default(
            BasicModel.DefaultState
        )
    }

    fun refreshAction(startRefresh: Boolean) {
        this.mutableLiveDataState.set(
            BasicModel.RefreshState(
                startRefresh
            )
        )
    }

    override fun onRefresh() {
        MainScope().launch(Dispatchers.Main) {
            (withContext(
                coroutineContext + Dispatchers.IO
            ) {
                userPreferences.sortedCardInfo()
            }).apply {
                if (this == GEO_SORT) {
                    mutableLiveDataState.set(
                        BasicModel.SortingByGeolocationState
                    )

                    return@launch
                }

                mutableLiveDataState.set(
                    BasicModel.SortingListState(
                        if (this == FREQUENCY_SORT)
                            observableCardList.mutableList.sortByFrequency()
                        else if (this == DATE_SORT)
                            observableCardList.sortByDate()
                        else
                            observableCardList.sortByABC()
                    )
                )

                refreshAction(false)
            }
        }
    }
}