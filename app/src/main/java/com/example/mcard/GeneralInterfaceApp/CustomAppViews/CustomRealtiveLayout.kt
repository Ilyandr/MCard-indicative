package com.example.mcard.GeneralInterfaceApp.CustomAppViews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.example.mcard.StorageAppActions.DataInterfaceCard

internal class CustomRealtiveLayout : RelativeLayout
{
    private lateinit var path: Path
    private lateinit var rect: RectF

    private var radius: Float = 20f
    var removeBorder = false

    private fun init()
    {
        this.path = Path()
        this.radius = DataInterfaceCard(context).actionRoundBorderCard()
    }

    constructor(context: Context?) : super(context!!) { init() }
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) { init() }
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context!!, attrs, defStyle) { init() }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas)
    {
        this.path.reset()
        this.rect = RectF(0f, 0f, this.width.toFloat(), this.height.toFloat())
        this.path.addRoundRect(rect, radius, radius, Path.Direction.CW)

        canvas.clipPath(path)
        super.onDraw(canvas)

        val paint = Paint()
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor(if (!this.removeBorder) "#4D676767" else "#00000000")
        canvas.drawPaint(paint)

        this.path.close()
    }

    fun setBorderRadius(newRadius: Float) { this.radius = newRadius }

    fun changeShadowCard(newRadius: Float = this.radius, init: Boolean = false)
    {
        this.setBorderRadius(newRadius)
        if (init) this.setBackgroundColor(Color.parseColor("#00000000"))
        this.invalidate()
    }
}