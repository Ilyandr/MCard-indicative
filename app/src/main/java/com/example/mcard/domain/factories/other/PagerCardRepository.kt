package com.example.mcard.domain.factories.other

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import com.example.mcard.repository.features.paging.GlobalCardsPagingSource

internal class PagerCardRepository {

    fun getSearchResult(data: String?) =
        Pager(
            PagingConfig(
                pageSize = 1,
                enablePlaceholders = true
            )
        ) {
            GlobalCardsPagingSource(
                data
            )
        }.liveData
}