@file:Suppress("DEPRECATION")

package com.example.mcard.repository.features.camera

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.mcard.R
import com.example.mcard.repository.features.*
import com.example.mcard.repository.features.camera.custom.CameraFlashMode
import com.example.mcard.repository.features.camera.processors.BarcodeProcessor
import com.example.mcard.repository.features.camera.processors.TextProcessor
import com.example.mcard.repository.features.fullDataCardAction
import com.example.mcard.repository.features.getColorRGB
import com.example.mcard.repository.features.messageAction
import com.example.mcard.repository.features.setUniqueIdentifier
import com.example.mcard.repository.source.usage.ProcessorSource
import com.example.mcard.repository.models.storage.CardEntity
import com.example.mcard.repository.models.storage.CardWithHistoryEntity
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.MultiDetector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class ProcessorDataSource(
    private var surfaceView: SurfaceView,
    private val finalStageAction: fullDataCardAction,
) : CameraFlashMode(),
    CameraSource.PictureCallback,
    ProcessorSource {

    override var barcodeAvailability = true

    private var useMultiDetector = true

    private val cardEntity: CardEntity by lazy {
        CardEntity(
            uniqueIdentifier = setUniqueIdentifier()
        )
    }

    private val barcodeDetector: BarcodeDetector by lazy {
        BarcodeDetector.Builder(surfaceView.context)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()
    }

    private val textDetector: TextRecognizer by lazy {
        TextRecognizer.Builder(surfaceView.context).build()
    }

    private val multiDetector: MultiDetector by lazy {
        MultiDetector
            .Builder().apply {
                if (useMultiDetector)
                    add(textDetector)
                add(barcodeDetector)
            }.build()
    }

    private val cameraSource: CameraSource by lazy {
        CameraSource.Builder(
            surfaceView.context, multiDetector)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1920, 1080)
            .setRequestedFps(30f)
            .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
            .build()
    }

    @SuppressLint("MissingPermission")
    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            surfaceView.holder.apply {
                this.addCallback(this@ProcessorDataSource)
                cameraSource.start(this)
            }
        } catch (ignore: Exception) {
        }
    }

    override fun surfaceChanged(
        holder: SurfaceHolder, format: Int, width: Int, height: Int,
    ) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        cameraSource.stop()
    }

    override fun launchMultiDetector(statusAction: messageAction?) {

        MainScope().launch(Dispatchers.Main) {
            textDetector.setProcessor(
                TextProcessor { dataId, data ->
                    when (dataId) {
                        ACTION_NAME ->
                            if (cardEntity.name.isNullOrEmpty()) {
                                cardEntity.name = data
                                statusAction checkCompleteTextDetector
                                        R.string.dataStatusViewSecondDescription
                            }


                        ACTION_NUMBER ->
                            if (cardEntity.number.isNullOrEmpty()) {
                                cardEntity.number = data
                                statusAction checkCompleteTextDetector
                                        R.string.dataStatusViewFirstDescription
                            }
                    }
                }
            )

            barcodeDetector.setProcessor(
                BarcodeProcessor {
                    if (!cardEntity.name.isNullOrEmpty()
                        && !cardEntity.number.isNullOrEmpty()
                        && cardEntity.barcode.isNullOrEmpty()
                    ) {
                        cardEntity.barcode = it.toString()

                        statusAction checkCompleteTextDetector
                                R.string.dataStatusViewFourthDescription

                        Executors.newSingleThreadScheduledExecutor().schedule({
                            MainScope().launch(Dispatchers.Main) {
                                cameraSource.takePicture(
                                    null, this@ProcessorDataSource
                                )
                            }
                        }, 2000, TimeUnit.MILLISECONDS)
                    }
                }
            )

            surfaceCreated(surfaceView.holder)
            cameraSource.launchFlash()
        }
    }

    private infix fun messageAction?.checkCompleteTextDetector(
        textDetectorId: Int,
    ) = this?.invoke(
        if (!cardEntity.name.isNullOrEmpty()
            && !cardEntity.number.isNullOrEmpty()
        ) {
            if (!barcodeAvailability)
                cameraSource.takePicture(
                    null, this@ProcessorDataSource
                )
            R.string.dataStatusViewThirdDescription
        } else
            textDetectorId
    )

    override fun launchSingleBarcodeDetector(
        afterAction: dataBarcodeAction,
    ) {
        MainScope().launch(Dispatchers.Main) {
            barcodeDetector.setProcessor(
                BarcodeProcessor(
                    afterAction
                )
            )

            useMultiDetector = false
            surfaceCreated(surfaceView.holder)
            cameraSource.launchFlash()
        }
    }

    override fun updateHolder(data: SurfaceView) {
        this.surfaceView = data
    }

    override fun onPictureTaken(bytes: ByteArray) {
        MainScope().async(Dispatchers.IO) {
            try {
                BitmapFactory.decodeByteArray(
                    bytes, 0, bytes.size
                ).getColorRGB()
            } catch (ex: Exception) {
                Color.rgb(
                    (30..200).random(),
                    (30..200).random(),
                    (30..200).random()
                )
            }
        }.let {
            MainScope().launch(Dispatchers.Main) {
                cardEntity.color = it.await()
                cameraSource.release()
                finishDetectors()
                surfaceDestroyed(surfaceView.holder)

                finalStageAction.invoke(
                    CardWithHistoryEntity(
                        cardEntity
                    )
                )
            }
        }
    }

    private fun finishDetectors() {
        surfaceDestroyed(surfaceView.holder)
        barcodeDetector.release()
        textDetector.release()
    }

    companion object {
        const val ACTION_NAME: Short = 260
        const val ACTION_NUMBER: Short = 261
        const val ACTION_BARCODE: Short = 262
        const val ACTION_COLOR: Short = 263
    }
}