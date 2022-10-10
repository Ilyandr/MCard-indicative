package com.example.mcard.repository.features.location.source

import androidx.fragment.app.Fragment
import com.example.mcard.repository.features.location.dataSource.LocationDataSourse

internal interface LocationSourse {
    fun runWithGrantedAccess(): Any

    infix fun requireSecureFacilityListener(
        fragment: Fragment,
    ): LocationDataSourse.SecureFacilityListener
}