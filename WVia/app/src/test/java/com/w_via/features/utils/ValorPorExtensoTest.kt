package com.w_via.features.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class ValorPorExtensoTest {

    @Test
    fun `valor inteiro em reais`() {
        assertEquals("um mil e quinhentos reais", valorPorExtenso(1500.0))
    }

    @Test
    fun `um mil reais`() {
        assertEquals("um mil reais", valorPorExtenso(1000.0))
    }

    @Test
    fun `um real`() {
        assertEquals("um real", valorPorExtenso(1.0))
    }

    @Test
    fun `valor com centavos`() {
        assertEquals(
            "cento e vinte e três reais e quarenta e cinco centavos",
            valorPorExtenso(123.45)
        )
    }

    @Test
    fun `apenas centavos`() {
        assertEquals("cinquenta centavos", valorPorExtenso(0.50))
    }

    @Test
    fun `um centavo`() {
        assertEquals("um centavo", valorPorExtenso(0.01))
    }

    @Test
    fun `zero`() {
        assertEquals("zero reais", valorPorExtenso(0.0))
    }

    @Test
    fun `milhar com centena`() {
        assertEquals("um mil e cem reais", valorPorExtenso(1100.0))
    }

    @Test
    fun `dezenas de milhar`() {
        assertEquals("trinta e quatro mil e quinhentos reais", valorPorExtenso(34500.0))
    }

    @Test
    fun `milhão`() {
        assertEquals("um milhão e duzentos mil reais", valorPorExtenso(1_200_000.0))
    }

    @Test
    fun `centena exata`() {
        assertEquals("cem reais", valorPorExtenso(100.0))
        assertEquals("duzentos reais", valorPorExtenso(200.0))
    }

    @Test
    fun `dezena composta`() {
        assertEquals("vinte e um reais", valorPorExtenso(21.0))
        assertEquals("noventa e nove reais", valorPorExtenso(99.0))
    }
}
