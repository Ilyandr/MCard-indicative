package com.example.mcard.repository.features.storage.database.source

import com.example.mcard.repository.features.storage.database.QuestDatabase
import com.example.mcard.repository.models.storage.CardWithHistoryEntity
import com.example.mcard.repository.models.storage.HistoryEntity

internal class RoomQuestLocaleDataSource(
    private val questDatabase: QuestDatabase,
) : QuestLocaleDataSource {

    override suspend fun removeDatabase() =
        questDatabase
            .questCardWithHistoryDao()
            .removeDatabase()

    override suspend fun insertCard(cardEntity: CardWithHistoryEntity) =
        questDatabase
            .questCardWithHistoryDao()
            .insertCardWithHistory(cardEntity)

    override suspend fun selectHistory(
        parentId: String,
    ): List<HistoryEntity> =
        questDatabase
            .questCardWithHistoryDao()
            .loadHistory(parentId)

    override suspend fun insertHistory(historyEntity: HistoryEntity) =
        questDatabase
            .questCardWithHistoryDao()
            .insertHistory(historyEntity)

    override suspend fun loadCardsName(): List<String> =
        questDatabase
            .questCardWithHistoryDao()
            .loadCardsName()

    override suspend fun loadCards(): MutableList<CardWithHistoryEntity> =
        questDatabase
            .questCardWithHistoryDao()
            .loadCards()

    override suspend fun deleteCard(
        cardWithHistoryEntity: CardWithHistoryEntity,
    ) =
        questDatabase
            .questCardWithHistoryDao()
            .deleteCard(cardWithHistoryEntity)

    override suspend fun changeCard(
        data: CardWithHistoryEntity,
    ) =
        questDatabase
            .questCardWithHistoryDao()
            .changeCard(data.cardEntity)
}