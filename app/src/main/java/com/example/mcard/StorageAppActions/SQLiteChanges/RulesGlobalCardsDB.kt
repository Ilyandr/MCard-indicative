package com.example.mcard.StorageAppActions.SQLiteChanges

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase

internal class RulesGlobalCardsDB(context: Context?) : SQLiteOpenHelper(context
    , GeneralRulesDB.name_sql
    , null
    , GeneralRulesDB.version_sql)
{
    override fun onCreate(db: SQLiteDatabase) =
        db.execSQL(rulesCommandCreateDB)

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int)
    {
        db.execSQL("""drop table if exists ${GeneralRulesDB.TABLE_NAME}""")
        onCreate(db)
    }

    fun onRemove(db: SQLiteDatabase)
    {
        db.execSQL("""drop table if exists ${GeneralRulesDB.TABLE_NAME}""")
        onCreate(db)
    }

    companion object
    {
        private const val rulesCommandCreateDB =
            """create table ${GeneralRulesDB.TABLE_NAME}(${
                GeneralRulesDB.CARD_ID
            } integer primary key,${
                GeneralRulesDB.CARD_INFO
            } text, ${
                GeneralRulesDB.CARD_NUM
            } text, ${
                GeneralRulesDB.CARD_BARCODE
            } text, ${
                GeneralRulesDB.CARD_COLOR
            } integer, ${
                GeneralRulesDB.CARD_OWNER
            } integer, ${
                GeneralRulesDB.DATE_CARD_ADD
            } long, ${
                GeneralRulesDB.UNIQUE_IDENTIFIER
            } text)"""
    }
}