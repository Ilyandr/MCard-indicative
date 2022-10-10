package com.example.mcard.repository.di.modules

import android.content.Context
import android.net.NetworkRequest
import android.os.Bundle
import com.example.mcard.domain.factories.other.PagerCardRepository
import com.example.mcard.presentation.views.observers.ObservableList
import com.example.mcard.repository.features.location.dataSource.LocationService
import com.example.mcard.repository.features.connection.NetworkListener
import com.example.mcard.repository.features.payment.dataSource.PaymentDataSource
import com.example.mcard.repository.features.payment.source.PaymentSource
import com.example.mcard.repository.features.rest.firebase.*
import com.example.mcard.repository.features.rest.firebase.AuthorizationFirebase
import com.example.mcard.repository.features.rest.firebase.AdditionAuthFirebase
import com.example.mcard.repository.features.rest.geolocation.source.GeolocationControllerDataSource
import com.example.mcard.repository.features.rest.geolocation.source.GeolocationControllerSourse
import com.example.mcard.repository.features.rest.geolocation.api.LocationSource
import com.example.mcard.repository.features.storage.database.QuestDatabase
import com.example.mcard.repository.models.storage.CardWithHistoryEntity
import com.example.mcard.repository.features.storage.database.source.QuestLocaleDataSource
import com.example.mcard.repository.features.storage.database.source.RoomQuestLocaleDataSource
import com.example.mcard.repository.features.storage.preferences.UserPreferences
import com.example.mcard.repository.models.location.CardWithResult
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
internal class ModuleServices {
    @Provides
    @Singleton
    fun provideSharedPreferencesManager(context: Context) =
        UserPreferences(context)

    @Provides
    fun provideAuthorizationManagerFB(firebaseController: FirebaseController) =
        AuthorizationFirebase(firebaseController)

    @Singleton
    @Provides
    fun provideBasicManagerFB(firebaseController: FirebaseController) =
        AdditionAuthFirebase(firebaseController)

    @Provides
    fun provideGlobalManagerFB(firebaseController: FirebaseController) =
        GlobalFirebase(firebaseController)

    @Provides
    fun providePersonalManagerFB(firebaseController: FirebaseController) =
        PersonalFirebase(firebaseController)

    @Singleton
    @Provides
    fun provideCardsManagerFB(firebaseController: FirebaseController) =
        CaretakerCardsFirebase(firebaseController)

    @Provides
    @Singleton
    fun provideNetworkListener(request: NetworkRequest) =
        NetworkListener(request)

    @Provides
    fun providePaymentDataSource(source: PaymentSource) =
        PaymentDataSource(source)

    @Singleton
    @Provides
    fun provideFirebaseController(context: Context) =
        FirebaseController(context)

    @Provides
    fun provideEmptyArgs() = Bundle()

    @Provides
    @Singleton
    fun provideRoomQuestLocaleDataSource(
        roomDatabase: QuestDatabase,
    ): QuestLocaleDataSource =
        RoomQuestLocaleDataSource(roomDatabase)

    @Provides
    fun provideGeolocationController(
        locationService: LocationService,
        api: LocationSource,
    ): GeolocationControllerSourse =
        GeolocationControllerDataSource(
            locationService, api
        )

    @Provides
    fun provideGeolocationService(
        @Named("localeObservableList")
        observableList: ObservableList<CardWithHistoryEntity>,
        @Named("placeObservableList")
        observablePlaceList: ObservableList<CardWithResult>,
    ) =
        LocationService(
            observableList, observablePlaceList
        )

    @Provides
    @Singleton
    fun providePagerCardRepository() =
        PagerCardRepository()
}