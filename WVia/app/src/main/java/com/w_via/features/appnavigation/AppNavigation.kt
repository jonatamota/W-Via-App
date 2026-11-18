package com.w_via.features.appnavigation

import android.annotation.SuppressLint
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.w_via.BuildConfig
import com.w_via.features.authentication.finishlogin.FinishCreateAuthScreen
import com.w_via.features.authentication.loginAuth.LoginScreen
import com.w_via.features.authentication.splash.SplashScreen
import com.w_via.features.datamycnpj.MyCnpjScreen
import com.w_via.features.datamycnpj.UploadFileScreen
import com.w_via.features.datamycnpj.UploadFileViewModel
import com.w_via.features.declaration.DeclaracaoObitoScreen
import com.w_via.features.declaration.DeclaracaoDetalheScreen
import com.w_via.features.declaration.DeclarationScreen
import com.w_via.features.declaration.createdeclaration.DeclarationDraftViewModel
import com.w_via.features.declaration.createdeclaration.DeclarationGridScreen
import com.w_via.features.declaration.createdeclaration.declarationSectionDescriptors
import com.w_via.features.home.HomeRouteScreen
import com.w_via.features.home.MapRouteViewModel
import com.w_via.features.home.SearchDestinationScreen
import com.w_via.features.initscreens.InitStepScreen
import com.w_via.features.initscreens.PrivacyPolicyScreen
import com.w_via.features.maps.mapLibreAPI.MapState
import com.w_via.features.profile.InformacoesProfissionaisScreen
import com.w_via.features.profile.LegalDocumentScreen
import com.w_via.features.profile.LicenseScreen
import com.w_via.features.profile.ProfilePlaceholderScreen
import com.w_via.features.profile.ProfileScreen
import com.w_via.features.receipt.CreateReciboScreen
import com.w_via.features.receipt.MyReceiptScreen
import com.w_via.features.receipt.ReciboDetalheScreen
import com.w_via.features.receipt.ReciboSucessoScreen
import com.w_via.features.routes.Routes
import com.w_via.features.testscreen.TestScreen


// Animação "esteira/parede": as abas da barra deslizam horizontalmente como se
// todas fizessem parte de uma única parede contínua (sem fade, sem zoom).
// Avançando para a aba à direita -> nova entra da direita, antiga sai à esquerda.
// Voltando para a aba à esquerda  -> nova entra da esquerda, antiga sai à direita.
private fun wallSlideRightEnter(): EnterTransition =
    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(350))

private fun wallSlideLeftEnter(): EnterTransition =
    slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(350))

private fun wallSlideRightExit(): ExitTransition =
    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(350))

private fun wallSlideLeftExit(): ExitTransition =
    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(350))

// Direção relativa à navegação real: compara a aba de origem ([fromRoute]) com a
// de destino ([toRoute]). Direita ( → ) quando o destino está à direita da origem.
// Quando um dos lados NÃO é uma aba (tela empilhada, ex.: grid e seções), o
// comportamento volta ao padrão de avanço: a aba sai para a esquerda enquanto a
// nova tela entra da direita (mesmo movimento do push).
private fun barEnter(toRoute: String?, fromRoute: String?): EnterTransition {
    val toIndex = barTabIndex[toRoute]
    val fromIndex = barTabIndex[fromRoute]
    return if (toIndex != null && fromIndex != null) {
        if (toIndex > fromIndex) wallSlideRightEnter() else wallSlideLeftEnter()
    } else {
        wallSlideRightEnter()
    }
}

private fun barExit(toRoute: String?, fromRoute: String?): ExitTransition {
    val toIndex = barTabIndex[toRoute]
    val fromIndex = barTabIndex[fromRoute]
    return if (toIndex != null && fromIndex != null) {
        if (toIndex > fromIndex) wallSlideRightExit() else wallSlideLeftExit()
    } else {
        wallSlideRightExit()
    }
}

// Transição de "empilhar" uma tela: mesmo deslize horizontal em esteira da barra
// de navegação, como se a tela fosse um painel contíguo da mesma parede.
// Avançando (push)  -> parede desliza à esquerda: nova entra da direita, antiga sai para a esquerda.
// Voltando (pop)    -> parede desliza à direita: nova entra da esquerda, antiga sai para a direita.
private fun pushEnter(): EnterTransition = wallSlideRightEnter()

private fun pushExit(): ExitTransition = wallSlideRightExit()

private fun pushPopEnter(): EnterTransition = wallSlideLeftEnter()

private fun pushPopExit(): ExitTransition = wallSlideLeftExit()

// Ordem das abas na NavigationBar (da esquerda para direita):
// Perfil(0) -> Home(1) -> Recibos(2) -> Declarações(3) -> Testes(4, só debug)
private val barTabIndex = mapOf(
    Routes.PERFIL to 0,
    Routes.HOME to 1,
    Routes.MEUS_RECIBOS to 2,
    Routes.DECLARATION to 3,
    Routes.TESTE to 4
)

@SuppressLint("UseKtx")
@Composable
fun AppNavigation(
    navController: NavHostController,
    bottomBarPadding: Dp = 0.dp,
    mapState: MapState? = null,
    onMapStateChange: ((MapState) -> MapState) -> Unit = {},
    mapRouteViewModel: MapRouteViewModel? = null
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {

        composable(
            Routes.SPLASH,
            enterTransition = { fadeIn(animationSpec = tween(150)) },
            exitTransition = { fadeOut(animationSpec = tween(150)) }
        ) {
            SplashScreen(navController = navController)
        }

        composable(
            Routes.TELA_START,
            enterTransition = { pushEnter() },
            exitTransition = { pushExit() },
            popEnterTransition = { pushPopEnter() },
            popExitTransition = { pushPopExit() }
        ) {
            InitStepScreen(navController = navController)
        }

        composable(
            Routes.POLITICA_PRIVACIDADE,
            enterTransition = { pushEnter() },
            exitTransition = { pushExit() },
            popEnterTransition = { pushPopEnter() },
            popExitTransition = { pushPopExit() }
        ) {
            PrivacyPolicyScreen(navController = navController)
        }

        composable(
            Routes.HOME,
            enterTransition = {
                barEnter(targetState.destination.route, initialState.destination.route)
            },
            exitTransition = {
                barExit(targetState.destination.route, initialState.destination.route)
            },
            popEnterTransition = { wallSlideLeftEnter() },
            popExitTransition = { wallSlideRightExit() }
        ) {
            if (mapState != null) {
                HomeRouteScreen(
                    navController = navController,
                    bottomBarPadding = bottomBarPadding,
                    mapState = mapState,
                    onMapStateChange = onMapStateChange,
                    viewModel = mapRouteViewModel ?: return@composable
                )
            }
        }

        composable(
            Routes.MEUS_RECIBOS,
            enterTransition = {
                barEnter(targetState.destination.route, initialState.destination.route)
            },
            exitTransition = {
                barExit(targetState.destination.route, initialState.destination.route)
            },
            popEnterTransition = { wallSlideLeftEnter() },
            popExitTransition = { wallSlideRightExit() }
        ) {
            MyReceiptScreen(
                navController = navController,
                bottomBarPadding = bottomBarPadding,
            )
        }

        composable(
            Routes.DECLARATION,
            enterTransition = {
                barEnter(targetState.destination.route, initialState.destination.route)
            },
            exitTransition = {
                barExit(targetState.destination.route, initialState.destination.route)
            },
            popEnterTransition = { wallSlideLeftEnter() },
            popExitTransition = { wallSlideRightExit() }
        ) {
            DeclarationScreen(
                navController = navController,
                bottomBarPadding = bottomBarPadding,
            )
        }

        if (BuildConfig.DEBUG) {
            composable(
                Routes.TESTE,
                enterTransition = {
                    barEnter(targetState.destination.route, initialState.destination.route)
                },
                exitTransition = {
                    barExit(targetState.destination.route, initialState.destination.route)
                },
                popEnterTransition = { wallSlideLeftEnter() },
                popExitTransition = { wallSlideRightExit() }
            ) {
                TestScreen(
                    navController = navController,
                    bottomBarPadding = bottomBarPadding,
                )
            }
        }

        composable(
            Routes.CREATE_DECLARATION,
            enterTransition = { pushEnter() },
            exitTransition = { pushExit() },
            popEnterTransition = { pushPopEnter() },
            popExitTransition = { pushPopExit() }
        ) {
            DeclaracaoObitoScreen(
                navController = navController,
                bottomBarPadding = bottomBarPadding,
                onConcluir = {
                }
            )
        }

        composable(
            Routes.DECLARATION_GRID,
            enterTransition = { pushEnter() },
            exitTransition = { pushExit() },
            popEnterTransition = { pushPopEnter() },
            popExitTransition = { pushPopExit() }
        ) {
            DeclarationGridScreen(
                navController = navController,
                bottomBarPadding = bottomBarPadding,
            )
        }

        declarationSectionDescriptors.forEach { descriptor ->
            declarationSectionRoute(
                route = descriptor.route,
                navController = navController
            ) { draftViewModel ->
                descriptor.content(navController, draftViewModel)
            }
        }

        composable(
            Routes.MYCNPJ,
            arguments = listOf(),
            enterTransition = { pushEnter() },
            exitTransition = { pushExit() },
            popEnterTransition = { pushPopEnter() },
            popExitTransition = { pushPopExit() }
        ) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Routes.UPLOAD_FILE)
            }
            val uploadViewModel: UploadFileViewModel = viewModel(parentEntry)
            MyCnpjScreen(
                navController = navController,
                viewModel = uploadViewModel
            )
        }

        composable(
            Routes.RECIBO_DETALHE_ARG,
            arguments = listOf(
                navArgument("reciboId") { type = NavType.LongType }
            ),
            enterTransition = { pushEnter() },
            exitTransition = { pushExit() },
            popEnterTransition = { pushPopEnter() },
            popExitTransition = { pushPopExit() }
        ) { backStackEntry ->
            val reciboId = backStackEntry.arguments?.getLong("reciboId") ?: 0L
            ReciboDetalheScreen(
                navController = navController,
                reciboId = reciboId
            )
        }

        composable(
            Routes.RECIBO_SUCESSO_ARG,
            arguments = listOf(
                navArgument("reciboId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            ),
            enterTransition = { pushEnter() },
            exitTransition = { pushExit() },
            popEnterTransition = { pushPopEnter() },
            popExitTransition = { pushPopExit() }
        ) { backStackEntry ->
            val reciboId = backStackEntry.arguments?.getString("reciboId")
                .orEmpty().ifBlank { null }
            ReciboSucessoScreen(
                navController = navController,
                reciboId = reciboId
            )
        }

        composable(
            Routes.DECLARACAO_DETALHE_ARG,
            arguments = listOf(
                navArgument("declaracaoId") { type = NavType.LongType }
            ),
            enterTransition = { pushEnter() },
            exitTransition = { pushExit() },
            popEnterTransition = { pushPopEnter() },
            popExitTransition = { pushPopExit() }
        ) { backStackEntry ->
            val declaracaoId = backStackEntry.arguments?.getLong("declaracaoId") ?: 0L
            DeclaracaoDetalheScreen(
                navController = navController,
                declaracaoId = declaracaoId
            )
        }

        composable(
            Routes.DECLARACAO_SUCESSO,
            enterTransition = { pushEnter() },
            exitTransition = { pushExit() },
            popEnterTransition = { pushPopEnter() },
            popExitTransition = { pushPopExit() }
        ) {
            ReciboSucessoScreen(
                navController = navController,
                documentType = "Declaração de Óbito",
                backRoute = Routes.DECLARATION,
                shareTitle = "Compartilhar declaração"
            )
        }

        composable(
            Routes.UPLOAD_FILE,
            arguments = listOf(),
            enterTransition = { pushEnter() },
            exitTransition = { pushExit() },
            popEnterTransition = { pushPopEnter() },
            popExitTransition = { pushPopExit() }
        ) { backStackEntry ->
            UploadFileScreen(
                navController = navController,
                onNavigateToCNPJ = { navController.navigate(Routes.MYCNPJ) }
            )
        }

        composable(
            Routes.CRIAR_RECIBO,
            enterTransition = { pushEnter() },
            exitTransition = { pushExit() },
            popEnterTransition = { pushPopEnter() },
            popExitTransition = { pushPopExit() }
        ) { backStackEntry ->
            CreateReciboScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.PERFIL,
            enterTransition = {
                barEnter(targetState.destination.route, initialState.destination.route)
            },
            exitTransition = {
                barExit(targetState.destination.route, initialState.destination.route)
            },
            popEnterTransition = { wallSlideLeftEnter() },
            popExitTransition = { wallSlideRightExit() }
        ) {
            ProfileScreen(navController = navController)
        }

        composable(
            Routes.DADOS_PESSOAIS,
            enterTransition = { pushEnter() },
            exitTransition = { pushExit() },
            popEnterTransition = { pushPopEnter() },
            popExitTransition = { pushPopExit() }
        ) {
            ProfilePlaceholderScreen(
                title = "Dados pessoais",
                navController = navController
            )
        }

        composable(
            Routes.INFORMACOES_PROFISSIONAIS,
            enterTransition = { pushEnter() },
            exitTransition = { pushExit() },
            popEnterTransition = { pushPopEnter() },
            popExitTransition = { pushPopExit() }
        ) {
            InformacoesProfissionaisScreen(navController = navController)
        }

        composable(
            Routes.TERMOS_USO,
            enterTransition = { pushEnter() },
            exitTransition = { pushExit() },
            popEnterTransition = { pushPopEnter() },
            popExitTransition = { pushPopExit() }
        ) {
            LegalDocumentScreen(
                title = "Termos de uso",
                assetFileName = "termosUsoOffiline.txt",
                navController = navController
            )
        }

        composable(
            Routes.LICENCAS,
            enterTransition = { pushEnter() },
            exitTransition = { pushExit() },
            popEnterTransition = { pushPopEnter() },
            popExitTransition = { pushPopExit() }
        ) {
            LicenseScreen(navController = navController)
        }

        composable(
            route = Routes.SEARCH_DESTINATION + "?field={field}",
            enterTransition = { fadeIn(animationSpec = tween(150)) },
            exitTransition = { fadeOut(animationSpec = tween(150)) },
            popEnterTransition = { fadeIn(animationSpec = tween(150)) },
            popExitTransition = { fadeOut(animationSpec = tween(150)) },
            arguments = listOf(navArgument("field") { defaultValue = "destino" })
        ) { backStackEntry ->
            val field = backStackEntry.arguments?.getString("field") ?: "destino"
            SearchDestinationScreen(
                navController = navController,
                field = field,
                mapRouteViewModel = mapRouteViewModel
            )
        }
        composable(
            Routes.FINALIZAR_CONTA,
            enterTransition = { pushEnter() },
            exitTransition = { pushExit() },
            popEnterTransition = { pushPopEnter() },
            popExitTransition = { pushPopExit() }
        ) {
            FinishCreateAuthScreen(
                navController = navController,
            )
        }

        composable(
            Routes.LOGIN,
            enterTransition = { pushEnter() },
            exitTransition = { pushExit() },
            popEnterTransition = { pushPopEnter() },
            popExitTransition = { pushPopExit() }
        ) {
            LoginScreen(
                navController = navController,
            )
        }
    }
}

/**
 * Declara uma rota de seção da Declaração de Óbito. O [DeclarationDraftViewModel]
 * é escopado ao back stack entry do grid (DECLARATION_GRID), garantindo que todas
 * as seções compartilhem o mesmo rascunho em memória.
 */
private fun NavGraphBuilder.declarationSectionRoute(
    route: String,
    navController: NavHostController,
    content: @Composable (DeclarationDraftViewModel) -> Unit,
) {
    composable(
        route = route,
        enterTransition = { pushEnter() },
        exitTransition = { pushExit() },
        popEnterTransition = { pushPopEnter() },
        popExitTransition = { pushPopExit() }
    ) { backStackEntry ->
        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry(Routes.DECLARATION_GRID)
        }
        val draftViewModel: DeclarationDraftViewModel = viewModel(parentEntry)
        content(draftViewModel)
    }
}
