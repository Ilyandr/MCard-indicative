package com.example.mcard.repository.di.modules

import android.content.Context
import android.view.animation.Animation
import androidx.room.Room
import com.example.mcard.presentation.adapters.StoriesAdapter
import com.example.mcard.presentation.views.observers.ObservableList
import com.example.mcard.repository.features.storage.database.QuestDatabase
import com.example.mcard.repository.models.location.CardWithResult
import com.example.mcard.repository.models.storage.CardWithHistoryEntity
import dagger.Module
import dagger.Provides
import dagger.Reusable
import java.text.SimpleDateFormat
import javax.inject.Named
import javax.inject.Singleton

@Module
internal class ModuleFeatures {
    @Provides
    @Singleton
    @Named("localeObservableList")
    fun provideLocaleObservableList():
            ObservableList<CardWithHistoryEntity> =
        ObservableList(
            mutableList = mutableListOf()
        )

    @Provides
    @Singleton
    @Named("placeObservableList")
    fun providePlaceSearchResponseObservableList():
            ObservableList<CardWithResult> =
        ObservableList(
            mutableList = mutableListOf()
        )

    @Provides
    @Reusable
    fun provideDateFormat() =
        SimpleDateFormat(
            "dd.MM - hh:mm"
        )

    @Provides
    fun provideStoriesAdapter(
        @Named("animationSelect") animationSelected: Animation,
    ) = StoriesAdapter(
        animationSelected
    )

    @Provides
    @Singleton
    fun provideHistoryDatabase(context: Context) =
        Room.databaseBuilder(
            context,
            QuestDatabase::class.java,
            "cards_database"
        ).build()
}