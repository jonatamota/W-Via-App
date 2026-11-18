package com.w_via.features.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
//fun formatarDataPadrao(date: Any?): String {
//    return when (date) {
//        is Timestamp -> {
//            val instant = Instant.fromEpochSeconds(date.seconds, date.nanoseconds)
//            val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
//            formatarLocalDate(localDate)
//        }
//        is String -> {
//            try {
//                val localDate = LocalDate.parse(date)
//                formatarLocalDate(localDate)
//            } catch (e: Exception) {
//                date
//            }
//        }
//        is LocalDate -> formatarLocalDate(date)
//        is Instant -> {
//            val localDate = date.toLocalDateTime(TimeZone.currentSystemDefault()).date
//            formatarLocalDate(localDate)
//        }
//        else -> ""
//    }
//}

private fun formatarLocalDate(date: LocalDate): String {
    val anoAtual = kotlin.time.Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .year

    val dia = date.dayOfMonth.toString().padStart(2, '0')

    return if (date.year == anoAtual) {
        val nomeMes = date.month.name
            .lowercase()
            .replaceFirstChar { it.uppercase() }
            .let { nomeMesPortugues(date.monthNumber) }
        "$dia de $nomeMes"
    } else {
        val mes = date.monthNumber.toString().padStart(2, '0')
        "$dia/$mes/${date.year}"
    }
}

private fun nomeMesPortugues(mes: Int): String = when (mes) {
    1  -> "Janeiro"
    2  -> "Fevereiro"
    3  -> "Março"
    4  -> "Abril"
    5  -> "Maio"
    6  -> "Junho"
    7  -> "Julho"
    8  -> "Agosto"
    9  -> "Setembro"
    10 -> "Outubro"
    11 -> "Novembro"
    12 -> "Dezembro"
    else -> ""
}