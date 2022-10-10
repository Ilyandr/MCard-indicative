package com.example.mcard.repository.features.rest.commercial.ads

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mcard.R
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import com.yandex.metrica.push.YandexMetricaPush

internal class YandexMetrica: BroadcastReceiver()
{
    override fun onReceive(applicationContext: Context?, intent: Intent?)
    {
        applicationContext?.let {
            YandexMetrica.activate(
                it, YandexMetricaConfig.newConfigBuilder(
                    it.getString(R.string.YANDEX_METRICA_API_KEY)).withLogs()
                    .build()).apply { YandexMetricaPush.init(it) }
        }
    }
}