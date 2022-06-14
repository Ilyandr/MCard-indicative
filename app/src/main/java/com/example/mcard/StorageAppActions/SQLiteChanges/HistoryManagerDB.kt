package com.example.mcard.StorageAppActions.SQLiteChanges

import android.database.sqlite.SQLiteDatabase
import android.content.ContentValues
import android.annotation.SuppressLint
import android.content.Context
import kotlin.jvm.Synchronized
import com.example.mcard.AdapersGroup.HistoryUseInfoEntity
import java.text.SimpleDateFormat
import java.util.*

internal class HistoryManagerDB(context: Context)
{
    private var SQLiteDatabase: SQLiteDatabase? = null
    private val databaseRules: RulesUseCardsHistoryDB
    private val contentValues: ContentValues

    init
    {
        this.databaseRules = RulesUseCardsHistoryDB(context)
        this.contentValues = ContentValues()
    }

    @SuppressLint("SimpleDateFormat")
    @Synchronized
    fun writeNewHistoryDB(nameShop: String, addressShop: String)
    {
        this.SQLiteDatabase = databaseRules.writableDatabase

        this.contentValues.put(RulesUseCardsHistoryDB.SHOP_NAME, nameShop)
        this.contentValues.put(RulesUseCardsHistoryDB.SHOP_ADDRESS, addressShop)
        this.contentValues.put(RulesUseCardsHistoryDB.TIME_ADD
            , SimpleDateFormat("dd.MM.yyyy HH:mm")
                .format(Calendar.getInstance().time))

        this.SQLiteDatabase?.insert(
            RulesUseCardsHistoryDB.TABLE_NAME
            , null
            , contentValues)

        this.contentValues.clear()
        this.SQLiteDatabase?.close()
    }

    @Synchronized
    fun readDBHistory(getZerolist: MutableList<HistoryUseInfoEntity?>)
    {
        this.SQLiteDatabase =
            databaseRules.readableDatabase

        this.SQLiteDatabase?.query(
            RulesUseCardsHistoryDB.TABLE_NAME
            , null
            , null
            , null
            , null
            , null
            , null)?.let { cursor ->
            if (cursor.moveToFirst())
            {
                cursor.getColumnIndex(RulesUseCardsHistoryDB.HISTORY_ID)
                val shopNameId = cursor.getColumnIndex(RulesUseCardsHistoryDB.SHOP_NAME)
                val shopAddressId = cursor.getColumnIndex(RulesUseCardsHistoryDB.SHOP_ADDRESS)
                val timeAddId = cursor.getColumnIndex(RulesUseCardsHistoryDB.TIME_ADD)

                do getZerolist.add(
                    HistoryUseInfoEntity(
                        cursor.getString(shopNameId)
                        , cursor.getString(shopAddressId)
                        , cursor.getString(timeAddId)))
                while (cursor.moveToNext())
            }
            cursor.close()
        }
        this.SQLiteDatabase?.close()
    }

    @Synchronized
    fun removeSingleActionHistory(timeWriteHistoryValue: String): Boolean
    {
        this.SQLiteDatabase =
            databaseRules.writableDatabase

        this.SQLiteDatabase?.execSQL(
            "DELETE FROM " + RulesUseCardsHistoryDB.TABLE_NAME
                    + " WHERE " + RulesUseCardsHistoryDB.TIME_ADD
                    + " = '" + timeWriteHistoryValue + "'"
        )

        this.SQLiteDatabase?.close()
        return true
    }

    @Synchronized
    fun removeDb()
    {
        this.SQLiteDatabase =
            databaseRules.writableDatabase
        this.databaseRules.onRemove(SQLiteDatabase ?: return)
        this.SQLiteDatabase?.close()
    }
}