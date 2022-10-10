package com.example.mcard.repository.source.architecture.viewModels

import androidx.lifecycle.LifecycleOwner

internal interface InteractionView
{
    fun registrationOfInteraction(
        lifecycleOwner: LifecycleOwner
    ): Any?
}