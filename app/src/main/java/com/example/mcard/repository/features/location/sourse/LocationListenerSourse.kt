package com.example.mcard.repository.features.location.sourse

import androidx.fragment.app.Fragment
import com.example.mcard.repository.features.location.dataSource.LocationDataSourse

internal interface LocationListenerSourse {
    fun runWithGrantedAccess(): Any

    infix fun requireSecureFacilityListener(
        fragment: Fragment,
    ): LocationDataSourse.SecureFacilityListener
}