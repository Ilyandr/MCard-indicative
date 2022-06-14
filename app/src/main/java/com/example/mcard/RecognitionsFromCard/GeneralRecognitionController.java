package com.example.mcard.RecognitionsFromCard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.Animation;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.example.mcard.R;
import com.example.mcard.RecognitionsFromCard.GraphicRecognitions.CustomCameraFlashMode;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.text.TextRecognizer;
import java.io.IOException;

import kotlin.Pair;

public final class GeneralRecognitionController extends CustomCameraFlashMode
 implements SurfaceHolder.Callback, CameraSource.PictureCallback
{
    public CameraSource cameraSource;
    private final SurfaceView surfaceView;
    private final Runnable emergencyActionInCaseOfAnError;
    private final AppCompatButton btnColorCard;
    private AppCompatButton btnControlFlashMode;

    private final TextRecognizer textRecognizer;
    private final BarcodeDetector barcodeDetector;
    private final ImageColourManager imageColourManager;
    private final Runnable actionForFinalStage;

    private int color = Color.parseColor("#FFFFFF");
    public static final short ACTION_RECONGNITION_TEXT = 260;
    public static final short ACTION_RECOGNITION_BARCODE = -261;
    public static final short ACTION_RECOGNITION_COLOR = 521;
    public static final short ACTION_RECOGNITION_ADDITIONAL_COLOR = 673;

    public GeneralRecognitionController(@NonNull SurfaceView surfaceView
     , @NonNull Runnable emergencyActionInCaseOfAnError
     , @Nullable Pair<Context, Pair<AppCompatButton, Animation>> controlFlashMode
     , @Nullable AppCompatButton btnColorCard
     , @Nullable TextRecognizer textRecognizer
     , @Nullable BarcodeDetector barcodeDetector
     , @Nullable Runnable actionForFinalStage)
    {
        this.surfaceView = surfaceView;
        this.btnColorCard = btnColorCard;
        this.emergencyActionInCaseOfAnError = emergencyActionInCaseOfAnError;
        this.actionForFinalStage = actionForFinalStage;

        this.imageColourManager = new ImageColourManager();
        this.barcodeDetector = barcodeDetector;
        this.textRecognizer = textRecognizer;

        if (controlFlashMode != null)
        {
            this.btnControlFlashMode = controlFlashMode
                    .getSecond()
                    .getFirst();

            this.optionsButtonFlashMode(
                    controlFlashMode.getFirst()
                    , controlFlashMode.getSecond().getSecond());
        }
    }

    @Override
    @SuppressLint("MissingPermission")
    public void surfaceCreated(SurfaceHolder holder)
    {
        try
        {
            this.cameraSource.start(
                    surfaceView.getHolder());
        } catch (IOException ignore) { }
    }

    @Override
    public void surfaceChanged(
            SurfaceHolder holder, int format, int width, int height) { }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    { this.cameraSource.stop(); }

    public GeneralRecognitionController setCameraSourceAndReturnCallback(
     short inputAction
     , @NonNull Context context
     , @NonNull Detector.Processor processorForAction)
    {
        switch (inputAction)
        {
            case ACTION_RECONGNITION_TEXT:
                if (!textRecognizer.isOperational())
                    this.emergencyActionInCaseOfAnError.run();

                this.cameraSource = adaptiveBuilder(
                        context, this.textRecognizer);

                this.textRecognizer.setProcessor(
                        processorForAction);
                break;

            case ACTION_RECOGNITION_BARCODE:
                try { this.textRecognizer.release(); }
                catch (NullPointerException thisEventChangeBarcode) {}
                if (!barcodeDetector.isOperational())
                    this.emergencyActionInCaseOfAnError.run();

                this.cameraSource = adaptiveBuilder(
                        context, this.barcodeDetector);
                this.barcodeDetector.setProcessor(
                        processorForAction);
                surfaceCreated(surfaceView.getHolder());
                break;

            case ACTION_RECOGNITION_COLOR:
                if (!barcodeDetector.isOperational())
                    this.emergencyActionInCaseOfAnError.run();
                this.textRecognizer.release();

                this.barcodeDetector.setProcessor(
                        processorForAction);
                break;

            case ACTION_RECOGNITION_ADDITIONAL_COLOR:
                this.cameraSource = adaptiveBuilder(
                        context, this.barcodeDetector);
                this.textRecognizer.setProcessor(
                        processorForAction);

                surfaceCreated(
                        surfaceView.getHolder());
                break;

            default:
                throw new IllegalArgumentException(
                        "Input action not found.");
        }
        return this;
    }

    private synchronized CameraSource adaptiveBuilder(
     @NonNull Context context
     , @NonNull Detector detectorForAction)
    {
        return new CameraSource.Builder(
                context, detectorForAction)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1920, 1080)
                .setRequestedFps(25.0f)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
                .build();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void optionsButtonFlashMode(
     @Nullable Context context
     , @Nullable Animation animationClick)
    {
        if (this.btnControlFlashMode != null
                && context != null)
        {
            this.btnControlFlashMode.setOnClickListener(inputView ->
            {
                if (animationClick != null)
                    inputView.startAnimation(animationClick);
                flashOnButton(this.cameraSource);

                this.btnControlFlashMode.setCompoundDrawablesWithIntrinsicBounds(
                        null
                        , null
                        , context.getDrawable(!this.getFlashmode()
                                ? R.drawable.icon_flashmode_button_on
                                : R.drawable.icon_flashmode_button_off)
                        , null);

                Toast.makeText(context
                        , "Режим вспышки "
                                + (!this.getFlashmode() ? "включён" : "выключен")
                        , Toast.LENGTH_SHORT)
                        .show();
            });
        }
    }

    public int getColor()
    { return color; }
    public void setColor(int color)
    { this.color = color; }
    public void controlGetColor()
    {
        this.cameraSource.takePicture(
                null, this);
    }

    @Override
    public void onPictureTaken(@Nullable byte[] bytes)
    {
        try
        {
            this.color = Color.parseColor(
                    imageColourManager.setBitmapColorRGB(
                            BitmapFactory.decodeByteArray(
                                    bytes, 0, bytes.length)));
            try
            {
                this.btnColorCard.setBackgroundTintList(
                        ColorStateList.valueOf(this.color));
            }
            catch (Exception ignored)
            {
                this.btnColorCard.setBackgroundTintList(
                        ColorStateList.valueOf(this.color));
            }
        } catch (Exception ignored) { }
        finally
        {
            if (this.actionForFinalStage != null)
                actionForFinalStage.run();
        }
        this.cameraSource.release();
    }
}
