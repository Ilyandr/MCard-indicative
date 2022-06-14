@file:Suppress("DEPRECATION")
package com.example.mcard.RecognitionsFromCard.GraphicRecognitions

import android.hardware.Camera
import com.google.android.gms.vision.CameraSource

internal open class CustomCameraFlashMode
{
    var flashmode: Boolean = true

    fun flashOnButton(cameraSource: CameraSource?) =
        this.getCamera(cameraSource)?.let {
            try
            {
                this.flashmode = !this.flashmode
                val param = it.parameters

                param.flashMode =
                    if (!flashmode)
                        Camera.Parameters.FLASH_MODE_TORCH
                    else
                        Camera.Parameters.FLASH_MODE_OFF
                it.parameters = param
             }
            catch (e: java.lang.Exception)
            { e.printStackTrace() }
        }

    private infix fun getCamera(
        cameraSource: CameraSource?): Camera?
    {
        CameraSource::class.java.declaredFields.forEach {
            if (it.type === Camera::class.java)
            {
                it.isAccessible = true
                return try
                {
                    it.get(cameraSource
                        ?: return@forEach) as? Camera
                }
                catch (e: IllegalAccessException) { null }
            }
        }
        return null
    }
}