package com.example.mcard.repository.features.connection

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkRequest

internal class NetworkListener(
    private val networkRequest: NetworkRequest,
) : NetworkCallback() {

    fun enable(context: Context) =
        (context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager).registerNetworkCallback(
            networkRequest, this
        )

    override fun onLost(network: Network) {
        super.onLost(network)
        statusNetwork = false
    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        statusNetwork = true
    }

    companion object {
        var statusNetwork: Boolean = false
    }
}
