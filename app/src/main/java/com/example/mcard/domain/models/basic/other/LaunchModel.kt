package com.example.mcard.domain.models.basic.other

import androidx.annotation.IdRes


internal sealed class LaunchModel {

    internal object DefaultState : LaunchModel()

    internal data class LaunchState(
        @IdRes val navigationId: Int,
    ) : LaunchModel()
}