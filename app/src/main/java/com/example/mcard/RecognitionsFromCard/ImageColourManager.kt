package com.example.mcard.RecognitionsFromCard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.Toast
import com.example.mcard.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class ImageColourManager
{
    private var redValue = 0
    private var blueValue = 0
    private var greenValue = 0
    private var newBitmapChange: Bitmap? = null
    private val COLOR = "#"
    private var know_color: String? = null

    companion object
    {
        @DelicateCoroutinesApi
        @JvmStatic
        fun getMessage(context: Context) = GlobalScope.launch (Dispatchers.Main)
        {
            Toast.makeText(context
                , context.getString(R.string.infoToastAutoColorCardAdd)
                , Toast.LENGTH_LONG)
                .show()
        }.start()
    }

    fun setBitmapColorRGB(bitmap: Bitmap?): String
    {
        this.newBitmapChange = Bitmap.createScaledBitmap(bitmap!!
            , 1
            , 1
            , true)

        val color = newBitmapChange!!.getPixel(0, 0)
        newBitmapChange!!.recycle()

        this.redValue = Color.red(color)
        this.blueValue = Color.blue(color)
        this.greenValue = Color.green(color)

       return (COLOR
                + Integer.toHexString(this.redValue)
                + Integer.toHexString(this.greenValue)
                + Integer.toHexString(this.blueValue))
    }
}