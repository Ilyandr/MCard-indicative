package com.example.mcard.domain.viewModels.authorization

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mcard.domain.models.authorization.ReceptionModel
import com.example.mcard.R
import com.example.mcard.repository.source.architecture.viewModels.LiveViewModel
import com.example.mcard.repository.features.rest.firebase.AuthorizationFirebase
import com.example.mcard.repository.features.storage.preferences.UserPreferences
import com.example.mcard.repository.features.SupportLiveData.default
import com.example.mcard.repository.features.SupportLiveData.set
import com.example.mcard.repository.features.optionally.DataCorrectness.checkCorrectLogin
import com.example.mcard.repository.features.optionally.DataCorrectness.checkCorrectPassword
import com.example.mcard.repository.features.optionally.DataCorrectness.removeSymbolsTab
import javax.inject.Inject

internal class ReceptionViewModel : LiveViewModel<ReceptionModel>() {
    private val mutableLiveDataState =
        MutableLiveData<ReceptionModel>()
            .default(ReceptionModel.DefaultState)

    override val liveDataState: LiveData<ReceptionModel> =
        this.mutableLiveDataState

    @Inject
    lateinit var authorizationFirebase: AuthorizationFirebase

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun registrationOfInteraction(lifecycleOwner: LifecycleOwner) =
        lifecycleOwner.let {
            this.liveDataState.observe(lifecycleOwner) { currentState ->
                when (currentState) {
                    is ReceptionModel.LoadingState ->
                        this authentication currentState

                    is ReceptionModel.NotAuthorizedState -> {
                        this.userPreferences.setUserData(
                            "-", "-"
                        )
                    }

                    else -> return@observe
                }
            }
        }

    private infix fun authentication(data: ReceptionModel.LoadingState) =
        data.login?.removeSymbolsTab()
            ?.let { login ->
                data.password?.removeSymbolsTab()
                    ?.let { password ->
                        if (login.checkCorrectLogin()
                            && password.checkCorrectPassword()
                        )
                            this.authorizationFirebase.entranceAction(
                                authData = data,
                                successAction = {
                                    this.userPreferences.setUserData(
                                        it.login, it.password
                                    )

                                    this.mutableLiveDataState.set(
                                        ReceptionModel.SuccessState
                                    )
                                }
                            ) {
                                this.mutableLiveDataState.set(
                                    ReceptionModel.FaultState(it)
                                )
                            }
                        else
                            this.mutableLiveDataState.set(
                                ReceptionModel.FaultState(
                                    if (!login.checkCorrectLogin())
                                        R.string.errorCheckLogin
                                    else
                                        R.string.errorCheckPassword
                                )
                            )
                    }
            }

    fun loggedNoneAuthAction() =
        this.mutableLiveDataState.set(
            ReceptionModel.NotAuthorizedState
        )

    fun loggedAuthAction(login: String?, password: String?) =
        mutableLiveDataState.set(
            if (mutableLiveDataState.value is ReceptionModel.DefaultState)
                ReceptionModel.LoadingState(
                    login, password
                )
            else
                ReceptionModel.FaultState(
                    R.string.msgLoadingCheckAuth
                )
        )

    override fun actionСompleted() {
        mutableLiveDataState.default(
            ReceptionModel.DefaultState
        )
    }
}