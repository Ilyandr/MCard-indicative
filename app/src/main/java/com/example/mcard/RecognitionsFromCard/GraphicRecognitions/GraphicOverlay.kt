package com.example.mcard.RecognitionsFromCard.GraphicRecognitions

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.mcard.RecognitionsFromCard.GraphicRecognitions.GraphicOverlay.Graphic
import com.google.android.gms.vision.CameraSource

internal open class GraphicOverlay<T : Graphic?>(
    context: Context?, attrs: AttributeSet?) : View(context, attrs)
{
    private val lock = Any()
    private var previewWidth = 0
    private var widthScaleFactor = 1.0f
    private var previewHeight = 0
    private var heightScaleFactor = 1.0f
    private var facing = CameraSource.CAMERA_FACING_BACK
    private val graphics: MutableSet<T> = HashSet()

    abstract class Graphic(
        private val mOverlay: GraphicOverlay<*>)
    {
        abstract fun draw(canvas: Canvas?)
        abstract fun contains(x: Float, y: Float): Boolean
        fun scaleX(horizontal: Float) =
            horizontal * mOverlay.widthScaleFactor

        fun scaleY(vertical: Float) =
            vertical * mOverlay.heightScaleFactor

        fun translateX(x: Float) =
             if (mOverlay.facing == CameraSource.CAMERA_FACING_FRONT)
                 mOverlay.width - scaleX(x)
             else scaleX(x)

        fun translateY(y: Float) = scaleY(y)

        fun translateRect(inputRect: RectF): RectF
        {
            val returnRect = RectF()
            returnRect.left = translateX(inputRect.left)
            returnRect.top = translateY(inputRect.top)
            returnRect.right = translateX(inputRect.right)
            returnRect.bottom = translateY(inputRect.bottom)
            return returnRect
        }

        fun postInvalidate() = mOverlay.postInvalidate()
    }

    fun clear()
    {
        synchronized(lock) { graphics.clear() }
        postInvalidate()
    }

    fun add(graphic: T)
    {
        synchronized(lock) { graphics.add(graphic) }
        postInvalidate()
    }

    fun remove(graphic: T)
    {
        synchronized(lock) { graphics.remove(graphic) }
        postInvalidate()
    }

    fun getGraphicAtLocation(rawX: Float, rawY: Float): T?
    {
        synchronized(lock)
        {
            val location = IntArray(2)
            getLocationOnScreen(location)
            for (graphic in graphics)
                if (graphic!!.contains(
                        rawX - location[0], rawY - location[1]))
                    return graphic
            return null
        }
    }

    fun setCameraInfo(
        previewWidth: Int, previewHeight: Int, facing: Int)
    {
        synchronized(lock)
        {
            this.previewWidth = previewWidth
            this.previewHeight = previewHeight
            this.facing = facing
        }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas)
    {
        super.onDraw(canvas)
        synchronized(lock)
        {
            if (previewWidth != 0 && previewHeight != 0)
            {
                widthScaleFactor = (width.toFloat()
                        / previewWidth.toFloat())
                heightScaleFactor = (height.toFloat()
                        / previewHeight.toFloat())
            }

            for (graphic in graphics)
                graphic!!.draw(canvas)
        }
    }
}