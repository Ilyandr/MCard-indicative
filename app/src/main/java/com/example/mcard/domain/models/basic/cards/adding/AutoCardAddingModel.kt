package com.example.mcard.domain.models.basic.cards.adding

import com.example.mcard.repository.models.storage.BarcodeModel
import com.example.mcard.repository.models.storage.CardEntity

internal sealed class AutoCardAddingModel {
    internal object DefaultState : AutoCardAddingModel()

    internal object ProcessState : AutoCardAddingModel()

    internal data class ExternalUseBarcode(
        val barcodeModel: BarcodeModel
        ): AutoCardAddingModel()

    internal data class FinalAddingState(
        val cardEntity: CardEntity?
    ) : AutoCardAddingModel()
}