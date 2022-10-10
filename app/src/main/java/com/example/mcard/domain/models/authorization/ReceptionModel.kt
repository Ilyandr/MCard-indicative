package com.example.mcard.domain.models.authorization

import androidx.annotation.StringRes

internal sealed class ReceptionModel
{
    internal object DefaultState: ReceptionModel()

    internal object SuccessState: ReceptionModel()

    internal object NotAuthorizedState: ReceptionModel()

    internal data class LoadingState(
        val login: String?,
        val password: String?
        ): ReceptionModel()

    internal data class FaultState(
        @StringRes val messageId: Int
        ): ReceptionModel()
}