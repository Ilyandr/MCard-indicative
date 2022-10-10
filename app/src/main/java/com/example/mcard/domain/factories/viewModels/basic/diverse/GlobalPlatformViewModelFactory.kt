@file:Suppress("UNCHECKED_CAST")

package com.example.mcard.domain.factories.viewModels.basic.diverse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mcard.domain.factories.other.PagerCardRepository
import com.example.mcard.domain.viewModels.basic.diverse.GlobalPlatformViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

internal class GlobalPlatformViewModelFactory @AssistedInject constructor(
    @Assisted(value = "api")
    val dataSource: PagerCardRepository,
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        if (modelClass.isAssignableFrom(
                GlobalPlatformViewModel::class.java
            )
        ) GlobalPlatformViewModel(this.dataSource) as T
        else
            throw IllegalArgumentException()
}