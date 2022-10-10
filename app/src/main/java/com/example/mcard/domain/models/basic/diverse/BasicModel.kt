package com.example.mcard.domain.models.basic.diverse

import com.example.mcard.repository.models.storage.CardWithHistoryEntity

internal sealed class BasicModel {
    internal object DefaultState : BasicModel()

    internal data class SortingListState(
        val list: List<CardWithHistoryEntity>
    ): BasicModel()

    internal object PrimaryInitState : BasicModel()

    internal data class RefreshState(
        val isRefreshing: Boolean,
    ) : BasicModel()

    internal object EmptyDataState : BasicModel()

    internal object SortingByGeolocationState : BasicModel()
}