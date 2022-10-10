package com.example.mcard.repository.source.architecture.viewModels
import androidx.lifecycle.ViewModel

internal abstract class LiveViewModel <T>
    : ViewModel(), InteractionViewModel<T>
