package com.example.mcard.repository.features.rest.geolocation.api

import retrofit2.http.GET
import com.example.mcard.repository.models.location.PlaceSearchResponse
import retrofit2.Call
import retrofit2.http.Query

internal interface LocationSource
{
    @GET("maps/api/place/nearbysearch/json")
    fun getAllData(
        @Query("location") locationXY: String,
        @Query("radius") radiusSearch: Int,
        @Query("strictbounds") strictDefinition: Boolean,
        @Query("type") requestType: String,
        @Query("language") selectLanguage: String,
        @Query("key") keyAPI: String): Call<PlaceSearchResponse?>
}