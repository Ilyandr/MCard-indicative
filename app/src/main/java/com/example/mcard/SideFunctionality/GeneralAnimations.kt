package com.example.mcard.SideFunctionality

import android.animation.*
import android.os.Handler
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ListView
import com.example.mcard.BasicAppActivity.animationsWithCards

internal class GeneralAnimations(private val options1: Float
 , private val options2: Float
 , private val options3: Float
 , private val durationTopY: Long
 , private val durationRotationMS: Long)
{
    private lateinit var topY: Animator
    private lateinit var animSet: AnimatorSet
    private lateinit var animZ: AnimatorSet
    private lateinit var viewNow: View

    private var viewSecondR: View? = null
    private var startY = 1f
    private var actionResolution = true

    private lateinit var rotationStage1: ObjectAnimator
    private lateinit var rotationStage2: ObjectAnimator
    private lateinit var rotationStage3: ObjectAnimator

    fun liftingCard(view: View?
      , viewSecond: View? = null
      , actionAfter: Runnable?
      , moveY: Float?)
    {
        if (!animationsWithCards)
        {
            actionAfter?.run()
            return
        }
        setActionResolution(newValue = false)

        view?.let {
            this.startY = viewSecond?.y ?: it.y
            this.viewNow = it

            if (viewSecond != null)
                this.viewSecondR = viewSecond
        }

         this.topY = if (this.viewSecondR != null)
             ObjectAnimator.ofFloat(this.viewSecondR, View.Y
             , if (moveY != null) moveY / 1.45f else startY)
         else ObjectAnimator.ofFloat((view ?: this.viewNow), View.Y
             , if (view == null) startY else moveY!!)

        this.rotationStage1 = ObjectAnimator.ofFloat(
            view ?: this.viewNow, View.SCALE_Y, options1, options2)
        this.rotationStage2 = ObjectAnimator.ofFloat(
            view ?: this.viewNow, View.SCALE_Y, options2,  options3)
        this.rotationStage3 = ObjectAnimator.ofFloat(
            view ?: this.viewNow, View.SCALE_Y, options3,  options1)

        this.rotationStage1.setDuration(durationRotationMS).interpolator =
            AccelerateDecelerateInterpolator()
        this.rotationStage2.setDuration(durationRotationMS).interpolator =
            AccelerateDecelerateInterpolator()
        this.rotationStage3.setDuration(durationRotationMS).interpolator =
            AccelerateDecelerateInterpolator()

        this.animSet = AnimatorSet()
        this.animSet.setDuration(durationTopY).interpolator =
            AccelerateDecelerateInterpolator()
        this.animSet.playTogether(topY)
        this.animSet.addListener(object : AnimatorListenerAdapter()
        {
            override fun onAnimationStart(animation: Animator?)
            {
                super.onAnimationStart(animation)
                rotationStage1.addListener(object : AnimatorListenerAdapter()
                {
                    override fun onAnimationEnd(animation: Animator?)
                    {
                        super.onAnimationEnd(animation)
                        rotationStage2.addListener(object : AnimatorListenerAdapter()
                        {
                            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean)
                            {
                                super.onAnimationEnd(animation, isReverse)

                                rotationStage3.addListener(object: AnimatorListenerAdapter()
                                {
                                    override fun onAnimationEnd(animation: Animator?)
                                    {
                                        super.onAnimationEnd(animation)
                                        setActionResolution(newValue = true)
                                        actionAfter?.run()
                                    }
                                })
                                rotationStage3.start()
                            }
                        })
                        rotationStage2.start()
                    }
                })
                rotationStage1.start()
            }

            private fun removeAllListeners()
            {
                rotationStage1.removeAllListeners()
                rotationStage2.removeAllListeners()
                rotationStage3.removeAllListeners()

                animSet.cancel()
                animSet.removeAllListeners()
                topY.removeAllListeners()

                if (moveY == null && viewSecondR != null)
                    viewSecondR = null
            }

            override fun onAnimationEnd(animation: Animator?)
            {
                super.onAnimationEnd(animation)
                removeAllListeners()
            }
        })
        animSet.start()
    }

    @Suppress("DEPRECATION")
    fun setCardList(listView: ListView)
    {
        if (listView.count >= 1)
        {
            setActionResolution(newValue = false)

            Handler().postDelayed(
                {
                    val viewPosition = FloatArray(
                        listView.lastVisiblePosition + 1)
                    animSet = AnimatorSet()
                    animSet.setDuration(800).interpolator =
                        AccelerateDecelerateInterpolator()

                    for (i in viewPosition.indices)
                    {
                        viewPosition[i] = listView.getChildAt(i).y
                        listView.getChildAt(i).y = 1f
                    }

                    listView.scaleX = 1f
                    for (i in viewPosition.indices)
                    {
                        topY = ObjectAnimator.ofFloat(listView.getChildAt(i)
                            , View.Y
                            , viewPosition[i])

                        animSet.playTogether(topY)
                        animSet.addListener(object: AnimatorListenerAdapter()
                        {
                            override fun onAnimationEnd(animation: Animator?)
                            {
                                super.onAnimationEnd(animation)
                                setActionResolution(newValue = true)
                            }
                        })
                        animSet.start()
                    }
                }, 500)
        }
    }

    fun getActionResolution() =
        this.actionResolution
    fun setActionResolution(newValue: Boolean)
    { this.actionResolution = newValue }
}