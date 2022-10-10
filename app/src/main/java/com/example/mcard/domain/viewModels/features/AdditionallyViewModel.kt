package com.example.mcard.domain.viewModels.features

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mcard.domain.models.features.AdditionallyModel
import com.example.mcard.repository.di.AppComponent
import com.example.mcard.repository.features.SupportLiveData.default
import com.example.mcard.repository.features.SupportLiveData.set
import com.example.mcard.repository.features.payment.dataSource.PaymentController
import com.example.mcard.repository.features.payment.dataSource.PaymentDataSource
import com.example.mcard.repository.source.architecture.viewModels.LiveViewModel
import javax.inject.Inject

internal class AdditionallyViewModel(
    appComponent: AppComponent,
) : LiveViewModel<AdditionallyModel>() {

    private val mutableLiveState =
        MutableLiveData<AdditionallyModel>()
            .default(
                AdditionallyModel.DefaultState
            )

    @Inject
    lateinit var paymentDataSource: PaymentDataSource

    var paymentController: PaymentController? = null
        private set

    override val liveDataState: LiveData<AdditionallyModel> =
        this.mutableLiveState


    init {
        appComponent inject this
    }

    override fun actionСompleted() {
        mutableLiveState.set(
            AdditionallyModel.DefaultState
        )
    }

    infix fun setPaymentController(fragmentForResult: Fragment) {
        this.paymentController =
            PaymentController(
                dataSource = paymentDataSource,
                fragmentForResult, { amount ->
                    mutableLiveState.set(
                        AdditionallyModel.SuccessPaymentState(
                            "Платёж на сумму $amount руб. успешно проведён. Благодарим вас за поддержку данного проекта!"
                        )
                    )
                }) {
                mutableLiveState.set(
                    AdditionallyModel.FaultPaymentState
                )
            }
    }

    fun viewDestroyed() {
        this.paymentController = null
    }

    override fun registrationOfInteraction(
        lifecycleOwner: LifecycleOwner,
    ) {
    }
}