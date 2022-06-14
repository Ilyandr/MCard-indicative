package com.example.mcard.UserLocation

import com.example.mcard.GlobalListeners.NetworkListener.Companion.getStatusNetwork
import com.example.mcard.BasicAppActivity
import android.widget.Toast
import com.example.mcard.R
import com.example.mcard.FunctionalInterfaces.DelegateVoidInterface
import com.example.mcard.GroupServerActions.SubscribeController
import com.example.mcard.SideFunctionality.CustomAppDialog
import android.content.Intent
import android.provider.Settings
import com.example.mcard.UserLocation.RequestControllerGroup.GeolocationControllerAPI
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
internal object GeolocationFindStarter
{
   @JvmStatic
   fun safeLaunchGeoFind(context: BasicAppActivity) =
        if (!getStatusNetwork())
            Toast.makeText(
                context
                , R.string.NetworkErr
                , Toast.LENGTH_LONG)
                .show()
        else
            GeolocationListener(context, Pair(
                DelegateVoidInterface { inputObject: Any? ->
                    GeolocationControllerAPI(context)
                        .launchSearchByGEO((inputObject as String?)
                            ?: return@DelegateVoidInterface)
                    SubscribeController(context).checkFindGEO(
                        SubscribeController.MODE_POST, null)
                }, Runnable {
                    context.generalCardsListView
                        ?.swipeRefreshLayout
                        ?.isRefreshing = false
                    CustomAppDialog(context)
                        .buildEntityDialog(false)
                        .setTitle("Управление данными")
                        .setMessage(
                            R.string.messageActionGeolocation,
                           3f)
                        .setPositiveButton("Включить") {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            )
                        }
                        .setNegativeButton("Назад", null)
                        .show()
                })
        ).build()
    }