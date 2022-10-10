package com.example.mcard.repository.features.rest.geolocation.source

import com.example.mcard.repository.features.messageAction

internal interface GeolocationControllerSourse {
    fun launch(
        messageAction: messageAction,
        contextuserFormattedActualLocation: String,
    )
}