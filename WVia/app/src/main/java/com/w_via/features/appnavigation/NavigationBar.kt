package com.w_via.features.appnavigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.w_via.BuildConfig
import com.w_via.R.drawable.ic_bug_report_24dp
import com.w_via.R.drawable.ic_location_on_24dp
import com.w_via.R.drawable.ic_doc_svgrepo_com
import com.w_via.R.drawable.ic_do
import com.w_via.R.drawable.ic_identity_platform_24dp
import com.w_via.features.routes.Routes
import com.w_via.ui.theme.colorBrand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigation(
    startDestination: String,
    navController: NavController, userScaleFactor: Float = 1f,
) {
    val navItemList = listOf(
        BottomNavItem(Routes.PERFIL, "Perfil", ic_identity_platform_24dp, ic_identity_platform_24dp),
        BottomNavItem(Routes.HOME, "Rotas", ic_location_on_24dp, ic_location_on_24dp),
        BottomNavItem(Routes.MEUS_RECIBOS, "Recibos", ic_doc_svgrepo_com, ic_doc_svgrepo_com),
        BottomNavItem(Routes.DECLARATION, "Declarações", ic_do, ic_do),
    ) + if (BuildConfig.DEBUG) {
        listOf(BottomNavItem(Routes.TESTE, "Testes", ic_bug_report_24dp, ic_bug_report_24dp))
    } else {
        emptyList()
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selectedIndex = remember(currentRoute) {
        navItemList.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    }

    // Peso maior para o item selecionado; os demais dividem o restante
    val selectedWeight = 1.6f
    val unselectedWeight = 1f

    val animatedWeights = navItemList.indices.map { index ->
        val target = if (index == selectedIndex) selectedWeight else unselectedWeight
        animateFloatAsState(
            targetValue = target,
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
            label = "itemWeight$index"
        )
    }

    val corActive = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
    val corInative = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 4.dp)
                .height(58.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItemList.forEachIndexed { index, navItem ->
                val isSelected = currentRoute == navItem.route

                CompositionLocalProvider(LocalRippleConfiguration provides null) {
                    Box(
                        modifier = Modifier
                            .weight(animatedWeights[index].value)
                            .height(54.dp)
                            .padding(vertical = 4.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if (!isSelected) {
                                    navController.navigate(navItem.route) {
                                        popUpTo(Routes.HOME) {
                                            inclusive = navItem.route == Routes.HOME
                                            saveState = false
                                        }
                                        launchSingleTop = true
                                        restoreState = false
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (isSelected) navItem.selectedIconRes else navItem.iconRes
                                ),
                                contentDescription = navItem.label,
                                modifier = Modifier.size(20.dp),
                                tint = if (isSelected) corActive
                                else corInative
                            )
                            if (isSelected) {
                                Text(
                                    text = navItem.label,
                                    color = corActive,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationPreview() {
    val navController = rememberNavController()
    BottomNavigation(
        startDestination = Routes.HOME,
        navController = navController
    )
}
