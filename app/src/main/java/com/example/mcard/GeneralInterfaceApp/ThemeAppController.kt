package com.example.mcard.GeneralInterfaceApp

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.example.mcard.R
import com.example.mcard.StorageAppActions.DataInterfaceCard
import kotlinx.coroutines.*

@DelicateCoroutinesApi
internal class ThemeAppController(
    val dataInterfaceCard: DataInterfaceCard?)
{
    companion object
    { const val BASIC_APP_COLOR = "#00b3b3" }

    fun changeDesignIconBar(linearLayout: View) =
        GlobalScope.launch(Dispatchers.Main)
        {
            linearLayout.setBackgroundResource(
                R.drawable.bar_black)
            linearLayout.setCustomColorFilter()
        }


    fun changeDesignDefaultView(linearLayout: LinearLayout) =
        GlobalScope.launch(Dispatchers.Main)
        {
            linearLayout.setBackgroundResource(
                R.drawable.list_black)
            linearLayout.setCustomColorFilter()
        }

    fun setOptionsButtons(vararg arrayOfViews: View)
    {
        val selectDrawable: Deferred<Int> = GlobalScope.async(Dispatchers.IO)
        {
            if (dataInterfaceCard!!.actionGeneralIconsAppColor(null)
                != Color.TRANSPARENT)
                    dataInterfaceCard
                        .actionGeneralIconsAppColor(null)
            else
                Color.parseColor(BASIC_APP_COLOR)
        }

        GlobalScope.launch(Dispatchers.Main)
        {
            if (selectDrawable.await() != Color.TRANSPARENT)
            {
                val generalColor = ColorStateList.valueOf(
                    selectDrawable.await())

                arrayOfViews.forEach {
                    when (it)
                    {
                        is AppCompatImageButton ->
                            it.imageTintList = generalColor
                        is AppCompatButton ->
                        {
                            it.setHintTextColor(
                                Color.parseColor(BASIC_APP_COLOR))
                            it.typeface = dataInterfaceCard?.context?.let { context ->
                                ResourcesCompat.getFont(context, R.font.bandera_small_font)
                            }
                        }
                        is AppCompatTextView ->
                        {
                            it.setTextColor(
                                Color.parseColor(BASIC_APP_COLOR))
                            it.typeface = dataInterfaceCard?.context?.let { context ->
                                ResourcesCompat.getFont(context, R.font.bandera_small_font)
                            }
                        }
                    }
                }
            }
        }
    }

    fun settingsText(textView: View, text: String): Boolean
    {
        ((textView as? TextView)
            ?: (textView as? AppCompatEditText))
            ?.text = text
        val customUserTextColor = dataInterfaceCard
            ?.actionGeneralTextAppColor(null) != Color.TRANSPARENT

        ((textView as? TextView)
            ?: (textView as? AppCompatEditText))
            ?.setTextColor(if (customUserTextColor)
            dataInterfaceCard?.actionGeneralTextAppColor(
                null)
                ?.let {
                    ColorStateList.valueOf(it)
                }
        else
            ColorStateList.valueOf(
                Color.parseColor(BASIC_APP_COLOR)))

        return customUserTextColor
    }

    fun settingsTextAdapter(textView: View, text: String): Boolean
    {
        ((textView as? TextView)
            ?: (textView as? AppCompatEditText))
            ?.text = text
        val customUserTextColor = dataInterfaceCard
            ?.actionGeneralTextAppColor(null) != Color.TRANSPARENT

        ((textView as? AppCompatTextView)
            ?: (textView as? AppCompatEditText))
            ?.setTextColor(if (customUserTextColor)
                dataInterfaceCard?.actionGeneralTextAppColor(
                    null)
                    ?.let { ColorStateList.valueOf(it) }
            else
                ColorStateList.valueOf(
                    Color.parseColor(BASIC_APP_COLOR)))

        return customUserTextColor
    }

    fun settingsHint(textView: View, text: String): Boolean
    {
        ((textView as? TextView)
            ?: (textView as? AppCompatEditText))
            ?.hint = text
        val customUserTextColor = dataInterfaceCard
            ?.actionGeneralTextAppColor(null) != Color.TRANSPARENT

        ((textView as? TextView)
            ?: (textView as? AppCompatEditText))
            ?.setHintTextColor(if (customUserTextColor)
                dataInterfaceCard?.actionGeneralTextAppColor(
                    null)?.let { ColorStateList.valueOf(it) }
            else ColorStateList.valueOf(
                Color.parseColor(BASIC_APP_COLOR)))

        return customUserTextColor
    }

    private fun View.setCustomColorFilter() =
        this@ThemeAppController.dataInterfaceCard
            ?.actionGeneralInterfaceAppColor()
            ?.first
            ?.let {
                @Suppress("DEPRECATION")
                this.background.setColorFilter(
                    it, if (this@ThemeAppController.dataInterfaceCard
                            .actionGeneralInterfaceAppColor().second)
                        PorterDuff.Mode.SRC
                    else
                        PorterDuff.Mode.SCREEN)
            }
}
