package com.example.mcard.UserLocation.RequestControllerGroup

import java.lang.IllegalArgumentException

internal enum class QueryParams
{
    RADIUS_DEFAULT, REQUEST_TYPE_DEFAULT, LANGUAGE_DEFAULT, API_KEY;

    @Suppress("UNCHECKED_CAST")
    companion object
    {
        @JvmStatic
        fun <T> constantHandlerAPI(inputParam: Any) =
            when
            {
                RADIUS_DEFAULT == inputParam -> 100 as T
                REQUEST_TYPE_DEFAULT == inputParam -> "food|store|supermarket" as T
                API_KEY == inputParam -> "this is indicative project" as T
                LANGUAGE_DEFAULT == inputParam -> "ru" as T
                else -> IllegalArgumentException(
                    "Request parameter not found."
                ).fillInStackTrace() as T
            }

        const val GENERAL_URL =
            "https://maps.googleapis.com/"
    }
}