@file:Suppress("DEPRECATION")

package com.example.mcard.repository.features.camera.custom

import android.hardware.Camera
import com.google.android.gms.vision.CameraSource

internal open class CameraFlashMode {
    fun CameraSource?.launchFlash() =
        getCamera()?.let {
            try {
                val param = it.parameters
                param.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                it.parameters = param
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

    fun CameraSource?.getCamera(): Camera? {
        CameraSource::class.java.declaredFields.forEach {
            if (it.type === Camera::class.java) {
                it.isAccessible = true
                return try {
                    it.get(this
                        ?: return@forEach) as? Camera
                } catch (e: IllegalAccessException) {
                    null
                }
            }
        }
        return null
    }
}