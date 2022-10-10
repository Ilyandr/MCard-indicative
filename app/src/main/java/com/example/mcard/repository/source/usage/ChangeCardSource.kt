package com.example.mcard.repository.source.usage

import com.example.mcard.repository.models.storage.CardWithHistoryEntity


internal interface ChangeCardSource: EntranceUsageSource {
    fun barcodeAction()

    infix fun publishAction(
        data: CardWithHistoryEntity?,
    )

    infix fun removeAction(
        data: CardWithHistoryEntity?,
    )
}