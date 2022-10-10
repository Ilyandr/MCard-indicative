package com.example.mcard.repository.features.utils

import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import com.example.mcard.R

internal object ApplicationTheme {

    fun <T : ImageView> applyTheme(
        currentTheme: Boolean, view: T? = null,
    ) =
        AppCompatDelegate.setDefaultNightMode(
            if (currentTheme)
                run {
                    view?.setImageResource(
                        R.drawable.ic_mode_light
                    )
                    AppCompatDelegate.MODE_NIGHT_NO
                }
            else
                run {
                    view?.setImageResource(
                        R.drawable.ic_mode_night
                    )
                    AppCompatDelegate.MODE_NIGHT_YES
                }
        )
}