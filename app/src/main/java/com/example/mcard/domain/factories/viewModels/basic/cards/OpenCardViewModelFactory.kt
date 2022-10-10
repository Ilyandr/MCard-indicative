@file:Suppress("UNCHECKED_CAST")

package com.example.mcard.domain.factories.basic.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mcard.domain.viewModels.basic.cards.OpenCardViewModel
import com.example.mcard.repository.di.AppComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

internal class OpenCardViewModelFactory @AssistedInject constructor(
    @Assisted(value = "component")
    private val appComponent: AppComponent,
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        if (modelClass.isAssignableFrom(
                OpenCardViewModel::class.java
            )
        ) OpenCardViewModel(this.appComponent) as T
        else
            throw IllegalArgumentException()
}