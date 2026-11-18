package com.w_via

import kotlinx.coroutines.flow.MutableStateFlow

object NavigationManager {
    val pendingRoute = MutableStateFlow<String?>(null)
}