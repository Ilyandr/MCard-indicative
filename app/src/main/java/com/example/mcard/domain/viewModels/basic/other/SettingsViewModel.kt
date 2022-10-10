package com.example.mcard.domain.viewModels.basic.other

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mcard.R
import com.example.mcard.domain.models.basic.other.SettingsModel
import com.example.mcard.repository.di.AppComponent
import com.example.mcard.repository.features.SupportLiveData.default
import com.example.mcard.repository.features.SupportLiveData.set
import com.example.mcard.repository.features.optionally.DataCorrectness.checkCorrectLogin
import com.example.mcard.repository.features.optionally.DataCorrectness.checkCorrectPassword
import com.example.mcard.repository.features.rest.firebase.AdditionAuthFirebase
import com.example.mcard.repository.features.storage.preferences.UserPreferences
import com.example.mcard.repository.source.architecture.viewModels.LiveViewModel
import com.example.mcard.repository.source.usage.EntranceUsageSource
import javax.inject.Inject

internal class SettingsViewModel(
    appComponent: AppComponent,
) : LiveViewModel<SettingsModel>(), EntranceUsageSource {

    @Inject
    lateinit var additionAuthFirebase: AdditionAuthFirebase

    @Inject
    override lateinit var userPreferences: UserPreferences

    private val mutableLiveDataState =
        MutableLiveData<SettingsModel>()
            .default(
                SettingsModel.DefaultState
            )

    override val liveDataState: LiveData<SettingsModel> =
        this.mutableLiveDataState

    init {
        appComponent inject this
    }

    override fun actionСompleted() {
        this.mutableLiveDataState.set(
            SettingsModel.DefaultState
        )
    }

    fun changeLogin(
        oldData: String?, newData: String?,
    ) {
        if (oldData.checkCorrectLogin()
            && newData.checkCorrectLogin()
        ) {
            launchWaitingState()

            additionAuthFirebase.changeLogin(
                oldData!!, newData!!, ::sendMessage
            )
        } else
            sendMessage(
                R.string.msgWarningInputData
            )
    }

    private infix fun sendMessage(messageId: Int) =
        this.mutableLiveDataState.set(
            SettingsModel.MessageState(
                messageId
            )
        )

    private fun launchWaitingState() =
        this.mutableLiveDataState.set(
            SettingsModel.WaitingState
        )

    fun changePassword(
        oldData: String?, newData: String?,
    ) {
        if (oldData.checkCorrectPassword()
            && newData.checkCorrectPassword()
        ) {
            launchWaitingState()

            additionAuthFirebase.changePassword(
                oldData!!, newData!!, ::sendMessage
            )
        } else
            sendMessage(
                R.string.msgWarningInputData
            )
    }

    fun deleteAccount() {
        launchWaitingState()

        additionAuthFirebase.removeAccount(
            faultAction = ::sendMessage
        ) {
            mutableLiveDataState.set(
                SettingsModel.DeleteAccountState
            )
        }
    }

    fun clearCache() =
        mutableLiveDataState.set(
            SettingsModel.RemoveCacheState
        )

    override fun registrationOfInteraction(lifecycleOwner: LifecycleOwner) {}
}