package com.example.mcard.repository.di.modules

import android.content.Context
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.mcard.R
import com.example.mcard.repository.features.payment.source.PaymentSource
import com.example.mcard.repository.features.rest.geolocation.api.LocationSource
import com.example.mcard.repository.features.utils.LOCATION_HOST
import com.example.mcard.repository.features.utils.PAYMENTS_HOST
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
internal class ModuleRest {

    @Provides
    fun provideNetworkRequest(): NetworkRequest =
        NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

    @Provides
    fun provideGeolocationRetrofitBuilder(): LocationSource =
        Retrofit.Builder()
            .baseUrl(LOCATION_HOST)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LocationSource::class.java)

    @Provides
    fun providePaymentRetrofitBuilder(): PaymentSource =
        Retrofit.Builder()
            .baseUrl(PAYMENTS_HOST)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PaymentSource::class.java)

    @Provides
    @Singleton
    fun provideFirebaseAuth() =
        FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseConnection() =
        FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideFirestore() =
        FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseReference(context: Context, dataBase: FirebaseDatabase) =
        dataBase.getReference(
            context.getString(
                R.string.TableNameFirebase
            )
        )
}