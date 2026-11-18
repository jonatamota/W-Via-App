package com.w_via.features.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Testes para [RGFormatter.formatWithCursor].
 */
class RGFormatterTest {

    @Test
    fun `formatWithCursor insere ponto após 2 dígitos`() {
        val (formatted, cursor) = RGFormatter.formatWithCursor("12", 2)
        assertEquals("12.", formatted)
        assertEquals(3, cursor) // após o ponto
    }

    @Test
    fun `formatWithCursor insere segundo ponto após 5 dígitos`() {
        val (formatted, cursor) = RGFormatter.formatWithCursor("12345", 5)
        assertEquals("12.345.", formatted)
        assertEquals(6, cursor)
    }

    @Test
    fun `formatWithCursor insere traço após 8 dígitos`() {
        val (formatted, cursor) = RGFormatter.formatWithCursor("12345678", 8)
        assertEquals("12.345.678.", formatted)
        assertEquals(9, cursor)
    }

    @Test
    fun `formatWithCursor insere último dígito e posiciona cursor corretamente`() {
        val (formatted, cursor) = RGFormatter.formatWithCursor("123456789", 9)
        assertEquals("12.345.678-9", formatted)
        assertEquals(9, cursor) // fim da string (último dígito)
    }

    @Test
    fun `formatWithCursor lida com entrada vazia`() {
        val (formatted, cursor) = RGFormatter.formatWithCursor("", 0)
        assertEquals("", formatted)
        assertEquals(0, cursor)
    }
}