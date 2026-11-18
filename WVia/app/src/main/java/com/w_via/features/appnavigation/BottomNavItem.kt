package com.w_via.features.appnavigation

import androidx.annotation.DrawableRes

data class BottomNavItem(
    val route: String,
    val label: String,
    @DrawableRes val iconRes: Int, // Ícone para estado não selecionado
    @DrawableRes val selectedIconRes: Int, // Ícone para estado selecionado
)
