package com.example.mcard.UserLocation.RequestControllerGroup

import android.widget.Toast
import com.example.mcard.AdapersGroup.BasicCardManagerAdapter
import com.example.mcard.AdapersGroup.CardInfoEntity
import com.example.mcard.BasicAppActivity
import com.example.mcard.R
import com.example.mcard.StorageAppActions.SQLiteChanges.CardManagerDB
import com.example.mcard.StorageAppActions.SharedPreferencesManager
import com.example.mcard.UserActionsCard.CustomSortCardsManager.sortByInternalSystem
import com.example.mcard.UserLocation.ResponseAPIEntity.PlaceSearchResponse
import kotlinx.coroutines.*
import java.util.*

@DelicateCoroutinesApi
internal open class GeolocationService(
    private val context: BasicAppActivity)
{
    private lateinit var completionResponseMap: MutableMap<String?, String?>
    private lateinit var cardManagerDB: CardManagerDB

    fun build(allBody: PlaceSearchResponse?): GeolocationService
    {
        this.completionResponseMap = LinkedHashMap()
        this.cardManagerDB = CardManagerDB(this.context)

        if (allBody?.status == "OK")
            allBody.result?.forEach { (name, vicinity) ->
                completionResponseMap[name] = vicinity}
        return this
    }

    fun launchSearchMatches()
    {
        if (completionResponseMap.isEmpty())
            Toast.makeText(context
                , R.string.messageGeoEmpty
                , Toast.LENGTH_LONG)
                .show()
        else
        {
            val listAllDataLocaleUserDB: Deferred<List<CardInfoEntity>> =
                GlobalScope.async(Dispatchers.IO) {
                    CardManagerDB(context).readAllCards()
                }

            GlobalScope.launch(Dispatchers.Main)
            {
                val resultMatchesList: MutableList<CardInfoEntity> = ArrayList()
                val resultCoincidenceList: MutableList<Int?> = ArrayList()

                this@GeolocationService.completionResponseMap.keys.forEach {responseData ->
                    listAllDataLocaleUserDB.await().forEach {userData ->
                        if (BasicCardManagerAdapter
                                .percentageSimilarityResult(
                                    userData.name, responseData) >= 75)
                        {
                            if (!resultCoincidenceList.contains(
                                    userData.uniqueIdentifier.hashCode()))
                                resultMatchesList.add(userData)
                            resultCoincidenceList.add(
                                userData.uniqueIdentifier.hashCode())
                        }
                    }
                }; resultCoincidenceList.clear()

                if (resultMatchesList.size != 0)
                {
                    context.generalCardsListView.completionResponseMap =
                        this@GeolocationService.completionResponseMap

                    context.generalCardsListView.adapter =
                        BasicCardManagerAdapter(
                            context
                            , cardManagerDB.readAllCards()
                                .sortByInternalSystem(
                                    SharedPreferencesManager(context).sortedCardInfo(null)
                                    , resultMatchesList)
                            , false)
                    context.generalCardsListView.offLoadingAnimations(true)
                }
                 else showFaultToast()
            }.start()
        }; context.generalCardsListView.offLoadingAnimations(false)
    }

    fun showFaultToast() =
        Toast.makeText(context
            , context.getString(R.string.NoGEOFind)
            , Toast.LENGTH_LONG)
            .show()
}