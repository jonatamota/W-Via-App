package com.w_via.features.maps.mapLibreAPI

import android.annotation.SuppressLint
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import org.maplibre.android.maps.MapView

class MapLibreViewModel : ViewModel() {

    @SuppressLint("StaticFieldLeak")
    private var mapView: MapView? = null

    fun bindMapView(mapView: MapView, lifecycleOwner: LifecycleOwner) {
        this.mapView = mapView
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                mapView.onStart()
            }
            override fun onResume(owner: LifecycleOwner) {
                mapView.onResume()
            }
            override fun onPause(owner: LifecycleOwner) {
                mapView.onPause()
            }
            override fun onStop(owner: LifecycleOwner) {
                mapView.onStop()
            }
            override fun onDestroy(owner: LifecycleOwner) {
                mapView.onDestroy()
                this@MapLibreViewModel.mapView = null
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        mapView = null
    }
}