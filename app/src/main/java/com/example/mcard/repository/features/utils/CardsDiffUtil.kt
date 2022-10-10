package com.example.mcard.repository.features.utils

import androidx.recyclerview.widget.DiffUtil
import com.example.mcard.repository.models.storage.CardEntity
import com.example.mcard.repository.models.storage.CardWithHistoryEntity

internal class CardsDiffUtil(
    private val oldDataList: List<CardWithHistoryEntity>,
    private val newDataList: List<CardWithHistoryEntity>,
) : DiffUtil.Callback() {

    override fun getOldListSize() =
        this.oldDataList.size

    override fun getNewListSize() =
        this.newDataList.size

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int,
    ) =
        oldDataList[oldItemPosition].cardEntity.uniqueIdentifier ==
                newDataList[newItemPosition].cardEntity.uniqueIdentifier

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldDataList[oldItemPosition].cardEntity.run oldData@{

            newDataList[newItemPosition].cardEntity.run newData@{

                when {
                    this@oldData.uniqueIdentifier != this@newData.uniqueIdentifier
                            || this@oldData.barcode != this@newData.barcode
                            || this@oldData.name != this@newData.name
                            || this@oldData.number != this@newData.number
                            || this@oldData.color != this@newData.color
                            || this@oldData.cardOwner != this@newData.cardOwner
                            || this@oldData.dateAddCard != this@newData.dateAddCard -> false

                    else -> true
                }
            }
        }

    companion object {
        val cardsDiffCallback = object : DiffUtil.ItemCallback<CardEntity>() {
            override fun areItemsTheSame(
                oldItem: CardEntity,
                newItem: CardEntity,
            ) =
                oldItem.uniqueIdentifier ==
                        newItem.uniqueIdentifier

            override fun areContentsTheSame(
                oldItem: CardEntity,
                newItem: CardEntity,
            ) =
                when {
                    oldItem.uniqueIdentifier == newItem.uniqueIdentifier
                            && oldItem.barcode == newItem.barcode
                            && oldItem.name == newItem.name
                            && oldItem.number == newItem.number
                            && oldItem.color == newItem.color
                            && oldItem.cardOwner == newItem.cardOwner
                            && oldItem.dateAddCard == newItem.dateAddCard -> true
                    else -> false
                }
        }
    }
}