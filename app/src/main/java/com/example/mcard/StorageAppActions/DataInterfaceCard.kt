package com.example.mcard.StorageAppActions

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.Gravity

internal class DataInterfaceCard(val context: Context)
    : SharedPreferencesManager(context)
{
    companion object
    {
        const val TEXT_GRAVITY = "TEXT GRAVITY"
        const val CARD_SIZE = "CARD SIZE"
        const val TEXT_COLOR = "TEXT COLOR"
        const val GENERAL_APP_COLOR = "GENERAL COLOR"
        const val GENERAL_APP_COLOR_TYPE = "GENERAL COLOR TYPE"
        const val TEXT_SIZE = "TEXT SIZE"
        const val CARD_ROUND = "CARD ROUND"
        const val ICONS_COLOR = "ICONS APP COLOR"
        const val TEXT_APP_COLOR = "TEXT APP COLOR"
        const val ANIM_SELECTED = "ANIM SELECTED"
        private const val DEFAULT_SIZE = "DEFAULT SIZE"
        private const val DEFAULT_COORD = "DEFAULT COORD"
    }

    @SuppressLint("RtlHardcoded")
    fun actionTextGravity(setGravityInfo: Int? = null) : Int
    {
        if (setGravityInfo != null) editor.putInt(TEXT_GRAVITY, setGravityInfo).apply()
        return sharedPreferences.getInt(TEXT_GRAVITY, Gravity.LEFT)
    }

    fun actionCardSize(setCardHeight: Int? = null, setCardWidth: Int? = null): Pair<Int, Int>?
    {
        if (setCardHeight != null && setCardWidth != null)
            editor.putString(CARD_SIZE, "${setCardHeight}X${setCardWidth}").apply()

        val bigData = sharedPreferences.getString(CARD_SIZE, null)
        return if (bigData != null)
            Pair(bigData.split("X")[0].toInt(), bigData.split("X")[1].toInt())
        else null
    }

    fun actionTextColor(setAutoTextColor: Boolean? = null) : Boolean
    {
        if (setAutoTextColor != null)
            editor.putBoolean(
                TEXT_COLOR, setAutoTextColor).apply()
        return sharedPreferences.getBoolean(TEXT_COLOR, false)
    }

    fun actionGeneralInterfaceAppColor(
        inputColorInfo: Pair<Int, Boolean>? = null) : Pair<Int, Boolean>
    {
        if (inputColorInfo != null)
        {
            editor.putInt(
                GENERAL_APP_COLOR, inputColorInfo.first).apply()
            editor.putBoolean(
                GENERAL_APP_COLOR_TYPE, inputColorInfo.second).apply()
        }
        return Pair(sharedPreferences.getInt(
            GENERAL_APP_COLOR, Color.TRANSPARENT), sharedPreferences.getBoolean(
            GENERAL_APP_COLOR_TYPE, false))
    }

    fun actionGeneralIconsAppColor(inputColorInfo: Int? = null) : Int
    {
        if (inputColorInfo != null)
        {
            editor.putInt(
                ICONS_COLOR, inputColorInfo).apply()
        }
        return sharedPreferences.getInt(
            ICONS_COLOR, Color.TRANSPARENT)
    }

    fun actionGeneralTextAppColor(inputColorInfo: Int? = null) : Int
    {
        if (inputColorInfo != null)
        {
            editor.putInt(
                TEXT_APP_COLOR, inputColorInfo).apply()
        }
        return sharedPreferences.getInt(
            TEXT_APP_COLOR, Color.TRANSPARENT)
    }

    fun actionTextSize(setTextSize: Float? = null) : Float
    {
        if (setTextSize != null) editor.putFloat(TEXT_SIZE, setTextSize).apply()
        return sharedPreferences.getFloat(TEXT_SIZE, 19f)
    }

    fun actionRoundBorderCard(setCardBorder: Float? = null): Float
    {
        if (setCardBorder != null) editor.putFloat(CARD_ROUND, setCardBorder).apply()
        return sharedPreferences.getFloat(CARD_ROUND, 20f)
    }

    fun actionAnimSelectCard(setAnimSelectCard: Boolean? = null) : Boolean
    {
        if (setAnimSelectCard != null) editor.putBoolean(ANIM_SELECTED, setAnimSelectCard).apply()
        return sharedPreferences.getBoolean(ANIM_SELECTED, true)
    }

    fun actionDefaultCardSize(setCardHeight: Int? = null, setCardWidth: Int? = null, getMode: Boolean) =
        if (!getMode && !sharedPreferences.contains(DEFAULT_SIZE))
        { editor.putString(DEFAULT_SIZE, "${setCardHeight}X${setCardWidth}").apply(); null }
        else
        {
            val bigData = sharedPreferences.getString(DEFAULT_SIZE, null)
            if (bigData != null)Pair(bigData.split("X")[0].toInt(), bigData.split("X")[1].toInt())
            else null
        }

    fun actionCardCoord(coordX: Float? = null, coordY: Float? = null, getMode: Boolean) =
        if (!getMode)
        { editor.putString(DEFAULT_COORD, "${coordX}X${coordY}").apply(); null }
        else
        {
            val bigData = sharedPreferences.getString(DEFAULT_COORD, null)
            if (bigData != null)
                Pair(bigData.split("X")[0].toFloat()
                    , bigData.split("X")[1].toFloat())
            else null
        }

    fun actionDefaultSettings(itsAppInterface: Boolean) = if (itsAppInterface)
    {
        this.editor.remove(ICONS_COLOR).apply()
        this.editor.remove(TEXT_APP_COLOR).apply()
        this.editor.remove(GENERAL_APP_COLOR_TYPE).apply()
        this.editor.remove(GENERAL_APP_COLOR).apply()
    }
    else
    {
        this.editor.remove(TEXT_GRAVITY).apply()
        this.editor.remove(CARD_SIZE).apply()
        this.editor.remove(TEXT_COLOR).apply()
        this.editor.remove(TEXT_SIZE).apply()
        this.editor.remove(CARD_ROUND).apply()
        this.editor.remove(ANIM_SELECTED).apply()
        this.editor.remove(DEFAULT_COORD).apply()
    }

    fun getAllCardSettings() = hashMapOf<String, Any?>(TEXT_GRAVITY to actionTextGravity()
        , CARD_SIZE to actionCardSize()
        , TEXT_COLOR to actionTextColor()
        , TEXT_SIZE to actionTextSize()
        , CARD_ROUND to actionRoundBorderCard()
        , ANIM_SELECTED to actionAnimSelectCard())
}