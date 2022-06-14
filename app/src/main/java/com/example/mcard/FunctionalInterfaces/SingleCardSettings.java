package com.example.mcard.FunctionalInterfaces;

import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomRealtiveLayout;
import com.example.mcard.StorageAppActions.DataInterfaceCard;

import kotlin.Pair;

public interface SingleCardSettings
{
    default void settingsCard(DataInterfaceCard dataInterfaceCard
            , CustomRealtiveLayout customRealtiveLayout
            , AppCompatTextView cardName
            , AppCompatTextView cardNumber)
    {
        Pair<Integer, Integer> dataSize = dataInterfaceCard.actionCardSize(null, null);
        if (dataSize != null && customRealtiveLayout != null)
        {
            final LinearLayout.LayoutParams newParams = new LinearLayout.LayoutParams(dataSize.component2()
                    , dataSize.component1());

            Pair<Float, Float> dataCoord = dataInterfaceCard.actionCardCoord(null, null, true);
            customRealtiveLayout.setX(dataCoord.component1());

            newParams.setMargins(0, 40, 0, 20);
            customRealtiveLayout.setLayoutParams(newParams);
        }

        final int gravityInfo = dataInterfaceCard.actionTextGravity(null);
        final float textSizeInfo = dataInterfaceCard.actionTextSize(null);

        cardName.setGravity(gravityInfo);
        cardNumber.setGravity(gravityInfo);

        cardName.setTextSize(textSizeInfo);
        cardNumber.setTextSize(textSizeInfo);
    }

    default void settingsCard(DataInterfaceCard dataInterfaceCard
            , CustomRealtiveLayout customRealtiveLayout
            , AppCompatEditText cardName
            , AppCompatEditText cardNumber)
    {
        Pair<Integer, Integer> dataSize = dataInterfaceCard.actionCardSize(null, null);
        if (dataSize != null && customRealtiveLayout != null)
        {
            final LinearLayout.LayoutParams newParams = new LinearLayout.LayoutParams(dataSize.component2()
                    , dataSize.component1());

            Pair<Float, Float> dataCoord = dataInterfaceCard.actionCardCoord(null, null, true);
            customRealtiveLayout.setX(dataCoord.component1());

            newParams.setMargins(0, 40, 0, 20);
            customRealtiveLayout.setLayoutParams(newParams);
        }

        final int gravityInfo = dataInterfaceCard.actionTextGravity(null);
        final float textSizeInfo = dataInterfaceCard.actionTextSize(null);

        cardName.setGravity(gravityInfo);
        cardNumber.setGravity(gravityInfo);

        cardName.setTextSize(textSizeInfo);
        cardNumber.setTextSize(textSizeInfo);
    }

    void optionsViewByBarcodeCard();
}
