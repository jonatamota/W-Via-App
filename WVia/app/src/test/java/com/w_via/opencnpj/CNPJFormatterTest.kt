package com.w_via.opencnpj

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Testes para [CNPJFormatter.formatWithCursor].
 */
class CNPJFormatterTest {

    @Test
    fun `formatWithCursor insere ponto após 2 dígitos`() {
        val (formatted, cursor) = CNPJFormatter.formatWithCursor("12", 2)
        assertEquals("12.", formatted)
        assertEquals(3, cursor) // após o ponto
    }

    @Test
    fun `formatWithCursor insere segundo ponto após 5 dígitos`() {
        val (formatted, cursor) = CNPJFormatter.formatWithCursor("12345", 5)
        assertEquals("12.345.", formatted)
        assertEquals(6, cursor)
    }

    @Test
    fun `formatWithCursor insere barra após 8 dígitos`() {
        val (formatted, cursor) = CNPJFormatter.formatWithCursor("12345678", 8)
        assertEquals("12.345.678/", formatted)
        assertEquals(9, cursor)
    }

    @Test
    fun `formatWithCursor insere traço após 12 dígitos`() {
        val (formatted, cursor) = CNPJFormatter.formatWithCursor("123456789012", 12)
        assertEquals("12.345.678/9012.", formatted)
        assertEquals(13, cursor)
    }

    @Test
    fun `formatWithCursor insere últimos dois dígitos e posiciona cursor corretamente`() {
        val (formatted, cursor) = CNPJFormatter.formatWithCursor("12345678901234", 14)
        assertEquals("12.345.678/9012-34", formatted)
        assertEquals(14, cursor) // fim da string
    }

    @Test
    fun `formatWithCursor lida com letras (CNPJ alfanumérico)`() {
        // Testando com letras que devem ser convertidas para maiúsculas
        val (formatted, cursor) = CNPJFormatter.formatWithCursor("ab12cd34ef56gh", 14)
        assertEquals("AB.12C.D34/EF56-GH", formatted)
        assertEquals(14, cursor)
        
        // Testando entrada parcial
        val (formatted2, cursor2) = CNPJFormatter.formatWithCursor("ab12", 4)
        assertEquals("AB.12.", formatted2)
        assertEquals(4, cursor2) // após o segundo ponto
    }

    @Test
    fun `formatWithCursor lida com entrada vazia`() {
        val (formatted, cursor) = CNPJFormatter.formatWithCursor("", 0)
        assertEquals("", formatted)
        assertEquals(0, cursor)
    }
}