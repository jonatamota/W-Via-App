package com.w_via

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import com.w_via.features.appnavigation.AppNavigation
import com.w_via.features.appnavigation.BottomNavigation
import com.w_via.features.home.MapRouteViewModel
import com.w_via.features.routes.Routes
import com.w_via.features.maps.mapLibreAPI.MapState
import com.w_via.features.maps.mapLibreAPI.MapBackground
import com.w_via.features.maps.mapLibreAPI.MapLibreViewModel
import com.w_via.ui.theme.WViaTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w_via.ui.theme.colorBrand
import android.graphics.Color as AndroidColor

class MainActivity : AppCompatActivity() {
    @SuppressLint("UseKtx")
    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                false
            }
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT
            )
        )

        super.onCreate(savedInstanceState)

        setContent {
            WViaTheme {
                MainApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra("navigate_to")?.let { route ->
            NavigationManager.pendingRoute.value = route
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val mapViewModel: MapLibreViewModel = viewModel()
    // Escopado à Activity: sobrevive à navegação entre abas (a HOME é recriada
    // pelo popUpTo(HOME){inclusive=true}, o que antes destruía a rota).
    val mapRouteViewModel: MapRouteViewModel = viewModel(factory = MapRouteViewModel.Factory)

    val routesWithBottomBar = listOf(Routes.HOME, Routes.MEUS_RECIBOS, Routes.DECLARATION, Routes.PERFIL, Routes.TESTE)
    val imeVisible = WindowInsets.isImeVisible
    val showNavBar = currentDestination in routesWithBottomBar && !imeVisible
    val isMapScreen = currentDestination == Routes.HOME
    val isTabScreen = currentDestination in routesWithBottomBar

    // Nas telas da NavigationBar o mapa aparece direto (sem cobertura).
    // Fora delas (detalhes/criação/login) cobre o mapa para não "vazar" na transição.
    val mapCoverColor =
        if (isTabScreen) Color.Transparent else MaterialTheme.colorScheme.background

    // Estado global do mapa
    var mapState by remember {
        mutableStateOf(
            MapState(
                latitude = -23.5505,
                longitude = -46.6333,
                zoom = 11.0,
                isMyLocationEnabled = false,
                routePoints = emptyList(),
                tollPoints = emptyList(),
                showTollNames = true
            )
        )
    }

    val pendingRoute by NavigationManager.pendingRoute.collectAsState()
    LaunchedEffect(pendingRoute) {
        pendingRoute?.let { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                launchSingleTop = true
            }
            NavigationManager.pendingRoute.value = null
        }
    }

    Scaffold(
        bottomBar = {
            if (showNavBar) BottomNavigation(
                startDestination = Routes.HOME,
                navController = navController
            )
        }
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize()) {
            MapBackground(
                mapState = mapState,
                modifier = Modifier.fillMaxSize(),
                viewModel = mapViewModel
            )

            // Cobre o mapa com cor sólida quando não está na HOME (e durante transições)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(mapCoverColor)
            )

            AppNavigation(
                navController = navController,
                bottomBarPadding = if (showNavBar) paddingValues.calculateBottomPadding() else 0.dp,
                mapState = mapState,
                onMapStateChange = { transform -> mapState = transform(mapState) },
                mapRouteViewModel = mapRouteViewModel
            )

            SystemBarsScrim(
                showStatusBar = !isMapScreen,
                statusBarColor = colorBrand,
                showNavigationBar = isTabScreen,
                navigationBarColor = MaterialTheme.colorScheme.background
            )
        }
    }
}

@Composable
fun BoxScope.SystemBarsScrim(
    showStatusBar: Boolean,
    statusBarColor: Color,
    showNavigationBar: Boolean,
    navigationBarColor: Color
) {
    Box(
        Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
            .windowInsetsTopHeight(WindowInsets.statusBars)
            .background(if (showStatusBar) statusBarColor else Color.Transparent)
    )
    Box(
        Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .windowInsetsBottomHeight(WindowInsets.navigationBars)
            .background(if (showNavigationBar) navigationBarColor else Color.Transparent)
    )
}
