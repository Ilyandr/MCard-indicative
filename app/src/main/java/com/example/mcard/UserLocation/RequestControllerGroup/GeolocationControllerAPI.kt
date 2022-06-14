package com.example.mcard.UserLocation.RequestControllerGroup

import com.example.mcard.BasicAppActivity
import com.example.mcard.UserLocation.RequestControllerGroup.QueryParams.Companion.constantHandlerAPI
import com.example.mcard.UserLocation.ResponseAPIEntity.PlaceSearchResponse
import retrofit2.Retrofit
import com.example.mcard.UserLocation.RequestControllerGroup.QueryParams.Companion.GENERAL_URL
import kotlinx.coroutines.DelicateCoroutinesApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory

@DelicateCoroutinesApi
internal class GeolocationControllerAPI(
    context: BasicAppActivity) : Callback<PlaceSearchResponse?>
{
    val geolocationService: GeolocationService =
        GeolocationService(context)

    fun launchSearchByGEO(userFormattedActualLocation: String)
    {
        Retrofit.Builder()
            .baseUrl(GENERAL_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleGeoAPI::class.java)
            .getAllData(userFormattedActualLocation
                , constantHandlerAPI(QueryParams.RADIUS_DEFAULT)
                , true
                , constantHandlerAPI(QueryParams.REQUEST_TYPE_DEFAULT)
                , constantHandlerAPI(QueryParams.LANGUAGE_DEFAULT)
                , constantHandlerAPI(QueryParams.API_KEY))
            .enqueue(this)
    }

    override fun onResponse(
        responseCall: Call<PlaceSearchResponse?>
        , responseSuccess: Response<PlaceSearchResponse?>) =
        responseSuccess.body()?.let {
            if (responseSuccess.isSuccessful)
                geolocationService.build(it)
                    .launchSearchMatches()
        }!!

    override fun onFailure(
        call: Call<PlaceSearchResponse?>, t: Throwable) =
        this.geolocationService.showFaultToast()
}