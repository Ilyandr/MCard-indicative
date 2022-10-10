package com.example.mcard.domain.factories.viewModels

import com.example.mcard.domain.factories.basic.cards.adding.ManuallyCardAddViewModelFactory
import com.example.mcard.domain.factories.basic.cards.OpenCardViewModelFactory
import com.example.mcard.domain.factories.viewModels.basic.diverse.BasicViewModelFactory
import com.example.mcard.domain.factories.basic.HostViewModelFactory
import com.example.mcard.domain.factories.viewModels.basic.diverse.GlobalPlatformViewModelFactory
import com.example.mcard.domain.factories.other.PagerCardRepository
import com.example.mcard.domain.factories.viewModels.basic.diverse.SettingsViewModelFactory
import com.example.mcard.domain.factories.viewModels.features.AdditionallyViewModelFactory
import com.example.mcard.repository.di.AppComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory

@AssistedFactory
internal interface SupportBasicViewModelFactory {
    infix fun create(
        @Assisted(value = "component")
        appComponent: AppComponent,
    ): BasicViewModelFactory
}

@AssistedFactory
internal interface SupportHostViewModelFactory {
    infix fun create(
        @Assisted(value = "component")
        appComponent: AppComponent,
    ): HostViewModelFactory
}

@AssistedFactory
internal interface SupportManuallyCardAddViewModelFactory {
    infix fun create(
        @Assisted(value = "component")
        appComponent: AppComponent,
    ): ManuallyCardAddViewModelFactory
}

@AssistedFactory
internal interface SupportOpenCardViewModelFactory {
    infix fun create(
        @Assisted(value = "component")
        appComponent: AppComponent,
    ): OpenCardViewModelFactory
}

@AssistedFactory
internal interface SupportSettingsViewModelFactory {
    infix fun create(
        @Assisted(value = "component")
        appComponent: AppComponent,
    ): SettingsViewModelFactory
}

@AssistedFactory
internal interface SupportAdditionallyViewModelFactory {
    infix fun create(
        @Assisted(value = "component")
        appComponent: AppComponent,
    ): AdditionallyViewModelFactory
}

@AssistedFactory
internal interface SupportGlobalPlatformViewModelFactory {
    infix fun create(
        @Assisted(value = "api")
        dataSource: PagerCardRepository,
    ): GlobalPlatformViewModelFactory
}