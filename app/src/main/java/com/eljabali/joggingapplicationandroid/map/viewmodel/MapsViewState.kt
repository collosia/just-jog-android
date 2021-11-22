package com.eljabali.joggingapplicationandroid.map.viewmodel

import com.google.android.gms.maps.model.LatLng

data class MapsViewState(
    val listOfLatLng: List<LatLng> = emptyList(),
)