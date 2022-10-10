package com.example.mcard.repository.features.optionally

import com.example.mcard.repository.models.storage.CardWithHistoryEntity

internal object CardsSorting {

    fun List<CardWithHistoryEntity>.sortByFrequency() =
        this.toMutableList()
            .sortedBy {
                it.usageHistory.size
            }.reversed()

    fun List<CardWithHistoryEntity>.sortByDate() =
        this.toMutableList()
            .sortedBy { singleCardEntity ->
                singleCardEntity.cardEntity.dateAddCard
            }.reversed()

    fun List<CardWithHistoryEntity>.sortByABC() =
        this.toMutableList()
            .sortedBy { singleCardEntity ->
                singleCardEntity.cardEntity.name
            }.reversed()

    const val GEO_SORT = -921
    const val FREQUENCY_SORT = -942
    const val DATE_SORT = -963
    const val ABC_SORT = -984
}