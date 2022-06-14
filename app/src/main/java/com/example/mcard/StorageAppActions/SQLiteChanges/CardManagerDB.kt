package com.example.mcard.StorageAppActions.SQLiteChanges

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import com.example.mcard.AdapersGroup.CardInfoEntity
import com.example.mcard.CommercialAction.YandexADS.MediationNetworkEntity
import com.example.mcard.CommercialAction.YandexADS.RewardedMobileMediationManager.buildAndShowAd
import com.example.mcard.GroupServerActions.SubscribeController.Companion.realDate
import com.example.mcard.GroupServerActions.SubscribeController.Companion.toDate
import com.example.mcard.GroupServerActions.SyncPersonalManager
import com.example.mcard.R
import com.example.mcard.StorageAppActions.SQLiteChanges.GeneralRulesDB.BASIC_CONTROLLED_CARD_COUNT
import com.example.mcard.StorageAppActions.SQLiteChanges.GeneralRulesDB.CARD_BARCODE
import com.example.mcard.StorageAppActions.SQLiteChanges.GeneralRulesDB.CARD_COLOR
import com.example.mcard.StorageAppActions.SQLiteChanges.GeneralRulesDB.CARD_ID
import com.example.mcard.StorageAppActions.SQLiteChanges.GeneralRulesDB.CARD_INFO
import com.example.mcard.StorageAppActions.SQLiteChanges.GeneralRulesDB.CARD_NUM
import com.example.mcard.StorageAppActions.SQLiteChanges.GeneralRulesDB.CARD_OWNER
import com.example.mcard.StorageAppActions.SQLiteChanges.GeneralRulesDB.DATE_CARD_ADD
import com.example.mcard.StorageAppActions.SQLiteChanges.GeneralRulesDB.TABLE_NAME
import com.example.mcard.StorageAppActions.SQLiteChanges.GeneralRulesDB.UNIQUE_IDENTIFIER
import com.example.mcard.UserActionsCard.CardAddActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import java.text.SimpleDateFormat
import java.util.*

@DelicateCoroutinesApi
internal class CardManagerDB(private val context: Context)
{
    private val rulesGlobalCardsDB: RulesGlobalCardsDB
    private var SQLiteDatabase: SQLiteDatabase? = null
    private var cursor: Cursor? = null
    private var singleCardID = 0

    companion object
    {
        const val key_operation = "INFO"
        const val BARCODE_NONE = "-"
    }

    init
    {
        this.rulesGlobalCardsDB =
            RulesGlobalCardsDB(context)
    }

    @Synchronized
    fun saveAndFlashDB(
        cardInfoEntity: CardInfoEntity,
        actionAfter: Runnable)
    {
        val uniqueIdentifier = cardInfoEntity.uniqueIdentifier
            ?: setUniqueIdentifier()

        if (haveCardInDataBase(uniqueIdentifier
                , cardInfoEntity.barcode) != null)
            replaceObjectDB(cardInfoEntity)
        else if (readAllCards().size >= BASIC_CONTROLLED_CARD_COUNT)
            context.buildAndShowAd(
                MediationNetworkEntity(
                    if (context is CardAddActivity)
                        R.string.warningSubscribeStorageOutput
                    else R.string.warningSubscribeStorageInput
                    , null
                    , null)
                {
                    finalActionAddCard(uniqueIdentifier, cardInfoEntity)
                    actionAfter.run()
                })
        else
            finalActionAddCard(
                uniqueIdentifier, cardInfoEntity)
        actionAfter.run()
    }

    @Synchronized
    private fun finalActionAddCard(
        uniqueIdentifier: String, cardInfoEntity: CardInfoEntity)
    {
        val contentValues = ContentValues()
        this.SQLiteDatabase = rulesGlobalCardsDB.writableDatabase

        contentValues.put(CARD_INFO, cardInfoEntity.name)
        contentValues.put(CARD_NUM, cardInfoEntity.number)
        contentValues.put(CARD_BARCODE, cardInfoEntity.barcode)
        contentValues.put(CARD_COLOR, cardInfoEntity.color)
        contentValues.put(CARD_OWNER, cardInfoEntity.cardOwner)
        contentValues.put(DATE_CARD_ADD, cardInfoEntity.dateAddCard ?: realDate().toDate())
        contentValues.put(UNIQUE_IDENTIFIER, uniqueIdentifier)

        this.SQLiteDatabase?.insert(
            TABLE_NAME
            , null
            , contentValues)
        this.SQLiteDatabase?.close()
    }

    @Synchronized
    fun haveCardInDataBase(
        uniqueIdentifier: String, barcodeData: String?): CardInfoEntity?
    {
        readAllCards().also { allUserCards ->
            for (i in allUserCards.indices)
                try
                {
                    if ((allUserCards[i]
                            .uniqueIdentifier
                                == uniqueIdentifier)
                        || barcodeData != null
                        && allUserCards[i].barcode == barcodeData
                        && allUserCards[i].barcode != BARCODE_NONE)
                        return allUserCards[i]
                } catch (emptyDB: NullPointerException) { }
            allUserCards.clear()
            return null
        }
    }

    @Synchronized
    fun primaryStartApp(): Boolean
    {
        this.cursor = rulesGlobalCardsDB
            .readableDatabase
            .rawQuery(
                "SELECT *"
                        + " FROM " + TABLE_NAME
                , null)

        val primaryStartApp = (cursor?.count == 0)
        cursor?.close()
        return primaryStartApp
    }

    @SuppressLint("Range")
    @Synchronized
    @Throws(ConcurrentModificationException::class)
    fun readAllCards(): MutableList<CardInfoEntity>
    {
        val findAllList: MutableList<CardInfoEntity> = ArrayList()
        try
        {
            this.SQLiteDatabase =
                rulesGlobalCardsDB.readableDatabase
            this.cursor = SQLiteDatabase?.rawQuery(
                "SELECT *"
                        + " FROM " + TABLE_NAME
                , null)

            this.cursor?.let { cursor ->
                if (cursor.moveToFirst())
                {
                    singleCardID = cursor.getColumnIndex(CARD_ID)
                    do {
                        if (cursor.getString(
                                cursor.getColumnIndex(CARD_NUM)) != null)
                            findAllList.add(
                                CardInfoEntity(
                                    cursor.getString(cursor.getColumnIndex(CARD_NUM))
                                    , cursor.getString(cursor.getColumnIndex(CARD_INFO))
                                    , cursor.getString(cursor.getColumnIndex(CARD_BARCODE))
                                    , cursor.getInt(cursor.getColumnIndex(CARD_COLOR))
                                    , cursor.getString(cursor.getColumnIndex(CARD_OWNER))
                                    , cursor.getLong(cursor.getColumnIndex(DATE_CARD_ADD))
                                    , cursor.getString(cursor.getColumnIndex(UNIQUE_IDENTIFIER))
                            )
                        )
                    } while (cursor.moveToNext())
                    cursor.close()
                }
            }
        } catch (ignored: SQLiteException) { }

        this.SQLiteDatabase!!.close()
        return findAllList
    }

    @Synchronized
    fun replaceObjectDB(newCardInfoEntity: CardInfoEntity)
    {
        this.SQLiteDatabase =
            rulesGlobalCardsDB.writableDatabase
        val newInputSingleCard = ContentValues()

        newInputSingleCard.put(CARD_INFO, newCardInfoEntity.name)
        newInputSingleCard.put(CARD_NUM, newCardInfoEntity.number)
        newInputSingleCard.put(CARD_BARCODE, newCardInfoEntity.barcode)
        newInputSingleCard.put(CARD_COLOR, newCardInfoEntity.color)
        newInputSingleCard.put(CARD_OWNER, newCardInfoEntity.cardOwner)
        newInputSingleCard.put(DATE_CARD_ADD, newCardInfoEntity.dateAddCard ?: realDate().toDate())
        newInputSingleCard.put(UNIQUE_IDENTIFIER, newCardInfoEntity.uniqueIdentifier)

        this.SQLiteDatabase?.update(
            TABLE_NAME,
            newInputSingleCard,
            UNIQUE_IDENTIFIER + " = '" + newCardInfoEntity.uniqueIdentifier + "'"
            , null)

        newInputSingleCard.clear()
        this.SQLiteDatabase?.close()
    }

    @Synchronized
    fun removeObjectDB(uniqueIdentifier: String)
    {
        this.SQLiteDatabase =
            rulesGlobalCardsDB.writableDatabase
        val sqlDbHelp =
            rulesGlobalCardsDB.readableDatabase
        this.cursor = sqlDbHelp.query(
            TABLE_NAME, null, null, null, null, null, null)

        this.SQLiteDatabase?.execSQL(
            "DELETE FROM " + TABLE_NAME
                    + " WHERE " + UNIQUE_IDENTIFIER
                    + " = '" + uniqueIdentifier + "'")
        this.cursor?.close()
        this.SQLiteDatabase?.close()
    }

    @SuppressLint("Range")
    @Synchronized
    fun allCardDefaultName(): List<String>
    {
        val allCardDefaultName: MutableList<String> = ArrayList()
        this.SQLiteDatabase =
            rulesGlobalCardsDB.readableDatabase
        this.cursor = SQLiteDatabase?.rawQuery(
            "SELECT "
                    + CARD_INFO
                    + " FROM "
                    + TABLE_NAME
            , null)

        this.cursor?.let { cursor ->
            if (cursor.moveToFirst())
                do allCardDefaultName.add(
                    cursor.getString(
                        cursor.getColumnIndex(CARD_INFO))
                        ?: break)
                while (cursor.moveToNext())
        }

        this.cursor?.close()
        return allCardDefaultName
    }

    @SuppressLint("SimpleDateFormat")
    private fun setUniqueIdentifier() =
        (SimpleDateFormat("ddMMyyyySSSSssZ")
            .format(Calendar.getInstance().time)
                + SyncPersonalManager
            .generationPersonalUID()
            .hashCode() / 2)

    @Synchronized
    fun removeDatabase()
    {
        this.SQLiteDatabase =
            rulesGlobalCardsDB.writableDatabase
        this.rulesGlobalCardsDB
            .onRemove(SQLiteDatabase ?: return)
        this.SQLiteDatabase?.close()
    }
}