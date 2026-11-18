package com.w_via.features.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Testes para [InssFormatter.formatWithCursor].
 */
class InssFormatterTest {

    @Test
    fun `formatWithCursor lida com entrada vazia`() {
        val (formatted, cursor) = InssFormatter.formatWithCursor("", 0)
        assertEquals("", formatted)
        assertEquals(0, cursor)
    }

    @Test
    fun `formatWithCursor insere primeiros zeros`() {
        val (formatted, cursor) = InssFormatter.formatWithCursor("1", 1)
        assertEquals("000.00000.00-0", formatted)
        assertEquals(1, cursor) // após o primeiro dígito
    }

    @Test
    fun `formatWithCursor avança o cursor corretamente`() {
        // Testando alguns pontos chave
        val (formatted1, cursor1) = InssFormatter.formatWithCursor("12", 2)
        assertEquals("000.00000.12-0", formatted1)
        assertEquals(3, cursor1) // após o primeiro zero do grupo
        
        val (formatted2, cursor2) = InssFormatter.formatWithCursor("123", 3)
        assertEquals("000.00000.123-0", formatted2)
        assertEquals(4, cursor2)
        
        val (formatted3, cursor3) = InssFormatter.formatWithCursor("1234", 4)
        assertEquals("000.00000.1234-0", formatted3)
        assertEquals(5, cursor3)
    }
}