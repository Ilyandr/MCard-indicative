package com.example.mcard.domain.viewModels.basic.cards.adding

import android.Manifest
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mcard.domain.models.basic.cards.adding.AutoCardAddingModel
import com.example.mcard.repository.features.SupportLiveData.default
import com.example.mcard.repository.features.SupportLiveData.set
import com.example.mcard.repository.features.messageAction
import com.example.mcard.repository.source.architecture.viewModels.LiveViewModel
import com.example.mcard.repository.source.usage.ProcessorSource
import com.example.mcard.repository.models.storage.CardWithHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

internal class AutoCardAddViewModel : LiveViewModel<AutoCardAddingModel>() {
    private val mutableLiveDataState =
        MutableLiveData<AutoCardAddingModel>()

    override val liveDataState: LiveData<AutoCardAddingModel> =
        this.mutableLiveDataState


    override fun registrationOfInteraction(lifecycleOwner: LifecycleOwner) {}

    override fun actionСompleted() {
        this.mutableLiveDataState.default(
            AutoCardAddingModel.DefaultState
        )
    }

    fun launchProcessAddingState(
        externalSingleAction: Boolean,
        processorController: ProcessorSource,
        statusAction: messageAction?,
    ) =
        MainScope().launch(Dispatchers.Main) {

            if (externalSingleAction)
                processorController.launchSingleBarcodeDetector {
                    mutableLiveDataState.set(
                        AutoCardAddingModel.ExternalUseBarcode(it)
                    )
                }
            else
                processorController.launchMultiDetector {
                    statusAction?.invoke(it)
                }

            mutableLiveDataState.set(
                AutoCardAddingModel.ProcessState
            )
        }

    fun getArrayPermission() =
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )

    infix fun saveAddingCard(cardEntity: CardWithHistoryEntity?) {
        mutableLiveDataState.set(
            AutoCardAddingModel.FinalAddingState(
                cardEntity?.cardEntity
            )
        )
    }
}