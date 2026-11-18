package com.w_via.features.maps.mapLibreAPI

import android.icu.text.NumberFormat
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.maps.MapView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.graphics.toColorInt
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import com.w_via.features.tollservice.TollPoint
import java.util.Locale

@Composable
fun MapLibreImpl(
    mapState: MapState,
    modifier: Modifier = Modifier,
    viewModel: MapLibreViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isDarkTheme = isSystemInDarkTheme()

    val haloColor = if (isDarkTheme) "#000000" else "#FFFFFF"
    val textColor = if (isDarkTheme) "#FFFFFF" else "#000000"

    // MapView é criado uma única vez e sobrevive a recomposições
    val mapView = remember {
        MapView(context).apply {
            setBackgroundColor("#E8E5E5".toColorInt())
        }
    }

    // Guarda se o estilo já foi carregado para não recarregar nas próximas recomposições
    var styleLoaded by remember { mutableStateOf(false) }

    // Vincula o ciclo de vida uma única vez
    LaunchedEffect(mapView) {
        viewModel.bindMapView(mapView, lifecycleOwner)
    }

    // --- Efeito 1: carrega o estilo UMA vez ---
    // Só roda na primeira composição (key = Unit)
    LaunchedEffect(Unit) {
        val styleUri = if (isDarkTheme) "asset://style_dark.json" else "asset://style.json"
        mapView.getMapAsync { map ->
            // Posição inicial antes do estilo carregar
            map.cameraPosition = CameraPosition.Builder()
                .target(LatLng(mapState.latitude, mapState.longitude))
                .zoom(mapState.zoom)
                .build()

            map.uiSettings.apply {
                isCompassEnabled = false
                isLogoEnabled = false
                isAttributionEnabled = false
            }

            map.setStyle(styleUri) { style ->
                // Adiciona as layers/sources uma única vez
                if (style.getSource("route-source") == null) {
                    style.addSource(GeoJsonSource("route-source"))
                    style.addLayerBelow(
                        LineLayer("route-layer", "route-source").apply {
                            setProperties(
                                PropertyFactory.lineColor("#6201ED"),
                                PropertyFactory.lineWidth(6f),
                                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
                            )
                        },
                        "road_label"
                    )
                }

                if (style.getSource("route-start-source") == null) {
                    style.addSource(GeoJsonSource("route-start-source"))
                    style.addLayer(
                        CircleLayer("route-start-layer", "route-start-source").apply {
                            setProperties(
                                PropertyFactory.circleRadius(6f),
                                PropertyFactory.circleColor("#0c920b"),
                                PropertyFactory.circleStrokeWidth(2f),
                                PropertyFactory.circleStrokeColor("#FFFFFF")
                            )
                        }
                    )
                }

                if (style.getSource("route-end-source") == null) {
                    style.addSource(GeoJsonSource("route-end-source"))
                    style.addLayer(
                        CircleLayer("route-end-layer", "route-end-source").apply {
                            setProperties(
                                PropertyFactory.circleRadius(6f),
                                PropertyFactory.circleColor("#d81616"),
                                PropertyFactory.circleStrokeWidth(2f),
                                PropertyFactory.circleStrokeColor("#FFFFFF")
                            )
                        }
                    )
                }

                if (style.getSource("toll-source") == null) {
                    style.addSource(GeoJsonSource("toll-source"))

                    style.addLayer(
                        CircleLayer("toll-circle-layer", "toll-source").apply {
                            setProperties(
                                PropertyFactory.circleRadius(8f),
                                PropertyFactory.circleColor("#6201ed"),
                                PropertyFactory.circleStrokeWidth(2.5f),
                                PropertyFactory.circleStrokeColor("#FFFFFF")
                            )
                        }
                    )

                    style.addLayer(
                        SymbolLayer("toll-label-layer", "toll-source").apply {
                            setProperties(
                                PropertyFactory.textField(
                                    if (mapState.showTollNames)
                                        Expression.format(
                                            Expression.formatEntry(
                                                Expression.get("nome"),
                                                Expression.FormatOption.formatFontScale(0.9)
                                            ),
                                            Expression.formatEntry(Expression.literal("\n")),
                                            Expression.formatEntry(
                                                Expression.get("valor"),
                                                Expression.FormatOption.formatFontScale(0.85)
                                            )
                                        )
                                    else
                                        Expression.get("valor")
                                ),
                                PropertyFactory.textSize(11f),
                                PropertyFactory.textColor(textColor),
                                PropertyFactory.textHaloColor(haloColor),
                                PropertyFactory.textHaloWidth(0.7f),
                                PropertyFactory.textAnchor(Property.TEXT_ANCHOR_CENTER),
                                PropertyFactory.textOffset(arrayOf(0f, 2.5f)),
                                PropertyFactory.textFont(arrayOf("Roboto Regular")),
                                PropertyFactory.textAllowOverlap(false)
                            )
                        }
                    )
                }

                styleLoaded = true
            }
        }
    }

    // --- Efeito 2: atualiza apenas os dados dinâmicos quando mapState muda ---
    // Não move a câmera: lat/lng ficam fora das keys (localização não reajusta a
    // visão) e o movimento só ocorre via [MapState.cameraTarget] (Efeito 3).
    // A serialização da geometria (GeoJSON) da polyline/pedágios é feita fora da
    // main thread para não travar o app em rotas longas com muitos pontos.
    LaunchedEffect(styleLoaded, mapState.routePoints, mapState.tollPoints, mapState.isMyLocationEnabled) {
        if (!styleLoaded) return@LaunchedEffect

        val payload = buildRouteGeoJson(mapState.routePoints, mapState.tollPoints)

        mapView.getMapAsync { map ->
            val style = map.style ?: return@getMapAsync

            // Atualiza localização do usuário
            if (mapState.isMyLocationEnabled) {
                try {
                    if (!map.locationComponent.isLocationComponentActivated) {
                        val locationOptions = LocationComponentOptions.builder(context).build()
                        map.locationComponent.apply {
                            activateLocationComponent(
                                LocationComponentActivationOptions
                                    .builder(context, style)
                                    .locationComponentOptions(locationOptions)
                                    .build()
                            )
                            isLocationComponentEnabled = true
                        }
                    } else {
                        map.locationComponent.isLocationComponentEnabled = true
                    }
                } catch (e: SecurityException) {
                    Log.e("MapLibreImpl", "Permissão negada: ${e.message}")
                }
            }

            // Pedágios
            style.getSourceAs<GeoJsonSource>("toll-source")?.setGeoJson(payload.tollsJson)

            // Rota (apenas o desenho da polyline)
            val routeJson = payload.routeJson
            if (routeJson != null && payload.startJson != null && payload.endJson != null) {
                style.getSourceAs<GeoJsonSource>("route-source")?.setGeoJson(routeJson)
                style.getSourceAs<GeoJsonSource>("route-start-source")?.setGeoJson(payload.startJson)
                style.getSourceAs<GeoJsonSource>("route-end-source")?.setGeoJson(payload.endJson)
            } else {
                // Limpa rota
                val vazio = FeatureCollection.fromFeatures(emptyList()).toJson()
                style.getSourceAs<GeoJsonSource>("route-source")?.setGeoJson(vazio)
                style.getSourceAs<GeoJsonSource>("route-start-source")?.setGeoJson(vazio)
                style.getSourceAs<GeoJsonSource>("route-end-source")?.setGeoJson(vazio)
            }
        }
    }

    // --- Efeito 3: movimento de câmera (só quando cameraTarget muda) ---
    LaunchedEffect(styleLoaded, mapState.cameraTarget, mapState.routePoints) {
        if (!styleLoaded) return@LaunchedEffect
        val target = mapState.cameraTarget ?: return@LaunchedEffect

        mapView.getMapAsync { map ->
            when (target) {
                is MapCameraTarget.Center -> {
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(target.latitude, target.longitude),
                            target.zoom
                        )
                    )
                }
                is MapCameraTarget.FitRoute -> {
                    if (mapState.routePoints.size >= 2) {
                        val latLngs = mapState.routePoints.map { LatLng(it.first, it.second) }
                        val cantosDistintos = latLngs.distinctBy { ll -> "${ll.latitude},${ll.longitude}" }
                        if (cantosDistintos.size >= 2) {
                            val bounds = LatLngBounds.Builder().includes(cantosDistintos).build()
                            map.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(
                                    bounds,
                                    target.horizontalPaddingPx,
                                    target.topPaddingPx,
                                    target.horizontalPaddingPx,
                                    target.bottomPaddingPx
                                )
                            )
                        } else {
                            val centro = latLngs.first()
                            map.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    centro,
                                    map.cameraPosition.zoom.coerceAtLeast(13.0)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { mapView },
            // update vazio — toda lógica está nos LaunchedEffects acima
            update = {},
            modifier = Modifier.fillMaxSize()
        )

        if (!styleLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            )
        }
    }
}

/**
 * Serializa rota/pedágios para GeoJSON fora da main thread. Aplicar o JSON
 * pronto via [GeoJsonSource.setGeoJson] (java.lang.String) é barato; o custo
 * para centenas/milhares de pontos fica no thread de background.
 */
private suspend fun buildRouteGeoJson(
    routePoints: List<Pair<Double, Double>>,
    tollPoints: List<TollPoint>
): RouteGeoJsonPayload = withContext(Dispatchers.Default) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    val tollsJson = if (tollPoints.isNotEmpty()) {
        val features = tollPoints.map { toll ->
            Feature.fromGeometry(
                Point.fromLngLat(toll.lng, toll.lat)
            ).apply {
                addStringProperty("nome", toll.nome)
                addStringProperty("valor", currencyFormat.format(toll.valor))
                addStringProperty("concessionaria", toll.concessionaria)
            }
        }
        FeatureCollection.fromFeatures(features).toJson()
    } else {
        FeatureCollection.fromFeatures(emptyList()).toJson()
    }

    var routeJson: String? = null
    var startJson: String? = null
    var endJson: String? = null

    if (routePoints.size >= 2) {
        val points = routePoints.map { Point.fromLngLat(it.second, it.first) }
        routeJson = LineString.fromLngLats(points).toJson()

        val startPoint = routePoints.first()
        startJson = Feature.fromGeometry(
            Point.fromLngLat(startPoint.second, startPoint.first)
        ).toJson()

        val endPoint = routePoints.last()
        endJson = Feature.fromGeometry(
            Point.fromLngLat(endPoint.second, endPoint.first)
        ).toJson()
    }

    RouteGeoJsonPayload(
        tollsJson = tollsJson,
        routeJson = routeJson,
        startJson = startJson,
        endJson = endJson
    )
}

private data class RouteGeoJsonPayload(
    val tollsJson: String,
    val routeJson: String?,
    val startJson: String?,
    val endJson: String?
)