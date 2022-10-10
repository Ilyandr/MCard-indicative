package com.example.mcard.domain.models.features

internal sealed class AdditionallyModel {

    internal object DefaultState : AdditionallyModel()

    internal data class SuccessPaymentState(
        val message: String,
    ) : AdditionallyModel()

    internal object FaultPaymentState : AdditionallyModel()
}