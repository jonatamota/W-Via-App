package com.w_via.features.maps.mapLibreAPI

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MapBackground(
    mapState: MapState,
    modifier: Modifier = Modifier,
    viewModel: MapLibreViewModel
) {
    MapWrapper(
        mapState = mapState,
        modifier = modifier,
        viewModel = viewModel
    )
}
