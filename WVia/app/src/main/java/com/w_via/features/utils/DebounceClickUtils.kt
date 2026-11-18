package com.w_via.features.utils

object ClickDebounce {
    private var lastClickTime = 0L
    private const val CLICK_DELAY = 500L // milissegundos

    fun canClick(): Boolean {
        val currentTime = System.currentTimeMillis()
        val canClick = currentTime - lastClickTime > CLICK_DELAY
        if (canClick) {
            lastClickTime = currentTime
        }
        return canClick
    }
}
