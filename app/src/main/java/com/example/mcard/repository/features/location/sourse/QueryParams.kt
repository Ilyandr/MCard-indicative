@file:Suppress("UNCHECKED_CAST")

package com.example.mcard.repository.features.location.sourse

import java.lang.IllegalArgumentException

internal enum class QueryParams {
    RADIUS_DEFAULT, REQUEST_TYPE_DEFAULT, LANGUAGE_DEFAULT, API_KEY;

    companion object {
        infix fun <T> constantHandlerAPI(inputParam: Any) =
            when {
                RADIUS_DEFAULT == inputParam -> 75 as T
                REQUEST_TYPE_DEFAULT == inputParam -> "food|store|supermarket" as T
                API_KEY == inputParam -> "This is an indicative model of the application, without api keys." as T
                LANGUAGE_DEFAULT == inputParam -> "ru" as T
                else -> IllegalArgumentException(
                    "Request parameter not found."
                ).fillInStackTrace() as T
            }
    }
}