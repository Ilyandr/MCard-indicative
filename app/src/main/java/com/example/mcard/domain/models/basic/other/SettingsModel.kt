package com.example.mcard.domain.models.basic.other

import androidx.annotation.StringRes

internal sealed class SettingsModel {

    internal object DefaultState : SettingsModel()

    internal object WaitingState : SettingsModel()

    internal data class MessageState(
        @StringRes val messageId: Int,
    ) : SettingsModel()

    internal object DeleteAccountState : SettingsModel()

    internal object RemoveCacheState : SettingsModel()
}