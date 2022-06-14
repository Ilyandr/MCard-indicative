package com.example.mcard.CommercialAction.YandexADS

import androidx.annotation.Keep
import androidx.annotation.StringRes

@Keep
internal data class MediationNetworkEntity(
 @StringRes val info: Int
 , val adUnitId: String? = null
 , val actionForADS: Pair<Runnable?, Runnable?>? = null
 , val actionSuccessADS: Runnable? = null)