package com.example.mcard.repository.models.other

import androidx.annotation.StringRes

internal data class SpannableData(
    @StringRes val linkId: Int,
    val startParentIndex: Int,
    val endParentIndex: Int,
)