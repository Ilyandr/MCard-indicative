package com.example.mcard.presentation.views.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.LinearLayout

internal class CustomOverlay : LinearLayout {
    private var windowFrame: Bitmap? = null

    constructor(context: Context?) :
            super(context)

    constructor(context: Context?, attrs: AttributeSet?) :
            super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int,
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (windowFrame == null)
            createWindowFrame()

        canvas.drawBitmap(
            windowFrame ?: return, 0f, 0f, null
        )
    }

    override fun isEnabled() = false

    override fun isClickable() = false

    override fun isInEditMode() = true

    private fun createWindowFrame() {
        this.windowFrame =
            Bitmap.createBitmap(
                width, height, Bitmap.Config.ARGB_8888
            )

        windowFrame?.apply {
            Canvas(this).let { canvas ->
                Paint(Paint.ANTI_ALIAS_FLAG).let { paint ->
                    paint.color =
                        Color.argb(
                            150, 0, 0, 0
                        )

                    canvas.drawRect(
                        RectF(
                            0f, 0f, width.toFloat(), height.toFloat()
                        ), paint
                    )

                    paint.xfermode =
                        PorterDuffXfermode(
                            PorterDuff.Mode.SRC_OUT
                        )

                    RectF(
                        32f,
                        height.toFloat() * 0.65f,
                        width.toFloat() - 32,
                        (width - 32) / 1.5f
                    ).let { cardView ->
                        Path().let { roundPath ->
                            roundPath.addRoundRect(
                                cardView, 20f, 20f, Path.Direction.CW
                            )

                            canvas.clipPath(roundPath)
                            canvas.drawRect(cardView, paint)
                        }
                    }
                }
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        windowFrame = null
    }
}