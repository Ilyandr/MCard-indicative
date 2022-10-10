package com.example.mcard.domain.viewModels.basic.diverse

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.mcard.domain.factories.other.PagerCardRepository
import com.example.mcard.repository.features.SupportLiveData.set


internal class GlobalPlatformViewModel(
    val dataSource: PagerCardRepository,
) : ViewModel() {

    private val mutableLiveDataState =
        MutableLiveData<String?>(null)

    private var searchParams: String? = null

    val listData =
        mutableLiveDataState.switchMap { searchQuery ->
            dataSource
                .getSearchResult(searchQuery)
                .cachedIn(viewModelScope)
        }

    infix fun actionSearch(data: String?) {
        mutableLiveDataState.set(
            if (data?.isEmpty() == true)
                null
            else
                data
        )
    }
}