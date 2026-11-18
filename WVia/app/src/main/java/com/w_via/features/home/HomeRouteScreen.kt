package com.w_via.features.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import com.w_via.R
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import androidx.navigation.NavController
import com.w_via.designsystem.appbuttons.PrimaryButton
import com.w_via.features.maps.mapLibreAPI.MapCameraTarget
import com.w_via.features.maps.mapLibreAPI.MapState
import com.w_via.features.maps.location.UserLocationProvider
import com.w_via.features.home.components.CalculatorBoxBottom
import com.w_via.features.home.components.ResultCard
import com.w_via.features.home.components.RoutesBar
import com.w_via.features.home.components.ListItem
import com.w_via.features.routes.Routes
import com.w_via.features.home.components.CostProfitItem
import com.w_via.features.tollservice.TipoVeiculo
import com.w_via.features.tollservice.TollPoint

@Composable
fun HomeRouteScreen(
    navController: NavController,
    bottomBarPadding: Dp,
    mapState: MapState,
    onMapStateChange: ((MapState) -> MapState) -> Unit,
    viewModel: MapRouteViewModel
) {
    val context = LocalContext.current
    val isPrefsLoaded by viewModel.isPrefsLoaded.collectAsState()
    val origemText by viewModel.origemText.collectAsState()
    val destinoText by viewModel.destinoText.collectAsState()
    val routePoints by viewModel.routePoints.collectAsState()
    val modalExpanded by viewModel.modalExpanded.collectAsState()
    val mostrarResultadoCalculo by viewModel.mostrarResultadoCalculo.collectAsState()
    val resultadoCalculo by viewModel.resultadoCalculo.collectAsState()
    val mensagemCalculo by viewModel.mensagemCalculo.collectAsState()
    val routeError by viewModel.routeError.collectAsState()
    val isLoadingRoute by viewModel.isLoadingRoute.collectAsState()
    val swapAnimationTrigger by viewModel.swapAnimationTrigger.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Verifica se a permissão já estava concedida para não pedir de novo ao voltar
    val alreadyGranted = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) ==
                PackageManager.PERMISSION_GRANTED
    }
    var permissaoLocalizacao by remember { mutableStateOf(alreadyGranted) }
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    val userLocationProvider = remember { UserLocationProvider(context.applicationContext) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showRationaleDialog by remember { mutableStateOf(false) }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    val selectedPlace by savedStateHandle
        ?.getStateFlow("selected_place", null as PlaceSuggestion?)
        ?.collectAsState() ?: remember { mutableStateOf(null) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            permissaoLocalizacao = granted
            if (!granted) {
                val activity = context as? Activity
                if (activity != null && ActivityCompat.shouldShowRequestPermissionRationale(
                        activity, Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) showRationaleDialog = true
                else showPermissionDialog = true
            }
        }
    )

    // Só pede permissão se ainda não foi concedida
    LaunchedEffect(Unit) {
        if (!alreadyGranted) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    LaunchedEffect(permissaoLocalizacao) {
        // Atualizar isMyLocationEnabled no estado global do mapa (escrita atômica)
        onMapStateChange { current -> current.copy(isMyLocationEnabled = permissaoLocalizacao) }

        if (permissaoLocalizacao) {
            // Verificar se GPS está habilitado
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            Log.d(
                "HomeRouteScreen",
                "GPS habilitado: $isGpsEnabled, Network habilitado: $isNetworkEnabled"
            )

            if (!isGpsEnabled && !isNetworkEnabled) {
                Log.e("HomeRouteScreen", "GPS e Network Location estão desabilitados")
                // Opcional: mostrar dialog para habilitar GPS
                return@LaunchedEffect
            }

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                Log.d("HomeRouteScreen", "Tentando obter localização atual...")
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    CancellationTokenSource().token
                ).addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        Log.d(
                            "HomeRouteScreen",
                            "Localização obtida: ${location.latitude}, ${location.longitude}"
                        )
                        userLocation = Pair(location.latitude, location.longitude)
                        // Atualizar posição no mapa global. Só centraliza a câmera
                        // quando não há rota desenhada (não rouba a visão da rota).
                        onMapStateChange { current ->
                            current.copy(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                zoom = 13.0,
                                cameraTarget = if (current.routePoints.isEmpty()) {
                                    MapCameraTarget.Center(location.latitude, location.longitude, 13.0)
                                } else {
                                    current.cameraTarget
                                }
                            )
                        }
                    } else {
                        Log.d(
                            "HomeRouteScreen",
                            "getCurrentLocation retornou null, tentando lastLocation..."
                        )
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                            if (lastLoc != null) {
                                Log.d(
                                    "HomeRouteScreen",
                                    "lastLocation obtido: ${lastLoc.latitude}, ${lastLoc.longitude}"
                                )
                                userLocation = Pair(lastLoc.latitude, lastLoc.longitude)
                                // Atualizar posição no mapa global
                                onMapStateChange { current ->
                                    current.copy(
                                        latitude = lastLoc.latitude,
                                        longitude = lastLoc.longitude,
                                        zoom = 13.0,
                                        cameraTarget = if (current.routePoints.isEmpty()) {
                                            MapCameraTarget.Center(lastLoc.latitude, lastLoc.longitude, 13.0)
                                        } else {
                                            current.cameraTarget
                                        }
                                    )
                                }
                            } else {
                                Log.e("HomeRouteScreen", "Nenhuma localização disponível")
                            }
                        }
                    }
                }.addOnFailureListener { e ->
                    Log.e("HomeRouteScreen", "Erro ao obter localização: ${e.message}")
                }
            } catch (e: SecurityException) {
                Log.e("HomeRouteScreen", "Permissao negada: ${e.message}")
            }
        } else {
            Log.d("HomeRouteScreen", "Permissão de localização não concedida")
        }
    }

    LaunchedEffect(mensagemCalculo) {
        val msg = mensagemCalculo ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.limparMensagemCalculo()
    }

    LaunchedEffect(routeError) {
        val msg = routeError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.limparErroRota()
    }

    LaunchedEffect(selectedPlace) {
        selectedPlace?.let { place ->
            val field = savedStateHandle?.get<String>("selected_field") ?: "destino"

            val isCurrentLocation =
                place.primaryText == "Sua localização atual" || place.placeId == "mylocation"

            if (isCurrentLocation) {
                // Aplica o texto imediatamente para dar feedback ao usuário.
                if (field == "origem") {
                    viewModel.setOrigemText("Sua localização atual")
                } else {
                    viewModel.setDestinoText("Sua localização atual")
                }

                // Resolve as coordenadas reais de forma suspensa (com timeout),
                // reaproveitando a localização já obtida quando disponível.
                val loc = userLocation ?: userLocationProvider.obterLocalizacao()

                if (loc != null) {
                    if (field == "origem") {
                        viewModel.atualizarOrigemComLocalizacao(loc.first, loc.second)
                    } else {
                        viewModel.atualizarDestinoComLocalizacao(loc.first, loc.second)
                    }
                } else {
                    snackbarHostState.showSnackbar(
                        "Não foi possível obter sua localização. Verifique o GPS e as permissões."
                    )
                    if (field == "origem") {
                        viewModel.setOrigemText("De onde?")
                    } else {
                        viewModel.setDestinoText("Para onde?")
                    }
                }
                savedStateHandle?.remove<PlaceSuggestion>("selected_place")
                savedStateHandle?.remove<String>("selected_field")
            } else {
                if (field == "origem") {
                    viewModel.atualizarOrigem(place.primaryText, place.latitude, place.longitude)
                } else {
                    viewModel.atualizarDestino(place.primaryText, place.latitude, place.longitude)
                }
                savedStateHandle?.remove<PlaceSuggestion>("selected_place")
                savedStateHandle?.remove<String>("selected_field")
            }
        }
    }

    val resultadoPedagio by viewModel.resultadoPedagio.collectAsState()
    val tipoVeiculo by viewModel.tipoVeiculo.collectAsState()

    val density = LocalDensity.current

    // Área que a modal cobre no rodapé do mapa. Com a modal expandida o espaço
    // visível é menor, então a câmera dá zoom out; recolhida, mais espaço ⇒ zoom in.
    val routeBottomPadding =
        bottomBarPadding + if (modalExpanded) 320.dp else 88.dp

    // Atualizar o estado global do mapa com os pontos de rota e pedágios.
    // A câmera acompanha apenas quando a rota é (re)traçada ou quando a modal
    // expande/recolhe — ver Efeito 3 do mapa.
    LaunchedEffect(routePoints, resultadoPedagio, tipoVeiculo, modalExpanded) {
        onMapStateChange { current ->
            current.copy(
                routePoints = routePoints,
                tollPoints = resultadoPedagio?.pracas?.map {
                    TollPoint(
                        lat = it.lat,
                        lng = it.lng,
                        nome = it.nome,
                        valor = when (tipoVeiculo) {
                            TipoVeiculo.CARRO -> it.valorCarro
                            TipoVeiculo.MOTO -> it.valorMoto
                        },
                        concessionaria = it.concessionaria
                    )
                } ?: emptyList(),
                cameraTarget = if (routePoints.isNotEmpty()) {
                    MapCameraTarget.FitRoute(
                        topPaddingPx = with(density) { 140.dp.roundToPx() },
                        bottomPaddingPx = with(density) { routeBottomPadding.roundToPx() },
                        horizontalPaddingPx = with(density) { 40.dp.roundToPx() }
                    )
                } else {
                    current.cameraTarget
                }
            )
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            title = { Text("Permissao de Localizacao") },
            text = { Text("Para mostrar sua localizacao atual no mapa, conceda a permissao nas configuracoes do dispositivo.") },
            confirmButton = {
                Button(onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts("package", context.packageName, null)
                    context.startActivity(intent)
                    showPermissionDialog = false
                }) { Text("Ir para Configuracoes") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) { Text("Mais tarde") }
            }
        )
    }

    // Aguarda o DataStore ser lido para não renderizar com estado padrão
    if (!isPrefsLoaded) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(bottom = bottomBarPadding)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
            )
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        ) {

            val resultado = resultadoCalculo
            val showResult = mostrarResultadoCalculo && resultado != null

            AnimatedContent(
                targetState = showResult,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.92f)) togetherWith
                            (fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 0.92f))
                },
                label = "TopBarAnimation"
            ) { isResultVisible ->
                if (isResultVisible && resultado != null) {
                    ResultCard(
                        onClickAction = {
                            viewModel.voltarParaEdicaoRota()
                        },
                        column1Items = listOf(
                            ListItem(
                                R.drawable.ic_route_24dp,
                                resultado.totalKmTexto
                            ),
                            ListItem(
                                R.drawable.ic_local_gas_station_24dp,
                                resultado.combustivelTexto
                            ),
                            ListItem(
                                R.drawable.ic_humidity_mid_24dp,
                                resultado.litrosTexto
                            ),
                        ),
                        column2Items = listOf(
                            ListItem(
                                R.drawable.ic_chronic_24dp,
                                resultado.tempoTexto
                            ),
                            ListItem(
                                R.drawable.ic_pedagio,
                                resultado.totalPedagioTexto
                            ),
                            ListItem(
                                R.drawable.ic_paid_24dp,
                                resultado.valorCobrarTexto
                            )
                        ),
                        costItem = CostProfitItem(
                            iconSource = R.drawable.ic_paid_24dp,
                            text = "Custos",
                            value = resultado.custosTotaisTexto,
                            color = Color.Red
                        ),
                        profitItem = CostProfitItem(
                            iconSource = R.drawable.ic_paid_24dp,
                            text = "Lucro",
                            value = resultado.lucroTexto,
                            color = Color(0xFF32A207)
                        )
                    )
                } else {
                    RoutesBar(
                        origemValue = origemText,
                        destinoValue = destinoText,
                        swapAnimationTrigger = swapAnimationTrigger,
                        onOrigemClick = {
                            navController.navigate(Routes.SEARCH_DESTINATION + "?field=origem")
                        },
                        onDestinoClick = {
                            navController.navigate(Routes.SEARCH_DESTINATION + "?field=destino")
                        },
                        onSwapClick = { viewModel.trocarOrigemDestino() }
                    )
                }
            }
        }


        BottomSheetRoute(
            modifier = Modifier.align(Alignment.BottomCenter),
            viewModel = viewModel,
            mostrarResultadoCalculo = mostrarResultadoCalculo,
            isLoadingRoute = isLoadingRoute,
            onCalcularRota = { viewModel.calcularCustosRota() }
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp)
        )
    }
}


@Composable
private fun BottomSheetRoute(
    viewModel: MapRouteViewModel,
    mostrarResultadoCalculo: Boolean,
    onCalcularRota: () -> Unit,
    isLoadingRoute: Boolean,
    modifier: Modifier = Modifier
) {
    val expanded by viewModel.modalExpanded.collectAsState()

    val valorKm by viewModel.valorKm.collectAsState()
    val valorCombustivel by viewModel.valorCombustivel.collectAsState()
    val autonomia by viewModel.autonomia.collectAsState()
    val valorAdicional by viewModel.valorAdicional.collectAsState()
    val idaVolta by viewModel.idaVolta.collectAsState()

    val colorSurface = MaterialTheme.colorScheme.background
    val cornerRadius = RoundedCornerShape(0.dp)
    val heightItems = 40.dp

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Barra de controle: toggle sempre visível + botão Calcular condicional
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Box de toggle: SEMPRE visível
            Box(
                modifier = Modifier
                    .size(heightItems, heightItems)
                    .border(1.dp, MaterialTheme.colorScheme.outline, cornerRadius)
                    .clickable { viewModel.setModalExpanded(!expanded) }
                    .background(colorSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowDown
                    else Icons.Default.KeyboardArrowUp,
                    contentDescription = if (expanded) "Recolher" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Botão Calcular Rota: visível apenas quando recolhido E sem ResultCard
            AnimatedVisibility(
                visible = !expanded && !mostrarResultadoCalculo,
                enter = fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.95f),
                exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.95f)
            ) {
                PrimaryButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(heightItems)
                        .padding(horizontal = 8.dp),
                    onClick = onCalcularRota,
                    text = "Calcular Rota",
                    isLoading = isLoadingRoute,
                    isEnabled = !isLoadingRoute,
                )
            }
        }

        // Modal
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = colorSurface)
        ) {
            // Handle — clica para recolher
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setModalExpanded(!expanded) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.outline,
                            shape = cornerRadius
                        )
                )
            }

            AnimatedVisibility(visible = expanded) {
                CalculatorBoxBottom(
                    onCalcularClick = onCalcularRota,
                    onIdaVoltaChange = { viewModel.setIdaVolta(it) },
                    idaVolta = idaVolta,
                    valorKm = valorKm,
                    onValorKmChange = { viewModel.setValorKm(it) },
                    valorCombustivel = valorCombustivel,
                    onValorCombustivelChange = { viewModel.setValorCombustivel(it) },
                    autonomia = autonomia,
                    onAutonomiaChange = { viewModel.setAutonomia(it) },
                    valorAdicional = valorAdicional,
                    onValorAdicionalChange = { viewModel.setValorAdicional(it) },
                    isLoading = isLoadingRoute,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp)
                )
            }
        }
    }
}