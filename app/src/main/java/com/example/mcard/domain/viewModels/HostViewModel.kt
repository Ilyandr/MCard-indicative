package com.example.mcard.domain.viewModels

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.example.mcard.repository.di.AppComponent
import com.example.mcard.repository.features.connection.NetworkListener
import com.example.mcard.repository.features.rest.commercial.ads.YandexMetrica
import com.example.mcard.repository.features.rest.firebase.FirebaseController
import com.example.mcard.repository.features.storage.preferences.UserPreferences
import com.example.mcard.repository.source.usage.EntranceUsageSource
import com.yandex.metrica.push.YandexMetricaPush
import javax.inject.Inject

internal class HostViewModel(appComponent: AppComponent) :
    ViewModel(), EntranceUsageSource {

    @Inject
    lateinit var networkListener: NetworkListener

    @Inject
    lateinit var firebaseController: FirebaseController

    @Inject
    override lateinit var userPreferences: UserPreferences

    init {
        appComponent inject this
        appComponent inject firebaseController
    }

    infix fun registerReceivers(
        activity: AppCompatActivity,
    ) {
        YandexMetrica().onReceive(
            activity, Intent(
                YandexMetricaPush.OPEN_DEFAULT_ACTIVITY_ACTION
            )
        )

        networkListener.enable(
            activity
        )
    }
}