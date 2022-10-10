package com.example.mcard.domain.viewModels.authorization

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mcard.domain.models.authorization.RegistrationModel
import com.example.mcard.repository.features.SupportLiveData.default
import com.example.mcard.repository.features.SupportLiveData.set
import com.example.mcard.R
import com.example.mcard.repository.features.optionally.DataCorrectness.checkCorrectLogin
import com.example.mcard.repository.features.optionally.DataCorrectness.checkCorrectPassword
import com.example.mcard.repository.features.optionally.DataCorrectness.removeSymbolsTab
import com.example.mcard.repository.source.architecture.viewModels.LiveViewModel
import com.example.mcard.repository.features.rest.firebase.AuthorizationFirebase
import com.example.mcard.repository.features.storage.preferences.UserPreferences
import javax.inject.Inject

internal class RegistrationViewModel : LiveViewModel<RegistrationModel>() {

    private val mutableLiveData =
        MutableLiveData<RegistrationModel>()
            .default(
                RegistrationModel.DefaultState
            )

    override val liveDataState: LiveData<RegistrationModel> =
        this.mutableLiveData

    @Inject
    lateinit var authorizationFirebase: AuthorizationFirebase

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun registrationOfInteraction(
        lifecycleOwner: LifecycleOwner,
    ) =
        lifecycleOwner.let {
            this.liveDataState.observe(it) { currentModel ->
                when (currentModel) {
                    is RegistrationModel.LoadingState ->
                        completeAction(currentModel)

                    else -> return@observe
                }
            }
        }

    private fun completeAction(
        loadingState: RegistrationModel.LoadingState,
    ) {
        loadingState.login
            ?.removeSymbolsTab()
            ?.let { login ->
                loadingState.password
                    ?.removeSymbolsTab()
                    ?.let { password ->
                        loadingState.accountName
                            ?.removeSymbolsTab()
                            ?.let { accountName ->
                                if (!login.checkCorrectLogin())
                                    this.mutableLiveData.set(
                                        RegistrationModel.FaultState(
                                            R.string.errorCheckLogin
                                        )
                                    )
                                else if (!password.checkCorrectPassword())
                                    this.mutableLiveData.set(
                                        RegistrationModel.FaultState(
                                            R.string.errorCheckPassword
                                        )
                                    )
                                else if (!accountName.checkCorrectPassword())
                                    this.mutableLiveData.set(
                                        RegistrationModel.FaultState(
                                            R.string.errorCheckID
                                        )
                                    )
                                else
                                    this.authorizationFirebase.registrationAction(
                                        authData = loadingState,
                                        successAction = {
                                            this.userPreferences.setUserData(
                                                it.login, it.password
                                            )

                                            this.mutableLiveData.set(
                                                RegistrationModel.SuccessState
                                            )
                                        },
                                        faultAction = {
                                            this.mutableLiveData.set(
                                                RegistrationModel.FaultState(it)
                                            )
                                        }
                                    )
                            }
                    }
            }
    }

    fun launchRegistrationAction(
        accountId: String?,
        login: String?,
        password: String?,
    ) =
        this.mutableLiveData.set(
            RegistrationModel.LoadingState(
                accountId, login, password
            )
        )

    override fun actionСompleted() {
        this.mutableLiveData.default(
            RegistrationModel.DefaultState
        )
    }
}