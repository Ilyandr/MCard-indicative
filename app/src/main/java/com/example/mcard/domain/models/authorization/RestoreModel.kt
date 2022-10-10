package com.example.mcard.domain.models.authorization

import androidx.annotation.StringRes

internal sealed class RestoreModel {
    internal object DefaultState : RestoreModel()

    internal data class LoadingState(
        val login: String?,
    ) : RestoreModel()

    internal data class CompleteState(
        @StringRes val messageId: Int,
    ) : RestoreModel()
}