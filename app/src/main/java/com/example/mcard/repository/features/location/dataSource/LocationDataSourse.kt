@file:Suppress("DEPRECATION")

package com.example.mcard.repository.features.location.dataSource

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.mcard.R
import com.example.mcard.repository.features.rest.geolocation.source.GeolocationControllerSourse
import com.example.mcard.repository.features.checkPermissions
import com.example.mcard.repository.features.getModuleAppComponent
import com.example.mcard.repository.features.location.source.LocationSourse
import com.example.mcard.repository.features.messageAction
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.yoomoney.sdk.signInApi.error.PermissionErrorException
import java.util.concurrent.TimeUnit
import javax.inject.Inject


internal class LocationDataSourse(
    context: Context,
    private val messageAction: messageAction,
) : LocationSourse {

    private var changeLocation: Location? = null

    @Inject
    lateinit var geolocationControllerSourse: GeolocationControllerSourse

    private val fusedLocationProviderClient: FusedLocationProviderClient by lazy {
        FusedLocationProviderClient(context)
    }

    private val locationCallback: LocationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.apply {
                    changeLocation = this
                    geolocationIsDefined(latitude, longitude)
                } ?: errorLocationCallback()
            }
        }
    }

    init {
        (context as? FragmentActivity)
            ?.getModuleAppComponent()
            ?.inject(this)
    }

    private fun requiredPermissions() =
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    override fun runWithGrantedAccess() =
        try {
            CoroutineScope(Dispatchers.IO).launch {
                delay(10000)
                withContext(Dispatchers.Main) {
                    if (changeLocation == null)
                        fusedLocationProviderClient.flushLocations()
                }
            }

            fusedLocationProviderClient.requestLocationUpdates(
                requiredLocationRequest(),
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (ex: PermissionErrorException) {
            warningOfLackOfAccess()
        }


    override fun requireSecureFacilityListener(fragment: Fragment) =
        SecureFacilityListener(fragment)

    private fun warningOfLackOfAccess() =
        this.messageAction.invoke(
            R.string.permissionError
        )

    private fun requiredLocationRequest() =
        LocationRequest
            .create()
            .apply {
                interval = TimeUnit.MILLISECONDS.toMillis(500)
                fastestInterval = TimeUnit.MILLISECONDS.toMillis(250)
                maxWaitTime = TimeUnit.SECONDS.toMillis(7)
                priority = Priority.PRIORITY_HIGH_ACCURACY
            }

    private fun errorLocationCallback() =
        this.messageAction.invoke(
            R.string.msgErrorLocation
        )

    private fun geolocationIsDefined(
        latitude: Double, longitude: Double,
    ) =
        fusedLocationProviderClient.removeLocationUpdates(
            locationCallback
        ).addOnCompleteListener {

            changeLocation = null
            geolocationControllerSourse.launch(
                messageAction, "$latitude,$longitude"
            )
        }

    inner class SecureFacilityListener(fragment: Fragment) {
        private val permissionsLauncher =
            fragment.checkPermissions(
                faultState = ::warningOfLackOfAccess,
            ) {
                fragment.locationEnabledResult().setResultCallback { resultCallback ->

                    resultCallback.status.apply {

                        when (status.statusCode) {
                            LocationSettingsStatusCodes.SUCCESS ->
                                runWithGrantedAccess()

                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                                resolutionForResult.launch(this.resolution?.let {
                                    IntentSenderRequest.Builder(it).build()
                                })
                        }
                    }
                }
            }

        private val resolutionForResult =
            fragment.registerForActivityResult(
                ActivityResultContracts.StartIntentSenderForResult()
            ) { activityResult ->
                if (activityResult.resultCode == RESULT_OK)
                    runWithGrantedAccess()
                else
                    warningOfLackOfAccess()
            }

        private fun Fragment.locationEnabledResult() =
            LocationServices
                .SettingsApi
                .checkLocationSettings(
                    GoogleApiClient.Builder(
                        requireActivity()
                    ).addApi(
                        LocationServices.API
                    ).build().apply {
                        connect()
                    },
                    LocationSettingsRequest.Builder().addLocationRequest(
                        requiredLocationRequest()
                    ).apply {
                        setAlwaysShow(true)
                    }.build()
                )

        fun launch() =
            permissionsLauncher.launch(
                requiredPermissions()
            )
    }
}