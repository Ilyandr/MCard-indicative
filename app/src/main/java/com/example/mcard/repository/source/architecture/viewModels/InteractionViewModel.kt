package com.example.mcard.repository.source.architecture.viewModels

import androidx.lifecycle.LiveData

internal interface InteractionViewModel <LiveDataType>: InteractionView
{
    val liveDataState: LiveData<LiveDataType>
    fun actionСompleted()
}