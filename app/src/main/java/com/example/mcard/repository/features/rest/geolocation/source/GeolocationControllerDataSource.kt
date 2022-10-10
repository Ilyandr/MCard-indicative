package com.example.mcard.repository.features.rest.geolocation.source

import com.example.mcard.R
import com.example.mcard.repository.features.location.dataSource.LocationService
import com.example.mcard.repository.features.location.sourse.QueryParams
import com.example.mcard.repository.features.location.sourse.QueryParams.Companion.constantHandlerAPI
import com.example.mcard.repository.features.messageAction
import com.example.mcard.repository.features.rest.geolocation.api.LocationSource
import com.example.mcard.repository.models.location.PlaceSearchResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


internal class GeolocationControllerDataSource(
    private val locationService: LocationService,
    private val api: LocationSource,
) : Callback<PlaceSearchResponse?>, GeolocationControllerSourse {

    private var messageAction: messageAction? = null
    override fun launch(
        messageAction: messageAction,
        contextuserFormattedActualLocation: String,
    ) =
        api.getAllData(
            contextuserFormattedActualLocation,
            constantHandlerAPI(QueryParams.RADIUS_DEFAULT),
            true,
            constantHandlerAPI(QueryParams.REQUEST_TYPE_DEFAULT),
            constantHandlerAPI(QueryParams.LANGUAGE_DEFAULT),
            constantHandlerAPI(QueryParams.API_KEY)
        ).enqueue(this).apply {
            this@GeolocationControllerDataSource.messageAction = messageAction
        }

    override fun onResponse(
        responseCall: Call<PlaceSearchResponse?>,
        responseSuccess: Response<PlaceSearchResponse?>,
    ) {
        responseSuccess.body()?.apply {
            if (responseSuccess.isSuccessful)
                locationService.create(
                    messageAction, this
                ).launchSearchMatches()
        }
    }

    override fun onFailure(
        call: Call<PlaceSearchResponse?>, t: Throwable,
    ) {
        messageAction?.invoke(R.string.msgApiErrorLocationService)
    }
}