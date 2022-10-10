package com.example.mcard.repository.features.storage.database.dao

import androidx.room.*
import com.example.mcard.repository.models.storage.CardEntity
import com.example.mcard.repository.models.storage.HistoryEntity
import com.example.mcard.repository.models.storage.CardWithHistoryEntity

@Dao
internal interface QuestCardWithHistoryDao {
    @Insert(
        entity = HistoryEntity::class,
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertHistory(historyEntity: HistoryEntity)

    @Insert(
        entity = CardEntity::class,
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertCard(cardEntity: CardEntity)

    @Transaction
    suspend fun insertCardWithHistory(
        cardWithHistoryEntity: CardWithHistoryEntity,
    ) {
        insertCard(cardWithHistoryEntity.cardEntity)

        cardWithHistoryEntity.usageHistory.forEach {
            insertHistory(it)
        }
    }

    @Transaction
    @Query("SELECT * FROM ${CardEntity.CARDS_TABLE_NAME}")
    suspend fun loadCards(): MutableList<CardWithHistoryEntity>

    @Transaction
    @Query("DELETE FROM ${CardEntity.CARDS_TABLE_NAME}")
    suspend fun removeDatabase()

    @Query("SELECT name FROM ${CardEntity.CARDS_TABLE_NAME}")
    suspend fun loadCardsName(): List<String>

    @Query("SELECT * FROM ${HistoryEntity.HISTORY_TABLE_NAME} WHERE usageCardId = :parentId")
    suspend fun loadHistory(
        parentId: String,
    ): List<HistoryEntity>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun changeCard(newCardEntity: CardEntity)

    @Transaction
    suspend fun deleteCard(
        cardWithHistoryEntity: CardWithHistoryEntity,
    ) {
        singleDeleteCard(cardWithHistoryEntity.cardEntity)

        cardWithHistoryEntity.usageHistory.forEach {
            singleDeleteHistory(it)
        }
    }

    @Delete(entity = CardEntity::class)
    suspend fun singleDeleteCard(cardEntity: CardEntity)

    @Delete(entity = HistoryEntity::class)
    suspend fun singleDeleteHistory(historyEntity: HistoryEntity)
}