package com.example.mcard.repository.features.location.dataSource

import com.example.mcard.presentation.views.observers.ObservableList
import com.example.mcard.R
import com.example.mcard.repository.features.messageAction
import com.example.mcard.repository.features.optionally.CardAdaptation.comparisonData
import com.example.mcard.repository.models.location.PlaceSearchResponse
import com.example.mcard.repository.models.storage.CardWithHistoryEntity
import com.example.mcard.repository.models.location.CardWithResult
import com.example.mcard.repository.models.location.Result
import kotlinx.coroutines.*
import java.util.*


internal class LocationService(
    private val observableCardList: ObservableList<CardWithHistoryEntity>,
    private val observablePlaceList: ObservableList<CardWithResult>,
) {
    private val completionResponseMap: LinkedList<Result> by lazy {
        LinkedList()
    }

    private var messageAction: messageAction? = null

    fun create(
        messageAction: messageAction?,
        allBody: PlaceSearchResponse?,
    ): LocationService {
        if (allBody?.status == "OK")
            allBody.result?.apply {
                completionResponseMap.addAll(this)
            }

        this.messageAction = messageAction
        return this
    }

    fun launchSearchMatches() {
        if (completionResponseMap.isEmpty()) {
            messageAction?.invoke(R.string.messageGeoEmpty)
            return
        }

        CoroutineScope(Dispatchers.Main).launch {

            mutableListOf<CardWithHistoryEntity>().apply {

                this.addAll(observableCardList.mutableList)

                observableCardList.mutableList.forEach { singleCard ->

                    this@LocationService.completionResponseMap.forEach { responseData ->

                        if (comparisonData(
                                responseData.name, singleCard.cardEntity.name
                            )
                        )
                            this.indexOfFirst {
                                it.cardEntity.uniqueIdentifier ==
                                        singleCard.cardEntity.uniqueIdentifier
                            }.apply index@{
                                if (this != -1) {
                                    this@apply[0] =
                                        this@apply[this].also {
                                            this@apply[this] = this@apply[0]
                                        }

                                    observablePlaceList.add(
                                        CardWithResult(
                                            singleCard.cardEntity.uniqueIdentifier, responseData
                                        )
                                    )
                                }
                            }
                    }
                }

                if (this@LocationService.completionResponseMap.size != 0) {
                    observableCardList notifyByLocation this
                    showSuccessToast()
                } else
                    showFaultToast()

                this@LocationService.completionResponseMap.clear()
            }
        }
    }

    private fun showFaultToast() =
        messageAction?.invoke(R.string.NoGEOFind)

    private fun showSuccessToast() =
        messageAction?.invoke(R.string.ResultFindGEOFinal)
}