package com.example.mcard.domain.viewModels.basic.other

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mcard.domain.models.basic.other.LaunchModel
import com.example.mcard.R
import com.example.mcard.repository.features.SupportLiveData.set
import com.example.mcard.repository.features.storage.preferences.UserPreferences
import com.example.mcard.repository.features.utils.ApplicationTheme
import com.example.mcard.repository.source.architecture.viewModels.LiveViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class LaunchViewModel : LiveViewModel<LaunchModel>() {

    private val mutableLiveDataState =
        MutableLiveData<LaunchModel>(
            LaunchModel.DefaultState
        )

    override val liveDataState: LiveData<LaunchModel> =
        this.mutableLiveDataState

    @Inject
    lateinit var userPreferences: UserPreferences

    fun launchControlAction() {

        ApplicationTheme.applyTheme(
            userPreferences.themeApp(), null
        )

        viewModelScope.launch(Dispatchers.IO) {

            delay(1500)

            withContext(Dispatchers.Main) {
                try {
                    mutableLiveDataState.set(
                        LaunchModel.LaunchState(
                            if (userPreferences.existData())
                                R.id.launchBasicFragmentFromLaunchFragment
                            else
                                R.id.launchReceptionFragment
                        )
                    )
                } catch (ex: IllegalStateException) {
                    return@withContext
                }
            }
        }
    }

    override fun registrationOfInteraction(
        lifecycleOwner: LifecycleOwner,
    ) {
    }

    override fun actionСompleted() {
        this.mutableLiveDataState.set(
            LaunchModel.DefaultState
        )
    }
}