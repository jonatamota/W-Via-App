package com.w_via.features.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CepFormatterTest {

    @Test
    fun `formata CEP com 8 digitos`() {
        assertEquals("01001-000", CepFormatter.format("01001000"))
    }

    @Test
    fun `mascara aplicada progressivamente`() {
        assertEquals("1", CepFormatter.format("1"))
        assertEquals("12345", CepFormatter.format("12345"))
        assertEquals("12345-6", CepFormatter.format("123456"))
        assertEquals("12345-678", CepFormatter.format("12345678"))
    }

    @Test
    fun `entrada ja formatada e normalizada`() {
        assertEquals("01001-000", CepFormatter.format("01001-000"))
    }

    @Test
    fun `entrada vazia retorna vazio`() {
        assertEquals("", CepFormatter.format(""))
        assertEquals("", CepFormatter.format(null))
    }

    @Test
    fun `entrada acima do limite e truncada`() {
        assertEquals("12345-678", CepFormatter.format("1234567890"))
    }

    @Test
    fun `unformat remove formatacao`() {
        assertEquals("01001000", CepFormatter.unformat("01001-000"))
    }

    @Test
    fun `isValid exige exatamente 8 digitos`() {
        assertTrue(CepFormatter.isValid("01001-000"))
        assertTrue(CepFormatter.isValid("12345678"))
        assertFalse(CepFormatter.isValid("12345-67"))
        assertFalse(CepFormatter.isValid(""))
        assertFalse(CepFormatter.isValid(null))
    }
}
