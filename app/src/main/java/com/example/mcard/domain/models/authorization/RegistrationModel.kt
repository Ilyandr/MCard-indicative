package com.example.mcard.domain.models.authorization

import androidx.annotation.StringRes

internal sealed class RegistrationModel
{
    internal object DefaultState: RegistrationModel()

    internal object SuccessState: RegistrationModel()

    internal data class FaultState(
        @StringRes val messageId: Int
        ): RegistrationModel()

    internal data class LoadingState(
        val accountName: String?,
        val login: String?,
        val password: String?
        ): RegistrationModel()
}