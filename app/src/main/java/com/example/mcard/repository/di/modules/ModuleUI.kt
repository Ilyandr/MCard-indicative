package com.example.mcard.repository.di.modules

import android.content.Context
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.example.mcard.R
import dagger.Module
import dagger.Provides
import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog
import javax.inject.Named
import javax.inject.Singleton

@Module
internal class ModuleUI {

    @Provides
    @Singleton
    @Named(value = "animationSelect")
    fun provideAnimationSelectObject(context: Context): Animation =
        AnimationUtils.loadAnimation(
            context, R.anim.select_object
        )

    @Provides
    @Named(value = "textFlashing")
    fun provideAnimationTextFlashing(context: Context): Animation =
        AnimationUtils.loadAnimation(
            context, R.anim.light_selected_btn
        )

    @Provides
    fun provideColorPickerDialog(): ColorPickerDialog =
        ColorPickerDialog()
            .withTheme(R.style.ColorPickerDialogTheme)
            .withColor(R.color.main_color)
            .withCornerRadius(16f)
            .withTitle("Выбор цвета")
}