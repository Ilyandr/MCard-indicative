package com.example.mcard.repository.features.connection

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.animation.Animation
import androidx.appcompat.widget.AppCompatTextView
import com.example.mcard.repository.features.connection.NetworkListener.Companion.statusNetwork
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

@DelicateCoroutinesApi
internal class OptionsNetworkRules(
private val listButtons: MutableList<AppCompatTextView>
, private val animationAction: Animation
, private var haveOwnerCard: Boolean)
{
    private lateinit var service: ScheduledExecutorService
    private lateinit var pairButtonAndBoolValue: Pair<AppCompatTextView, Boolean>

    companion object
    {
        private val shadowColor = ColorStateList.valueOf(
            Color.parseColor("#807C7B7B"))

        private const val TIME_UPDATE: Long = 3
        private const val DURATION_TIME: Long = 200
    }

    init
    {
        this.animationAction.setAnimationListener(object: Animation.AnimationListener
        {
            override fun onAnimationEnd(p0: Animation?)
            {
                pairButtonAndBoolValue.first.backgroundTintList =
                    if (pairButtonAndBoolValue.second) null else shadowColor
            }

            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationRepeat(p0: Animation?) {}
        })
    }

    private var statusNetworkConnection: Boolean? by Delegates.observable(null)
    { _, oldValue, newValue ->
        for (i in listButtons.indices)
        {
            if ((oldValue != newValue) && (this.haveOwnerCard))
            {
                this.pairButtonAndBoolValue = Pair(listButtons[i], newValue!!)

                this.pairButtonAndBoolValue
                    .first
                    .startAnimation(this.animationAction)

                Thread.sleep(DURATION_TIME)
            }
        }
    }

    fun registerListenerNetworkChange()
    {
        this.service = Executors.newSingleThreadScheduledExecutor()
        this.service.scheduleWithFixedDelay(
            { this.statusNetworkConnection = statusNetwork }
            , 0
            , TIME_UPDATE
            , TimeUnit.SECONDS)

       GlobalScope.launch (Dispatchers.IO)
       {
           delay(1000)
           this@OptionsNetworkRules.statusNetworkConnection =
               statusNetwork
       }.start()
    }

    fun unRegisterListenerNetworkChange()
    {
        this.service.shutdown()
        this.service.shutdownNow()
        this.listButtons.clear()
    }

    fun updateDataOwner(dataOwnerCard: String?)
    { this.haveOwnerCard = (dataOwnerCard != null) }
}