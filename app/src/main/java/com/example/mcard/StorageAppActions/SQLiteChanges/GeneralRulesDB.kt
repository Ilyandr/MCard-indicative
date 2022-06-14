package com.example.mcard.StorageAppActions.SQLiteChanges

internal object GeneralRulesDB
{
    const val version_sql = 8
    const val name_sql = "save_card_global"
    const val TABLE_NAME = "CARD"

    const val CARD_ID = "_id"
    const val UNIQUE_IDENTIFIER = "unique_identifier"
    const val CARD_INFO = "information"
    const val CARD_NUM = "number"
    const val CARD_BARCODE = "barcode"
    const val CARD_COLOR = "color"
    const val CARD_OWNER = "owner"
    const val DATE_CARD_ADD = "date"

    const val BASIC_CONTROLLED_CARD_COUNT = 10
    const val TRANSACTION_KEY = "key"
}