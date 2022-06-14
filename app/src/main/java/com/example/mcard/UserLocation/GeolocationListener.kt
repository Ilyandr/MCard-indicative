
package com.example.mcard.UserLocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.example.mcard.BasicAppActivity
import com.example.mcard.FunctionalInterfaces.DelegateVoidInterface
import com.example.mcard.R
import com.example.mcard.SideFunctionality.CustomAppDialog
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

@DelicateCoroutinesApi
internal class GeolocationListener(
 private val context: FragmentActivity
 , private val delegatePairActions: Pair<DelegateVoidInterface, Runnable>) : LocationListener
{
    private var dataLocation: Location? by Delegates.observable(null)
    { _, oldValue, newValue ->
        if (oldValue == null)
            GlobalScope.launch(Dispatchers.Main)
            {
                run {
                    delegatePairActions.first.delegateFunction(
                    newValue.getBasicUserLocation())
                }
            }.start()
    }

    @SuppressLint("MissingPermission")
    fun build()
    {
        if (!checkPermissionsGEOAction())
            return

        val locationController =
            (context.getSystemService(Context.LOCATION_SERVICE)
                    as LocationManager)

        if (!locationController.isProviderEnabled(
                LocationManager.GPS_PROVIDER))
            GlobalScope.launch(Dispatchers.Main)
            { delegatePairActions.second.run() }.start()
        else
            (context as? BasicAppActivity)
                ?.generalCardsListView
                ?.swipeRefreshLayout?.isRefreshing = true

            locationController
                .requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER
                    , 3000
                    , 10f
                    , this)
    }

    private fun Location?.getBasicUserLocation(): String? =
        this?.let { "${it.latitude},${it.longitude}" }

    private fun checkPermissionsGEOAction() =
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED)
        {
            (context as? BasicAppActivity)
                ?.generalCardsListView
                ?.offLoadingAnimations(false)

            CustomAppDialog(context)
                .buildEntityDialog(false)
                .setTitle("Предоставление разрешений")
                .setMessage(
                    R.string.messagePermissionUseGEO,
                    CustomAppDialog.DEFAULT_MESSAGE_SIZE)
                .setPositiveButton("Далее")
                {
                    ActivityCompat.requestPermissions(
                        context, arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                        , BasicAppActivity.PermissionCodes.ACCESS_LOCATION)
                }
                .setNegativeButton("Отмена", null)
                .show()
            false
        } else true

    override fun onProviderDisabled(provider: String)
    {
        (context as? BasicAppActivity)
            ?.generalCardsListView
            ?.swipeRefreshLayout?.isRefreshing = false

    }

    override fun onProviderEnabled(provider: String) {
        (context as? BasicAppActivity)
            ?.generalCardsListView
            ?.swipeRefreshLayout?.isRefreshing = true
    }

    override fun onLocationChanged(location: Location)
    { this.dataLocation = location }
}