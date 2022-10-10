package com.example.mcard.domain.viewModels.basic.cards

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mcard.R
import com.example.mcard.domain.models.basic.cards.CommonCardModel
import com.example.mcard.presentation.views.observers.ObservableList
import com.example.mcard.repository.di.AppComponent
import com.example.mcard.repository.features.SupportLiveData.default
import com.example.mcard.repository.features.SupportLiveData.set
import com.example.mcard.repository.features.connection.NetworkListener.Companion.statusNetwork
import com.example.mcard.repository.features.getCurrentTime
import com.example.mcard.repository.features.utils.DesignCardManager.requireCardImageFile
import com.example.mcard.repository.features.utils.DesignCardManager.requireDrawable
import com.example.mcard.repository.features.utils.DesignCardManager.saveDrawable
import com.example.mcard.repository.features.rest.firebase.CaretakerCardsFirebase
import com.example.mcard.repository.features.rest.firebase.FirebaseController
import com.example.mcard.repository.features.storage.database.source.QuestLocaleDataSource
import com.example.mcard.repository.features.storage.preferences.UserPreferences
import com.example.mcard.repository.models.location.CardWithResult
import com.example.mcard.repository.models.storage.CardEntity
import com.example.mcard.repository.models.storage.CardWithHistoryEntity
import com.example.mcard.repository.models.storage.HistoryEntity
import com.example.mcard.repository.models.storage.HistoryEntity.CREATOR.setBigHistoryAddress
import com.example.mcard.repository.source.architecture.viewModels.LiveViewModel
import com.example.mcard.repository.source.usage.ChangeCardSource
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import javax.inject.Inject
import javax.inject.Named


internal class OpenCardViewModel(
    appComponent: AppComponent,
) : LiveViewModel<CommonCardModel>(), ChangeCardSource {

    @Inject
    lateinit var questLocaleDataSource: QuestLocaleDataSource

    @Inject
    lateinit var firebaseController: FirebaseController

    @Inject
    lateinit var dateFormat: SimpleDateFormat

    @Inject
    @Named("placeObservableList")
    lateinit var placeObservableList: ObservableList<CardWithResult>

    @Inject
    @Named("localeObservableList")
    lateinit var cardsObservableList: ObservableList<CardWithHistoryEntity>

    @Inject
    lateinit var caretakerCardsFirebase: CaretakerCardsFirebase

    @Inject
    override lateinit var userPreferences: UserPreferences

    private val mutableLiveData =
        MutableLiveData<CommonCardModel>()
            .default(CommonCardModel.DefaultState)

    override val liveDataState: LiveData<CommonCardModel> =
        this.mutableLiveData

    init {
        appComponent inject this
    }

    override infix fun registrationOfInteraction(lifecycleOwner: LifecycleOwner) =
        liveDataState.observe(lifecycleOwner) { currentState ->
            when (currentState) {
                is CommonCardModel.FinallyChangeBarcodeState -> {
                    currentState.cardModel?.apply data@{
                        cardEntity.barcode = currentState.data
                        saveAndApplyChange(
                            this@data, true
                        )
                    }
                }

                is CommonCardModel.AcceptImportState -> {
                    currentState.data?.run {
                        saveAndApplyChange(
                            CardWithHistoryEntity(this), false
                        )
                    }
                }

                else -> return@observe
            }
        }

    override fun actionСompleted() {
        mutableLiveData.default(
            CommonCardModel.DefaultState
        )
    }

    private fun saveAndApplyChange(
        cardModel: CardWithHistoryEntity, isChange: Boolean,
    ) =
        CoroutineScope(Dispatchers.IO).launch {

            if (isChange)
                questLocaleDataSource.changeCard(cardModel)
            else
                questLocaleDataSource.insertCard(cardModel)

            delay(500)

            if (!checkOfflineEntrance()) {

                CoroutineScope(Dispatchers.Main).launch {

                    cardsObservableList notifyData questLocaleDataSource.loadCards()
                    actionСompleted()
                }

                return@launch
            }

            caretakerCardsFirebase.syncByLocale().apply {
                CoroutineScope(Dispatchers.Main).launch {

                    cardsObservableList notifyData this@apply

                    if (isChange)
                        actionСompleted()
                    else
                        mutableLiveData.set(
                            CommonCardModel.CompleteState(
                                cardModel
                            )
                        )
                }
            }
        }

    infix fun saveNewHistoryUsage(
        cardWithHistoryEntity: CardWithHistoryEntity,
    ) {
        MainScope().launch(Dispatchers.Main) {
            withContext(MainScope().coroutineContext + Dispatchers.IO) {
                cardWithHistoryEntity
                    .cardEntity
                    .setHistoryModel()
                    .apply {
                        questLocaleDataSource.insertHistory(
                            this
                        )
                    }
            }.apply {
                changeInformation(
                    R.id.createNewHistoryBtn, cardWithHistoryEntity, this
                )

                mutableLiveData.set(
                    CommonCardModel.WriteHistoryState(
                        this
                    )
                )
            }
        }
    }

    private fun CardEntity.setHistoryModel() =
        placeObservableList.find { singleResult ->
            singleResult.cardId == uniqueIdentifier
        }?.run {
            HistoryEntity(
                timeAdd = dateFormat.getCurrentTime(),
                usageCardId = cardId,
                shopName = placeResult.name.toString(),
                shopAddress = setBigHistoryAddress(
                    Pair(
                        placeResult.geometry?.location.toString(),
                        placeResult.vicinity.toString()
                    )
                )
            )
        } ?: HistoryEntity(
            timeAdd = dateFormat.getCurrentTime(),
            usageCardId = uniqueIdentifier,
            shopName = name.toString(),
            ""
        )

    override fun barcodeAction() {
        mutableLiveData.set(
            CommonCardModel.ChangeBarcodeState
        )
    }

    fun imageAction(context: Context, path: Uri, name: String?) =
        (path requireDrawable context)?.saveDrawable(
            context requireCardImageFile name.toString()
        ) {
            mutableLiveData.set(
                CommonCardModel.ChangeImageState
            )
        }

    override fun publishAction(
        data: CardWithHistoryEntity?,
    ) {
        mutableLiveData.set(
            CommonCardModel.LoadingState
        )

        firebaseController.globalManagerDB.publishCard(
            data, faultAction = {
                mutableLiveData.set(
                    CommonCardModel.MessageState(it)
                )
            }
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                caretakerCardsFirebase.syncByLocale()
            }

            mutableLiveData.set(
                CommonCardModel.MessageState(it)
            )
        }
    }

    override fun removeAction(
        data: CardWithHistoryEntity?,
    ) {
        mutableLiveData.set(
            CommonCardModel.LoadingState
        )

        data?.cardEntity?.apply {

            if (!checkOfflineEntrance()) {

                CoroutineScope(Dispatchers.IO).launch {

                    questLocaleDataSource.deleteCard(data)
                    delay(1000)

                    withContext(Dispatchers.Main) {
                        questLocaleDataSource.loadCards()
                    }.run {
                        cardsObservableList notifyData this

                        mutableLiveData.set(
                            CommonCardModel.RemoveState
                        )
                    }
                }

                return@apply
            }

            firebaseController.globalManagerDB.removeGlobalCard(
                this, {
                    mutableLiveData.set(
                        CommonCardModel.MessageState(
                            it
                        )
                    )
                }
            ) {
                CoroutineScope(Dispatchers.IO).launch {

                    questLocaleDataSource.deleteCard(data)
                    delay(1000)

                    caretakerCardsFirebase.syncByLocale()

                    mutableLiveData.set(
                        CommonCardModel.RemoveState
                    )
                }
            }
        }
    }

    fun barcodeReadyChanged(
        cardModel: CardWithHistoryEntity?, data: String,
    ) {
        mutableLiveData.set(
            CommonCardModel.FinallyChangeBarcodeState(
                cardModel, data
            )
        )
    }

    fun <T> changeInformation(
        actionId: Int,
        cardModel: CardWithHistoryEntity,
        singleDataChange: T,
    ): CardWithHistoryEntity? =
        cardModel.cardEntity.run {

            if (singleDataChange is String)
                if (singleDataChange.isEmpty()) {
                    mutableLiveData.set(
                        CommonCardModel.MessageState(
                            R.string.msgWarningChangeCardInfo
                        )
                    )
                    return@run null
                }

            when (actionId) {
                R.id.confirmEditNameView ->
                    name = singleDataChange as String

                R.id.confirmEditNumberView ->
                    number = singleDataChange as String

                R.id.inputColor ->
                    color = singleDataChange as Int

                R.id.createNewHistoryBtn ->
                    cardModel.usageHistory.add(
                        singleDataChange as HistoryEntity
                    )
            }

            this@OpenCardViewModel.saveAndApplyChange(
                cardModel, true
            )

            mutableLiveData.set(
                CommonCardModel.MessageState(
                    R.string.successChangeCardInfo
                )
            )

            cardModel
        }

    infix fun acceptImportAction(cardEntity: CardEntity?) =
        mutableLiveData.set(
            if (statusNetwork)
                CommonCardModel.AcceptImportState(
                    cardEntity
                )
            else
                CommonCardModel.FailState
        )

    companion object {
        internal const val REQUEST_BARCODE_ACTION = "request_key"
        internal const val BARCODE_ACTION_RESULT = "barcode_result"
        internal const val IMAGE_ACTION_RESULT = "image_result"
    }
}