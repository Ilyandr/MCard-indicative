package com.example.mcard.repository.features.connection

import android.view.View
import android.view.animation.Animation
import com.example.mcard.R
import com.example.mcard.presentation.views.other.showMessage
import com.example.mcard.repository.features.connection.NetworkListener.Companion.statusNetwork

internal inline fun View?.setOnClickListenerWithConnection(
    animation: Animation? = null,
    crossinline action: (View?) -> Unit,
) = this?.setOnClickListener {
    animation?.let { animation ->
        it.startAnimation(animation)
    }
    if (statusNetwork)
        action.invoke(this)
    else
        context showMessage R.string.offlineNetworkMSG
}