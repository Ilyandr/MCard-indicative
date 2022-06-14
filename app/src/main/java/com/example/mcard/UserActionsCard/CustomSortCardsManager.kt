package com.example.mcard.UserActionsCard

import android.content.Context
import com.example.mcard.AdapersGroup.BasicCardManagerAdapter.percentageSimilarityResult
import com.example.mcard.AdapersGroup.CardInfoEntity
import com.example.mcard.AdapersGroup.HistoryUseInfoEntity
import com.example.mcard.BasicAppActivity
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomHeaderListView
import com.example.mcard.GroupServerActions.SubscribeController
import com.example.mcard.StorageAppActions.SQLiteChanges.HistoryManagerDB
import com.example.mcard.StorageAppActions.SharedPreferencesManager
import com.example.mcard.UserAuthorization.OfflineEntranceActions
import kotlinx.coroutines.DelicateCoroutinesApi
import java.util.*

@DelicateCoroutinesApi
internal object CustomSortCardsManager: OfflineEntranceActions
{
    const val TYPE_GEOLOCATION = 377
    const val TYPE_FREQUENCY = 378
    const val TYPE_DATE = 379
    const val TYPE_ABC = 380

    private var actionsAdditionaly: CustomHeaderListView? = null

    @JvmStatic
    fun MutableList<CardInfoEntity>.sortByInternalSystem(
        typeSort: Int,
        arguments: Any? = null,
        actionsAdditionaly: CustomHeaderListView? = null,
    ): MutableList<CardInfoEntity>
    {
        if (this.isEmpty())
        {
            actionsAdditionaly?.offLoadingAnimations(false)
            actionsAdditionaly?.checkEmptyList(true)
        }
        this@CustomSortCardsManager.actionsAdditionaly = actionsAdditionaly

        return when(typeSort)
        {
            TYPE_GEOLOCATION -> this sortByGeolocation arguments
            TYPE_FREQUENCY -> this sortByFrequency arguments as? Context
            TYPE_DATE -> this.sortByDate()
            TYPE_ABC -> this.sortByABC()
            else -> this
        }
    }

    private infix fun MutableList<CardInfoEntity>.sortByGeolocation(
        arguments: Any?,
    ): MutableList<CardInfoEntity> =
        arguments?.let { listFindCrads ->
            if (listFindCrads is BasicAppActivity)
            {
                actionsAdditionaly?.let {
                    if (!this@CustomSortCardsManager.checkOfflineEntrance(
                            SharedPreferencesManager(listFindCrads)))
                    {
                        listFindCrads.showOfferDialogRegistration()
                        this@CustomSortCardsManager
                            .actionsAdditionaly
                            ?.offLoadingAnimations(false)
                    }
                    else
                        SubscribeController(listFindCrads)
                            .checkFindGEO(
                                SubscribeController.MODE_GET, it)
                }
                this
            }
            else
            {
                val completeSortedData = LinkedList<CardInfoEntity>()
                for (singleUserCardsIndex in this.indices)
                    for (singleFindsCardsIndex in (listFindCrads as? List<*>)?.indices
                        ?: return@let this)
                        if (this[singleUserCardsIndex].uniqueIdentifier.equals(
                                (listFindCrads[singleFindsCardsIndex] as CardInfoEntity)
                                    .uniqueIdentifier))
                            completeSortedData.add(listFindCrads[
                                    singleFindsCardsIndex] as CardInfoEntity)

                completeSortedData.addAll(this)
                this.clear()
                (listFindCrads as? MutableList<*>)?.clear()

                completeSortedData.distinctBy {
                        singleCardEntity -> singleCardEntity.uniqueIdentifier }
            } as MutableList<CardInfoEntity>
        } ?:  this

    private infix fun MutableList<CardInfoEntity>.sortByFrequency(
        context: Context?,
    ): MutableList<CardInfoEntity> =
        (context?.let { contextNonNull ->
            val listHistoryInfo = mutableListOf<HistoryUseInfoEntity?>()
            val completeSortedData = LinkedList<CardInfoEntity>()
            HistoryManagerDB(context = contextNonNull).readDBHistory(listHistoryInfo)
            val sortedData = listHistoryInfo.groupingBy { it?.shopName }
                .eachCount()
                .filter { it.value > 0 }
                .entries
                .sortedBy { it.value }
                .reversed()

            for (singleSortedData in sortedData.indices)
                for (singleUserCardsIterator in this.indices)
                    if (percentageSimilarityResult(
                            this[singleUserCardsIterator].name, sortedData[singleSortedData].key) >= 75)
                        completeSortedData.add(this[singleUserCardsIterator])

            completeSortedData.addAll(this)
            this.clear()
            actionsAdditionaly?.swipeRefreshLayout?.isRefreshing = false

            completeSortedData.distinctBy {
                    singleCardEntity -> singleCardEntity.uniqueIdentifier }
        } ?: this) as MutableList<CardInfoEntity>

    private fun List<CardInfoEntity>.sortByDate(): MutableList<CardInfoEntity>
    {
        if (this.isEmpty())
            return this.toMutableList()
        actionsAdditionaly?.swipeRefreshLayout?.isRefreshing = false
        return this.sortedBy { singleCardEntity ->
            singleCardEntity.dateAddCard } as MutableList<CardInfoEntity>
    }

    private fun List<CardInfoEntity>.sortByABC(): MutableList<CardInfoEntity>
    {
        if (this.isEmpty())
            return this.toMutableList()
        actionsAdditionaly?.swipeRefreshLayout?.isRefreshing = false
        return this.sortedBy { singleCardEntity ->
            singleCardEntity.name } as MutableList<CardInfoEntity>
    }
}