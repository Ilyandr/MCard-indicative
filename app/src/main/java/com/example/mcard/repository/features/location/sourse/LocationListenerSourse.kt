package com.example.mcard.repository.features.location.sourse

import androidx.fragment.app.Fragment
import com.example.mcard.repository.features.location.dataSource.LocationListenerDataSourse

internal interface LocationListenerSourse {
    fun runWithGrantedAccess(): Any

    infix fun requireSecureFacilityListener(
        fragment: Fragment,
    ): LocationListenerDataSourse.SecureFacilityListener
}