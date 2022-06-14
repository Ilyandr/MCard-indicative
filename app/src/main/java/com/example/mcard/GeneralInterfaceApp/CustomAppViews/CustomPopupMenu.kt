package com.example.mcard.GeneralInterfaceApp.CustomAppViews

import android.content.Context
import android.os.Build
import android.view.View
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import com.example.mcard.R
import com.example.mcard.StorageAppActions.SharedPreferencesManager
import com.example.mcard.UserActionsCard.CustomSortCardsManager
import com.example.mcard.UserAuthorization.OfflineEntranceActions
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@DelicateCoroutinesApi
internal class CustomPopupMenu : PopupMenu, OfflineEntranceActions
{
    constructor(context: Context?, anchor: View?)
            : super(context, anchor)

    constructor(context: Context?, anchor: View?, gravity: Int)
            : super(context, anchor, gravity)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    constructor(context: Context?, anchor: View?, gravity: Int, popupStyleAttr: Int, popupStyleRes: Int)
            : super(context, anchor, gravity, popupStyleAttr, popupStyleRes)

    fun createMenuForSelectCardSort(
        sharedPreferencesManager: SharedPreferencesManager,
        additionalAction: Runnable)
    {
        this.inflate(R.menu.basic_select_menu)

            when (sharedPreferencesManager.sortedCardInfo())
            {
                CustomSortCardsManager.TYPE_GEOLOCATION ->
                    menu.getItem(0).isChecked = true
                CustomSortCardsManager.TYPE_FREQUENCY ->
                    menu.getItem(1).isChecked = true
                CustomSortCardsManager.TYPE_ABC ->
                    menu.getItem(2).isChecked = true
                CustomSortCardsManager.TYPE_DATE ->
                    menu.getItem(3).isChecked = true
            }

        this.setOnMenuItemClickListener {
            sharedPreferencesManager.sortedCardInfo(
                when (it.itemId)
                {
                    R.id.geoSort -> CustomSortCardsManager.TYPE_GEOLOCATION
                    R.id.countSort -> CustomSortCardsManager.TYPE_FREQUENCY
                    R.id.ABCSort -> CustomSortCardsManager.TYPE_ABC
                    R.id.dateSort -> CustomSortCardsManager.TYPE_DATE
                    else -> -1
                })
            GlobalScope.launch(Dispatchers.Main) { additionalAction.run() }.start()
            false
        }
        this.show()
    }
}