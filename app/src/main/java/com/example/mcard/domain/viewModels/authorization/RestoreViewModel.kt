package com.example.mcard.domain.viewModels.authorization

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mcard.domain.models.authorization.RestoreModel
import com.example.mcard.repository.features.SupportLiveData.default
import com.example.mcard.repository.features.SupportLiveData.set
import com.example.mcard.R
import com.example.mcard.repository.features.optionally.DataCorrectness.checkCorrectLogin
import com.example.mcard.repository.source.architecture.viewModels.LiveViewModel
import com.example.mcard.repository.features.rest.firebase.AuthorizationFirebase
import javax.inject.Inject

internal class RestoreViewModel : LiveViewModel<RestoreModel>() {

    private val mutableLiveDataState =
        MutableLiveData<RestoreModel>()
            .default(RestoreModel.DefaultState)

    override val liveDataState: LiveData<RestoreModel> =
        this.mutableLiveDataState

    @Inject
    lateinit var authorizationFirebase: AuthorizationFirebase

    override fun registrationOfInteraction(lifecycleOwner: LifecycleOwner) =
        lifecycleOwner.let {
            liveDataState.observe(it) { currentState ->
                when (currentState) {
                    is RestoreModel.LoadingState ->
                        currentState.restoreAction()

                    else -> return@observe
                }
            }
        }

    private fun RestoreModel.LoadingState.restoreAction() {
        if (this.login.checkCorrectLogin())
            authorizationFirebase.restoreAccount(
                data = this.login!!,
                completeAction = {
                    mutableLiveDataState.set(
                        RestoreModel.CompleteState(it)
                    )
                }
            )
        else
            mutableLiveDataState.set(
                RestoreModel.CompleteState(
                    R.string.errorCheckLogin
                )
            )
    }

    fun launchRestoreAction(login: String?) =
        this.mutableLiveDataState.set(
            RestoreModel.LoadingState(
                login
            )
        )

    override fun actionСompleted() {
        this.mutableLiveDataState.set(
            RestoreModel.DefaultState
        )
    }
}