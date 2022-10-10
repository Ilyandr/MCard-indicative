package com.example.mcard.repository.models.other

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.mcard.repository.features.unitAction

internal data class SheetModel(
    @StringRes val textId: Int,
    @DrawableRes val iconId: Int? = null,
    val isCheckable: Boolean,
    var singleChecked: Boolean = false,
    val action: unitAction,
)