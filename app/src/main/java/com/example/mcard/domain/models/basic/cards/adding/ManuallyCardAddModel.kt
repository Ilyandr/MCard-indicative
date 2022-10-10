package com.example.mcard.domain.models.basic.cards.adding

import com.example.mcard.repository.models.storage.CardWithHistoryEntity

internal sealed class ManuallyCardAddModel {
    internal object DefaultState : ManuallyCardAddModel()

    internal data class SavingDataState(
        val cardEntity: CardWithHistoryEntity,
    ) : ManuallyCardAddModel()

    internal object SavedDataState : ManuallyCardAddModel()
}