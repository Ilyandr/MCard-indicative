package com.example.mcard.repository.features.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mcard.repository.features.storage.database.dao.QuestCardWithHistoryDao
import com.example.mcard.repository.models.storage.CardEntity
import com.example.mcard.repository.models.storage.HistoryEntity

@Database(
    entities = [
        HistoryEntity::class, CardEntity::class
    ],
    exportSchema = false,
    version = 1,
   /* autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]*/
)
internal abstract class QuestDatabase : RoomDatabase() {
    abstract fun questCardWithHistoryDao(): QuestCardWithHistoryDao
}