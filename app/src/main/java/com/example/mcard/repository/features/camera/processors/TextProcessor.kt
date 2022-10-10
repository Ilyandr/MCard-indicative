package com.example.mcard.repository.features.camera.processors

import com.google.android.gms.vision.Detector.Detections
import com.example.mcard.repository.features.camera.ProcessorDataSource.Companion.ACTION_NAME
import com.example.mcard.repository.features.camera.ProcessorDataSource.Companion.ACTION_NUMBER
import com.example.mcard.repository.features.dataTextProcessorAction
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.jvm.Synchronized

internal class TextProcessor(
    private val successAction: dataTextProcessorAction,
) : Detector.Processor<TextBlock> {

    override fun release() {}

    override fun receiveDetections(detections: Detections<TextBlock>) {
        try {
            detections.detectedItems.apply {
                for (i in 0 until this.size()) {
                    this.valueAt(i)?.let {
                        correctGetText(it.value)
                    }
                }
            }
        } catch (ex: Exception) {
        }
    }

    @Synchronized
    private fun correctGetText(getBadResult: String?) {
        MainScope().launch(Dispatchers.Main) {
            getBadResult?.trim()?.let { dataResult ->
                if (dataResult.isNotEmpty()) {
                    dataResult.length.let { length ->
                        dataResult.trim().apply {
                            if (this.replace("O", "0").isNumberRegex()
                                && length >= 6
                                && length <= 16
                            ) successAction.invoke(ACTION_NUMBER,
                                this.replace("O", "0")
                            )
                        }

                        if (!dataResult.isNumberRegex()
                            && length >= 2
                            && length <= 20
                        ) successAction.invoke(
                            ACTION_NAME, dataResult
                        )
                    }
                }
            }
        }
    }

    fun String.isNumberRegex() =
        this.all { char -> char.isDigit() }
}