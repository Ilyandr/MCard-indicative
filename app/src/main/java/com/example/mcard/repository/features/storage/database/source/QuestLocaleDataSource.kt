package com.example.mcard.repository.features.storage.database.source

import com.example.mcard.repository.models.storage.CardWithHistoryEntity
import com.example.mcard.repository.models.storage.HistoryEntity

internal interface QuestLocaleDataSource {
    suspend fun selectHistory(parentId: String): List<HistoryEntity>

    suspend fun insertHistory(historyEntity: HistoryEntity)

    suspend fun removeDatabase()

    suspend fun insertCard(cardEntity: CardWithHistoryEntity)

    suspend fun loadCardsName(): List<String>

    suspend fun loadCards(): MutableList<CardWithHistoryEntity>

    suspend fun deleteCard(cardWithHistoryEntity: CardWithHistoryEntity)

    suspend fun changeCard(data: CardWithHistoryEntity)
}