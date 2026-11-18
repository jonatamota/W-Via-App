package com.w_via.features.maps.mapLibreAPI

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MapWrapper(
    mapState: MapState,
    modifier: Modifier = Modifier,
    viewModel: MapLibreViewModel
) {
    MapLibreImpl(
        mapState = mapState,
        modifier = modifier,
        viewModel = viewModel
    )
}