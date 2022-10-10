package com.example.mcard.domain.viewModels.basic.cards.adding

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mcard.R
import com.example.mcard.databinding.ManuallyCardAddInputDataBinding
import com.example.mcard.domain.models.basic.cards.adding.ManuallyCardAddModel
import com.example.mcard.presentation.views.observers.ObservableList
import com.example.mcard.repository.di.AppComponent
import com.example.mcard.repository.features.SupportLiveData.default
import com.example.mcard.repository.features.SupportLiveData.set
import com.example.mcard.repository.features.TRANSACTION_KEY
import com.example.mcard.repository.features.extractParcelable
import com.example.mcard.repository.features.messageAction
import com.example.mcard.repository.features.rest.firebase.CaretakerCardsFirebase
import com.example.mcard.repository.features.rest.firebase.PersonalFirebase
import com.example.mcard.repository.features.storage.database.source.QuestLocaleDataSource
import com.example.mcard.repository.features.storage.preferences.UserPreferences
import com.example.mcard.repository.models.storage.CardEntity
import com.example.mcard.repository.models.storage.CardWithHistoryEntity
import com.example.mcard.repository.source.architecture.viewModels.LiveViewModel
import com.example.mcard.repository.source.usage.EntranceUsageSource
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Named

internal class ManuallyCardAddViewModel(
    appComponent: AppComponent,
) : LiveViewModel<ManuallyCardAddModel>(), EntranceUsageSource {

    @Inject
    lateinit var caretakerCardsFirebase: CaretakerCardsFirebase

    @Inject
    lateinit var questLocaleDataSource: QuestLocaleDataSource

    @Inject
    lateinit var personalFirebase: PersonalFirebase

    @Inject
    override lateinit var userPreferences: UserPreferences

    @Inject
    @Named("localeObservableList")
    lateinit var observableCardList: ObservableList<CardWithHistoryEntity>

    private val mutableLiveDataState =
        MutableLiveData<ManuallyCardAddModel>()
            .default(
                ManuallyCardAddModel.DefaultState
            )

    override val liveDataState: LiveData<ManuallyCardAddModel> =
        this.mutableLiveDataState

    init {
        appComponent.inject(this)
    }

    override fun registrationOfInteraction(lifecycleOwner: LifecycleOwner) =
        this.liveDataState.observe(lifecycleOwner) { currentState ->
            when (currentState) {
                is ManuallyCardAddModel.SavingDataState ->
                    currentState.cardEntity.apply cardEntity@{

                        CoroutineScope(Dispatchers.IO).launch {

                            questLocaleDataSource.insertCard(this@cardEntity)
                            delay(1000)

                            if (!checkOfflineEntrance()) {

                                CoroutineScope(Dispatchers.Main).launch {
                                    observableCardList notifyData questLocaleDataSource.loadCards()

                                    mutableLiveDataState.set(
                                        ManuallyCardAddModel.SavedDataState
                                    )
                                }
                                return@launch
                            }

                            caretakerCardsFirebase.syncByLocale().apply {

                                CoroutineScope(Dispatchers.Main).launch {

                                    observableCardList notifyData this@apply

                                    mutableLiveDataState.set(
                                        ManuallyCardAddModel.SavedDataState
                                    )
                                }
                            }

                        }
                    }

                else -> return@observe
            }
        }

    override fun actionСompleted() {
        this.mutableLiveDataState.default(
            ManuallyCardAddModel.DefaultState
        )
    }

    fun dataExtractionAction(
        supportBinding: ManuallyCardAddInputDataBinding,
        arguments: Bundle?,
    ): CardEntity? =
        arguments.extractParcelable<CardEntity>(
            TRANSACTION_KEY
        )?.apply {
            supportBinding.inputName.setText(this.name)
            supportBinding.inputNumber.setText(this.number)
            supportBinding.inputColor.imageTintList =
                this.color?.let {
                    ColorStateList.valueOf(
                        it
                    )
                }
        }

    inline fun cardAddAction(
        cardEntity: CardEntity,
        faultAction: messageAction,
    ) {
        if (!cardEntity.name.isNullOrEmpty()
            && !cardEntity.number.isNullOrEmpty()
        )
            this.mutableLiveDataState.set(
                ManuallyCardAddModel.SavingDataState(
                    CardWithHistoryEntity(cardEntity)
                )
            )
        else
            faultAction.invoke(
                R.string.msgWarningAddCard
            )
    }
}