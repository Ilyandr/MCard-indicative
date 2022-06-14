package com.example.mcard.StorageAppActions.SQLiteChanges

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase

internal class RulesUseCardsHistoryDB(context: Context?) : SQLiteOpenHelper(context
    , name_sql
    , null
    , version_sql
)
{

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) =
        sqLiteDatabase.execSQL(rulesCommandCreateDB)

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int)
    {
        db.execSQL("drop table if exists $TABLE_NAME")
        onCreate(db)
    }

    fun onRemove(db: SQLiteDatabase)
    {
        db.execSQL("drop table if exists $TABLE_NAME")
        onCreate(db)
    }

    companion object
    {
        const val version_sql = 1
        const val name_sql = "history_card"
        const val TABLE_NAME = "HISTORY_USE"
        const val HISTORY_ID = "_id"
        const val SHOP_NAME = "information"
        const val SHOP_ADDRESS = "number"
        const val TIME_ADD = "barcode"

        private const val rulesCommandCreateDB =
            """create table $TABLE_NAME($HISTORY_ID integer primary key,$SHOP_NAME text, $SHOP_ADDRESS text, $TIME_ADD text)"""
    }
}