package com.example.mcard.SideFunctionality

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.example.mcard.GeneralInterfaceApp.MasterDesignCard
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.RoundRectCornerImageView
import com.example.mcard.R
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomRealtiveLayout
import jp.wasabeef.blurry.Blurry
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
internal class RotationCardMaster(
    private val fragmentActivity: FragmentActivity? = null
   , private val activity: AppCompatActivity? = null
   , val masterDesignCard: MasterDesignCard
   , private val designCard: RoundRectCornerImageView
   , private val cardSize: CustomRealtiveLayout
   , private val layoutAnimation: RelativeLayout
   , val changeNameCardET: Pair<TextView, Boolean>
   , private val numberCardET: TextView
   , var colorCard: Int)
{
    private val animationEmergence: Animation
    private val animationHide: Animation
    private val displayParams: WindowManager.LayoutParams?

    private lateinit var firstAnimator: ObjectAnimator
    private lateinit var secondAnimator: ObjectAnimator
    private var startAction = true

    companion object
    {
        @JvmStatic
        var rotationCardActive = false
        @JvmStatic
        var prevDisplayBright: Float? = 0.0f
    }

    init
    {
        this.animationEmergence = AnimationUtils.loadAnimation(
            contextInfo(), R.anim.to_text_light)
        this.animationHide = AnimationUtils.loadAnimation(
            contextInfo(), R.anim.anim_hide)

        this.displayParams = contextInfo()
            ?.window?.attributes
        prevDisplayBright = this.displayParams
            ?.screenBrightness
    }

    private fun rotationInit()
    {
        this.firstAnimator = ObjectAnimator.ofFloat(
            this.designCard, "scaleX", if (rotationCardActive) -1f else 1f, 0f)
        this.secondAnimator = ObjectAnimator.ofFloat(
            this.designCard, "scaleX", 0f, if (!rotationCardActive) -1f else 1f)

        this.firstAnimator.interpolator = DecelerateInterpolator()
        this.secondAnimator.interpolator = AccelerateDecelerateInterpolator()

        this.firstAnimator.duration = 250
        this.secondAnimator.duration = 250

        this.animationEmergence.duration = 30
        this.animationHide.duration = 30

        this.firstAnimator.addListener(object : AnimatorListenerAdapter()
        {
            override fun onAnimationStart(animation: Animator)
            {
                super.onAnimationStart(animation)

                this@RotationCardMaster.cardSize.removeBorder = true
                this@RotationCardMaster.cardSize.changeShadowCard()

                if (!rotationCardActive)
                {
                    if (changeNameCardET.second)
                        changeNameCardET.first.startAnimation(animationHide)
                    numberCardET.startAnimation(animationHide)

                    animationHide.setAnimationListener(object : Animation.AnimationListener
                    {
                        override fun onAnimationStart(animation: Animation)
                        { recognitionObjectsOption(0f) }
                        override fun onAnimationEnd(animation: Animation) {}
                        override fun onAnimationRepeat(animation: Animation) {}
                    })
                } else layoutAnimation.scaleX = 0f
            }

            override fun onAnimationEnd(animation: Animator)
            {
                super.onAnimationEnd(animation)
                if (!rotationCardActive) rotationViewBack()
                else rotationViewForward()

                secondAnimator.addListener(object : AnimatorListenerAdapter()
                {
                    override fun onAnimationEnd(animation: Animator)
                    {
                        super.onAnimationEnd(animation)
                        if (rotationCardActive)
                        {
                            if (changeNameCardET.second)
                                changeNameCardET.first.startAnimation(
                                    animationEmergence)
                            numberCardET.startAnimation(
                                animationEmergence)

                            animationEmergence.setAnimationListener(object : Animation.AnimationListener
                            {
                                override fun onAnimationStart(animation: Animation)
                                { recognitionObjectsOption(1f) }
                                override fun onAnimationEnd(animation: Animation) {}
                                override fun onAnimationRepeat(animation: Animation) {}
                            })
                        } else layoutAnimation.scaleX = 1f

                        this@RotationCardMaster.cardSize
                            .removeBorder = false
                        this@RotationCardMaster.cardSize
                            .changeShadowCard()

                        displayBright()
                        rotationClearRAM()

                        rotationCardActive = !rotationCardActive
                        startAction = !startAction
                    }
                })
                secondAnimator.start()
            }

            private fun rotationViewBack()
            {
                Blurry.with(contextInfo())
                    .capture(designCard)
                    .into(designCard)
            }

            private fun rotationViewForward()
            {
               this@RotationCardMaster.masterDesignCard.setCardDesignLocale(
                   changeNameCardET.first.text.toString()
                   , colorCard
                   , designCard)
            }

            private fun recognitionObjectsOption(settingSecond: Float)
            {
                if (changeNameCardET.second)
                    changeNameCardET.first.scaleY = settingSecond
                numberCardET.scaleY = settingSecond
            }

            private fun displayBright()
            {
                displayParams?.screenBrightness =
                    if (rotationCardActive) prevDisplayBright
                    else 1f

                contextInfo()?.window?.attributes =
                    displayParams
            }
        })
    }

    fun rotationAction()
    {
        if (startAction)
        {
            startAction = !startAction
            rotationInit()

            this.firstAnimator.start()
            this.cardSize.setBackgroundResource(R.color.prism)
        }
    }

    fun rotationClearRAM()
    {
        try
        {
            this.firstAnimator.removeAllListeners()
            this.secondAnimator.removeAllListeners()
        } catch (ignored: UninitializedPropertyAccessException) { return }
    }

    private fun contextInfo() =
        (fragmentActivity ?: activity)
}