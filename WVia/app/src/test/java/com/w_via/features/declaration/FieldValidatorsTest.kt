package com.w_via.features.declaration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FieldValidatorsTest {

    // --- digitCount ---

    @Test
    fun `digitCount conta apenas digitos`() {
        assertEquals(11, FieldValidators.digitCount("123.456.789-00"))
        assertEquals(8, FieldValidators.digitCount("01000-000"))
        assertEquals(0, FieldValidators.digitCount(""))
        assertEquals(0, FieldValidators.digitCount("abc"))
    }

    // --- CPF ---

    @Test
    fun `cpf valido com 11 digitos`() {
        assertTrue(FieldValidators.isCpfValid("123.456.789-00"))
        assertTrue(FieldValidators.isCpfValid("12345678900"))
    }

    @Test
    fun `cpf invalido com quantidade errada de digitos`() {
        assertFalse(FieldValidators.isCpfValid("123.456.789-0"))
        assertFalse(FieldValidators.isCpfValid("123456789001"))
        assertFalse(FieldValidators.isCpfValid(""))
    }

    // --- CEP ---

    @Test
    fun `cep valido com 8 digitos`() {
        assertTrue(FieldValidators.isCepValid("01000-000"))
        assertTrue(FieldValidators.isCepValid("01000000"))
    }

    @Test
    fun `cep invalido com quantidade errada de digitos`() {
        assertFalse(FieldValidators.isCepValid("01000-00"))
        assertFalse(FieldValidators.isCepValid("010000000"))
        assertFalse(FieldValidators.isCepValid(""))
    }

    // --- Telefone ---

    @Test
    fun `telefone valido com 10 ou 11 digitos`() {
        assertTrue(FieldValidators.isPhoneValid("(11) 2345-6789"))
        assertTrue(FieldValidators.isPhoneValid("(11) 98765-4321"))
    }

    @Test
    fun `telefone invalido com menos de 10 digitos`() {
        assertFalse(FieldValidators.isPhoneValid("(11) 2345-678"))
        assertFalse(FieldValidators.isPhoneValid(""))
    }

    // --- E-mail ---

    @Test
    fun `email valido`() {
        assertTrue(FieldValidators.isEmailValid("joao@example.com"))
        assertTrue(FieldValidators.isEmailValid("joao.silva@mail.org.br"))
    }

    @Test
    fun `email invalido`() {
        assertFalse(FieldValidators.isEmailValid(""))
        assertFalse(FieldValidators.isEmailValid("sem-arroba"))
        assertFalse(FieldValidators.isEmailValid("@sem-usuario.com"))
        assertFalse(FieldValidators.isEmailValid("usuario@semdoponto"))
        assertFalse(FieldValidators.isEmailValid("usuario@dominio."))
    }
}
