package com.example.mcard

import android.app.Application
import com.example.mcard.repository.di.AppComponent
import com.example.mcard.repository.di.DaggerAppComponent

internal class BasicApplication: Application()
{
    lateinit var modulesComponent: AppComponent

    override fun onCreate()
    {
        super.onCreate()

        this.modulesComponent =
            DaggerAppComponent
                .builder()
                .contextApplication(this)
                .build()
    }
}