package com.w_via.features.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Cálculo de idade completa (anos, meses e dias) entre duas datas.
 *
 * A semântica segue o [periodUntil] do kotlinx-datetime: anos e meses são
 * contados por inteiro a partir do mês/dia de nascimento, e os dias restantes
 * fecham o período (mesmos resultados do `java.time.Period`).
 */
object AgeCalculator {

    /** Idade expressa em anos, meses e dias completos. */
    data class Age(
        val years: Int,
        val months: Int,
        val days: Int
    )

    /**
     * Idade completa de alguém nascido em [birthDate] vista em [referenceDate].
     *
     * Exige [birthDate] anterior ou igual a [referenceDate]; caso contrário o
     * resultado é indefinido (período negativo).
     */
    fun ageOn(birthDate: LocalDate, referenceDate: LocalDate): Age {
        val period = birthDate.periodUntil(referenceDate)
        return Age(
            years = period.years,
            months = period.months,
            days = period.days
        )
    }

    @OptIn(ExperimentalTime::class)
    private fun today(): LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    /**
     * Converte uma data de nascimento em texto para [LocalDate], aceitando
     * tanto o formato brasileiro `dd/MM/yyyy` quanto o ISO `yyyy-MM-dd`.
     * Retorna null quando o texto está vazio ou é uma data inválida.
     */
    fun parse(value: String): LocalDate? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null

        val parts = if (trimmed.contains('/')) {
            val p = trimmed.split('/')
            if (p.size != 3) return null
            listOf(p[2], p[1], p[0])
        } else {
            val p = trimmed.split('-')
            if (p.size != 3) return null
            listOf(p[0], p[1], p[2])
        }

        return runCatching {
            LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        }.getOrNull()
    }

    /** Formata [date] no padrão brasileiro `dd/MM/yyyy`. */
    fun format(date: LocalDate): String =
        "%02d/%02d/%04d".format(date.dayOfMonth, date.monthNumber, date.year)

    /**
     * Idade completa de quem nasceu em [birthText] vista em [referenceDate]
     * (hoje quando não informada). Null quando a data é inválida.
     */
    fun ageOnText(
        birthText: String,
        referenceDate: LocalDate = today()
    ): Age? = parse(birthText)?.let { ageOn(it, referenceDate) }

    /** Apenas os anos completos ("45"), ou "" quando a data é inválida/vazia. */
    fun yearsOnly(
        birthText: String,
        referenceDate: LocalDate = today()
    ): String = ageOnText(birthText, referenceDate)?.years?.toString() ?: ""

    /**
     * Rótulo completo da idade ("45 anos, 3 meses e 2 dias"), pronto para
     * exibição somente leitura. Null quando a data é inválida/vazia.
     */
    fun fullAgeLabel(
        birthText: String,
        referenceDate: LocalDate = today()
    ): String? {
        val age = ageOnText(birthText, referenceDate) ?: return null
        return buildString {
            append(age.years).append(if (age.years == 1) " ano" else " anos")
            append(", ")
            append(age.months).append(if (age.months == 1) " mês" else " meses")
            append(" e ")
            append(age.days).append(if (age.days == 1) " dia" else " dias")
        }
    }
}
