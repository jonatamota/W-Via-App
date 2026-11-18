package com.w_via.features.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Testes para [CPFFormatter.formatWithCursor].
 */
class CPFFormatterTest {

    @Test
    fun `formatWithCursor insere ponto após 3 dígitos`() {
        val (formatted, cursor) = CPFFormatter.formatWithCursor("123", 3)
        assertEquals("123.", formatted)
        assertEquals(4, cursor) // após o ponto
    }

    @Test
    fun `formatWithCursor insere segundo ponto após 6 dígitos`() {
        val (formatted, cursor) = CPFFormatter.formatWithCursor("123456", 6)
        assertEquals("123.456.", formatted)
        assertEquals(7, cursor)
    }

    @Test
    fun `formatWithCursor insere traço após 9 dígitos`() {
        val (formatted, cursor) = CPFFormatter.formatWithCursor("123456789", 9)
        assertEquals("123.456.789.", formatted)
        assertEquals(10, cursor)
    }

    @Test
    fun `formatWithCursor insere último dígito e posiciona cursor corretamente`() {
        val (formatted, cursor) = CPFFormatter.formatWithCursor("12345678901", 11)
        assertEquals("123.456.789-01", formatted)
        assertEquals(12, cursor) // fim da string
    }

    @Test
    fun `formatWithCursor apaga dígito e move cursor para trás`() {
        // Começando com "123.456.789-0" (10 dígitos formatados)
        val (formatted, cursor) = CPFFormatter.formatWithCursor("1234567890", 10)
        assertEquals("123.456.789-0", formatted)
        assertEquals(11, cursor) // após o 0
        
        // Agora apaga o último dígito (volta para 9 dígitos)
        val (formatted2, cursor2) = CPFFormatter.formatWithCursor("123456789", 9)
        assertEquals("123.456.789.", formatted2)
        assertEquals(10, cursor2) // após o segundo ponto
    }

    @Test
    fun `formatWithCursor lida com entrada vazia`() {
        val (formatted, cursor) = CPFFormatter.formatWithCursor("", 0)
        assertEquals("", formatted)
        assertEquals(0, cursor)
    }
}