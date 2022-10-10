package com.example.mcard.repository.source.usage

import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.mcard.repository.features.dataBarcodeAction
import com.example.mcard.repository.features.messageAction

internal interface ProcessorSource : SurfaceHolder.Callback {
    var barcodeAvailability: Boolean

    infix fun launchMultiDetector(
        statusAction: messageAction?,
    )

    infix fun launchSingleBarcodeDetector(
        afterAction: dataBarcodeAction,
    )

    infix fun updateHolder(
        data: SurfaceView,
    )
}