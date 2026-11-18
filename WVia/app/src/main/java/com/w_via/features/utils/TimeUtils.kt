package com.w_via.features.utils

import android.annotation.SuppressLint

@SuppressLint("DefaultLocale")
fun Float.formatAsTime(): String {
    if (this < 0) return "00:00" // Caso o valor seja negativo, retorna um valor padrão
    val hours = this.toInt()
    val minutes = ((this - hours) * 60).toInt()
    return String.format("%02d:%02d", hours, minutes)
}
fun formatHoras(valor: String): String {
    val double = valor.toDoubleOrNull() ?: return "0h"
    val horas = double.toInt()
    val minutos = ((double - horas) * 60).toInt()
    return if (minutos > 0) "${horas}h ${minutos}min" else "${horas}h"
}
fun converterHoraParaDouble(hora: String): Double {
    return try {
        val parts = hora.split(":")
        val horas = parts[0].toIntOrNull() ?: 0
        val minutos = parts[1].toIntOrNull() ?: 0
        horas + (minutos / 60.0)
    } catch (e: Exception) {
        0.0
    }
}
fun formatarHorasParaTexto(valor: Double): String {
    val horas = valor.toInt()
    val minutos = ((valor - horas) * 60).toInt()
    return "${horas}h ${minutos}min"
}

fun Double.toHoraMinFormat(): String {
    val horas = this.toInt()
    val minutos = ((this - horas) * 60).toInt()

    return when {
        minutos == 0 -> "${horas}h"
        horas == 0 -> "${minutos}min"
        else -> "${horas}h ${minutos}min"
    }
}

fun formatarMinutosParaTexto(minutos: Int): String {
    val horas = minutos / 60
    val minutosRestantes = minutos % 60
    return if (horas > 0) "${horas}h ${minutosRestantes}min" else "${minutosRestantes}min"
}

// Função utilitária para formatar Float para HH:mm
@SuppressLint("DefaultLocale")
fun formatTime(time: Float?): String {
    if (time == null || time == 0f) return "--:--"
    val hours = time.toInt()
    val minutes = ((time - hours) * 60).toInt()
    return String.format("%02d:%02d", hours, minutes)
}

// Função para verificar se o campo deve estar desabilitado (mais apagado)
fun isFieldDisabled(index: Int, isInicio: Boolean, valueInicio: Float?, valueFim: Float?): Boolean {
    // Só desabilita o início do segundo período se ele tem término preenchido
    if (index == 1 && isInicio) {
        return valueFim != null && valueFim > 0f
    }

    // Só desabilita o término do primeiro período se o segundo período tem término preenchido
    if (index == 0 && !isInicio) {
        // Esta verificação será feita na tela passando o valor do segundo período
        return false // Por padrão, não desabilita
    }

    return false
}
