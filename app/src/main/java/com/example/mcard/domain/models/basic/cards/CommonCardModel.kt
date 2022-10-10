package com.example.mcard.domain.models.basic.cards

import androidx.annotation.StringRes
import com.example.mcard.repository.models.storage.CardEntity
import com.example.mcard.repository.models.storage.CardWithHistoryEntity
import com.example.mcard.repository.models.storage.HistoryEntity

internal sealed class CommonCardModel {
    internal object DefaultState : CommonCardModel()

    internal object LoadingState : CommonCardModel()

    internal data class CompleteState(
        val saveData: CardWithHistoryEntity,
    ) : CommonCardModel()

    internal data class WriteHistoryState(
        val historyEntity: HistoryEntity,
    ) : CommonCardModel()

    internal object ChangeBarcodeState : CommonCardModel()

    internal data class FinallyChangeBarcodeState(
        val cardModel: CardWithHistoryEntity?,
        val data: String,
    ) : CommonCardModel()

    internal object ChangeImageState : CommonCardModel()

    internal data class MessageState(
        @StringRes val messageId: Int,
    ) : CommonCardModel()

    internal object RemoveState : CommonCardModel()

    internal object FailState : CommonCardModel()

    internal data class AcceptImportState(
        val data: CardEntity?,
    ) : CommonCardModel()
}
