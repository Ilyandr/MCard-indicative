package com.example.mcard.RecognitionsFromCard.GraphicRecognitions;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.text.TextBlock;

public final class CustomGraphicOverlay extends GraphicOverlay.Graphic
{
    private final Paint rectPaint;
    private TextBlock textBlock;
    private Barcode barcodeBlock;

    public CustomGraphicOverlay(
            @NonNull GraphicOverlay mOverlay, @NonNull TextBlock textBlock)
    {
        super(mOverlay);

        this.textBlock = textBlock;

        this.rectPaint = new Paint();
        this.rectPaint.setColor(Color.WHITE);
        this.rectPaint.setStyle(Paint.Style.STROKE);
        this.rectPaint.setStrokeWidth(4.0f);

        postInvalidate();
    }

    public CustomGraphicOverlay(
            @NonNull GraphicOverlay mOverlay, @NonNull Barcode barcodeBlock)
    {
        super(mOverlay);

        this.barcodeBlock = barcodeBlock;

        this.rectPaint = new Paint();
        this.rectPaint.setColor(Color.WHITE);
        this.rectPaint.setStyle(Paint.Style.STROKE);
        this.rectPaint.setStrokeWidth(4.0f);

        postInvalidate();
    }

    public boolean contains(float x, float y)
    {
        if (textBlock == null)
            return false;

        RectF rect = new RectF(textBlock.getBoundingBox());
        rect = translateRect(rect);
        return rect.contains(x, y);
    }

    @Override
    public void draw(@Nullable Canvas canvas)
    {
        RectF detectedRect = new RectF( textBlock != null ?
                textBlock.getBoundingBox()
                : barcodeBlock.getBoundingBox());

        detectedRect = translateRect(detectedRect);
        canvas.drawRect(
                detectedRect, rectPaint);
    }
}

