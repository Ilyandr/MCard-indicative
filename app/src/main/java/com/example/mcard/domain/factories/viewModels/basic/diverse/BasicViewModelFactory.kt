@file:Suppress("UNCHECKED_CAST")

package com.example.mcard.domain.factories.viewModels.basic.diverse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mcard.domain.viewModels.basic.diverse.BasicViewModel
import com.example.mcard.repository.di.AppComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

internal class BasicViewModelFactory @AssistedInject constructor(
    @Assisted(value = "component")
    val appComponent: AppComponent,
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        if (modelClass.isAssignableFrom(
                BasicViewModel::class.java
            )
        ) BasicViewModel(this.appComponent) as T
        else
            throw IllegalArgumentException()
}