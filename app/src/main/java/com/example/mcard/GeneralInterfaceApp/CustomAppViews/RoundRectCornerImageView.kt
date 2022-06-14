package com.example.mcard.GeneralInterfaceApp.CustomAppViews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
open class RoundRectCornerImageView : AppCompatImageView
{
    protected lateinit var path: Path
    protected lateinit var rect: RectF
    protected var radius: Float = 20.0f
    private var resourseId: Int? = null

    constructor(context: Context?) : super(context!!) { init() }
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) { init() }
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context!!, attrs, defStyle) { init() }

    private fun init() { this.path = Path() }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas)
    {
        this.path.reset()
        this.rect = RectF(0f, 0f, this.width.toFloat(), this.height.toFloat())
        this.path.addRoundRect(rect, radius, radius, Path.Direction.CW)

        canvas.clipPath(path)
        super.onDraw(canvas)

        if (this.resourseId == null
            && this.scaleType != ScaleType.FIT_CENTER
            && this.radius != 0f)
        {
            val paint = Paint()
            paint.style = Paint.Style.STROKE
            paint.color = Color.parseColor("#000000")
            paint.strokeWidth = 2.0f
            canvas.drawPath(path, paint)
        }
        this.radius = 20f
        this.path.close()
    }

    fun setBorderRadius(newRadius: Float) { this.radius = newRadius }

    override fun setBackgroundResource(resId: Int)
    {
        this.resourseId = resId
        super.setBackgroundResource(resId)
    }
}