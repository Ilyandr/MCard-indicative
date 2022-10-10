package com.example.mcard.repository.features.camera.processors

import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.Detector.Detections
import com.example.mcard.repository.features.dataBarcodeAction
import com.example.mcard.repository.models.storage.BarcodeModel
import com.google.android.gms.vision.Detector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.lang.Exception

internal class BarcodeProcessor(
    private val successAction: dataBarcodeAction,
) : Detector.Processor<Barcode> {

    override fun receiveDetections(detections: Detections<Barcode>) {
        try {
            detections.detectedItems.valueAt(0)?.let {
                MainScope().launch(Dispatchers.Main) {
                    successAction.invoke(
                        BarcodeModel(
                            it.rawValue, it.format
                        )
                    )
                }
            }
        } catch (ignored: Exception) {
        }
    }

    override fun release() {}
}