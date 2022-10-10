package com.example.mcard.presentation.views.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

open class RoundView : AppCompatImageView {
    protected lateinit var path: Path
    protected lateinit var rect: RectF
    protected var radius: Float = 0f
    private var resourseId: Int? = null

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context!!,
        attrs,
        defStyle) {
        init()
    }

    private fun init() {
        this.path = Path()
    }

    override fun onDraw(canvas: Canvas) {
        this.path.reset()
        this.rect = RectF(
            0f, 0f, this.width.toFloat(), this.width.toFloat() / 1.5f
        )

        this.path.addRoundRect(
            rect, radius, radius, Path.Direction.CW
        )

        canvas.clipPath(path)
        super.onDraw(canvas)
        this.path.close()
    }

    fun setBorderRadius(newRadius: Float) {
        this.radius = newRadius
    }

    override fun setBackgroundResource(resId: Int) {
        this.resourseId = resId
        super.setBackgroundResource(resId)
    }
}