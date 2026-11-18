package com.w_via.features.utils

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class AgeCalculatorTest {

    private fun age(birth: String, reference: String): AgeCalculator.Age =
        AgeCalculator.ageOn(LocalDate.parse(birth), LocalDate.parse(reference))

    // --- casos básicos ---

    @Test
    fun `idade exata no aniversario`() {
        assertEquals(AgeCalculator.Age(36, 0, 0), age("1990-05-10", "2026-05-10"))
    }

    @Test
    fun `recém-nascido no dia do nascimento`() {
        assertEquals(AgeCalculator.Age(0, 0, 0), age("2026-08-04", "2026-08-04"))
    }

    @Test
    fun `um dia antes do aniversario conta 11 meses e 29 dias`() {
        assertEquals(AgeCalculator.Age(35, 11, 29), age("1990-05-10", "2026-05-09"))
    }

    @Test
    fun `mês exato pelo dia de nascimento`() {
        assertEquals(AgeCalculator.Age(0, 1, 0), age("2026-01-10", "2026-02-10"))
    }

    @Test
    fun `dia de nascimento ainda nao chegou no mes`() {
        assertEquals(AgeCalculator.Age(0, 0, 20), age("2026-01-10", "2026-01-30"))
    }

    @Test
    fun `virada de ano conta um dia`() {
        assertEquals(AgeCalculator.Age(0, 0, 1), age("2025-12-31", "2026-01-01"))
    }

    // --- ano bissexto (29/02) ---

    @Test
    fun `nascido em 29 de fevereiro faz aniversario em 28 de fevereiro`() {
        assertEquals(AgeCalculator.Age(0, 11, 30), age("2020-02-29", "2021-02-28"))
        assertEquals(AgeCalculator.Age(0, 11, 30), age("2016-02-29", "2017-02-28"))
    }

    @Test
    fun `nascido em 29 de fevereiro completa anos em ano bissexto`() {
        assertEquals(AgeCalculator.Age(4, 0, 0), age("2020-02-29", "2024-02-29"))
    }

    @Test
    fun `nascido em 29 de fevereiro em ano nao bissexto`() {
        assertEquals(AgeCalculator.Age(24, 11, 30), age("2000-02-29", "2025-02-28"))
        assertEquals(AgeCalculator.Age(25, 0, 1), age("2000-02-29", "2025-03-01"))
    }

    // --- fim de mês ---

    @Test
    fun `nascido no ultimo dia do mes em mes mais curto`() {
        assertEquals(AgeCalculator.Age(0, 0, 28), age("2021-01-31", "2021-02-28"))
    }

    @Test
    fun `meses desiguais fecham o periodo com o fim do mes`() {
        assertEquals(AgeCalculator.Age(2, 0, 30), age("2024-03-31", "2026-04-30"))
        assertEquals(AgeCalculator.Age(2, 0, 1), age("2024-03-31", "2026-04-01"))
    }

    // --- período maior ---

    @Test
    fun `idade de 25 anos`() {
        assertEquals(AgeCalculator.Age(25, 0, 0), age("2001-08-04", "2026-08-04"))
    }
}
