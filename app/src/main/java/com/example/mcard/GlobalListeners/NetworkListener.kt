package com.example.mcard.GlobalListeners

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import androidx.core.net.toUri
import com.example.mcard.GroupServerActions.GlobalDataFBManager
import com.example.mcard.StorageAppActions.SharedPreferencesManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

@DelicateCoroutinesApi
internal class NetworkListener(
    private val globalDataFBManager: GlobalDataFBManager? = null): BroadcastReceiver()
{
    private var context: Context? = null

    companion object
    {
        private var statusNetwork: Boolean? = null
        @JvmStatic fun getStatusNetwork() =
            this.statusNetwork ?: false
    }

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?)
    {
        if (context == null)
            return
        this.context = context

        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                (context.getSystemService(Service.CONNECTIVITY_SERVICE) as? ConnectivityManager)
                        ?.activeNetwork == null
            else !(context.getSystemService(Service.CONNECTIVITY_SERVICE) as? ConnectivityManager)
                ?.isActiveNetworkMetered!!)
            statusNetwork = false
        else checkingAccess()
    }

    private fun checkingAccess() = GlobalScope.launch(Dispatchers.IO)
    {
        try
        {
            val urlConnection = URL("https://www.google.com")
                .openConnection() as? HttpURLConnection
                ?: return@launch
            urlConnection.setRequestProperty("User-Agent", "Test")
            urlConnection.setRequestProperty("Connection", "close")

            urlConnection.connectTimeout = 1500
            urlConnection.connect()
            statusNetwork = (urlConnection.responseCode == 200)

            if (statusNetwork ?: return@launch
                && context != null)
            {
                val sharedPreferencesManager = SharedPreferencesManager(context!!)
                val dataUpdateGlobalPhoto = sharedPreferencesManager
                    .push_data_profile(null)

                if (sharedPreferencesManager.updateUserLocaleNickname(null))
                    sharedPreferencesManager.nameUserProfile(null)?.let {
                        GlobalDataFBManager(context)
                            .updateLocaleUserNickname(it)
                    }

                if (dataUpdateGlobalPhoto != "" && getStatusNetwork())
                    this@NetworkListener.context?.let {
                        GlobalDataFBManager(it)
                            .loadPhotoProfileToFB(
                                null, dataUpdateGlobalPhoto.toUri())
                        sharedPreferencesManager.push_data_profile("")
                    }
            }
        }
        catch (e: Exception)
        {  statusNetwork = false; }
    }
}