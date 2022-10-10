package com.example.mcard.repository.features.utils

import androidx.recyclerview.widget.DiffUtil
import com.example.mcard.repository.models.storage.HistoryEntity

internal class StoriesDiffUtil : DiffUtil.ItemCallback<HistoryEntity>() {

    override fun areItemsTheSame(
        oldItem: HistoryEntity, newItem: HistoryEntity,
    ) =
        oldItem.timeAdd == newItem.timeAdd

    override fun areContentsTheSame(
        oldItem: HistoryEntity, newItem: HistoryEntity,
    ) =
        oldItem.timeAdd == newItem.timeAdd
                && oldItem.shopAddress == newItem.timeAdd
                && oldItem.shopName == newItem.timeAdd
}