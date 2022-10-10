@file:Suppress("UNCHECKED_CAST")

package com.example.mcard.domain.factories.viewModels.features

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mcard.domain.viewModels.features.AdditionallyViewModel
import com.example.mcard.repository.di.AppComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

internal class AdditionallyViewModelFactory @AssistedInject constructor(
    @Assisted(value = "component")
    val appComponent: AppComponent,
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        if (modelClass.isAssignableFrom(
                AdditionallyViewModel::class.java
            )
        ) AdditionallyViewModel(this.appComponent) as T
        else
            throw IllegalArgumentException()
}