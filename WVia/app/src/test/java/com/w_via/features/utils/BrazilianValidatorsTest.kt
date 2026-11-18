package com.w_via.features.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrazilianValidatorsTest {

    // --- CPF ---

    @Test
    fun `cpf valido`() {
        assertTrue(BrazilianValidators.isCpfValid("529.982.247-25"))
        assertTrue(BrazilianValidators.isCpfValid("12345678909"))
        assertTrue(BrazilianValidators.isCpfValid("111.444.777-35"))
        assertTrue(BrazilianValidators.isCpfValid("168.995.350-09"))
        assertTrue(BrazilianValidators.isCpfValid("04176117060"))
    }

    @Test
    fun `cpf invalido`() {
        assertFalse(BrazilianValidators.isCpfValid("529.982.247-26"))
        assertFalse(BrazilianValidators.isCpfValid("12345678901"))
        assertFalse(BrazilianValidators.isCpfValid("12345678900"))
        assertFalse(BrazilianValidators.isCpfValid("111.111.111-11"))
        assertFalse(BrazilianValidators.isCpfValid("000.000.000-00"))
        assertFalse(BrazilianValidators.isCpfValid("123.456.789-0"))
        assertFalse(BrazilianValidators.isCpfValid(""))
    }

    // --- RG ---

    @Test
    fun `rg valido`() {
        assertTrue(BrazilianValidators.isRgValid("12.345.678-9"))
        assertTrue(BrazilianValidators.isRgValid("12345678"))
        assertTrue(BrazilianValidators.isRgValid("123456789"))
        assertTrue(BrazilianValidators.isRgValid("1234567X"))
        assertTrue(BrazilianValidators.isRgValid("1234567890"))
    }

    @Test
    fun `rg invalido`() {
        assertFalse(BrazilianValidators.isRgValid("123"))
        assertFalse(BrazilianValidators.isRgValid("123456789012"))
        assertFalse(BrazilianValidators.isRgValid("abcdefgh"))
        assertFalse(BrazilianValidators.isRgValid("1234A678"))
        assertFalse(BrazilianValidators.isRgValid(""))
    }

    // --- Telefone ---

    @Test
    fun `telefone valido`() {
        assertTrue(BrazilianValidators.isPhoneValid("(11) 91234-5678"))
        assertTrue(BrazilianValidators.isPhoneValid("(11) 2345-6789"))
        assertTrue(BrazilianValidators.isPhoneValid("11987654321"))
        assertTrue(BrazilianValidators.isPhoneValid("+55 11 98765-4321"))
        assertTrue(BrazilianValidators.isPhoneValid(" 41 99999-0000 "))
    }

    @Test
    fun `telefone invalido`() {
        assertFalse(BrazilianValidators.isPhoneValid("(11) 2345-678"))
        assertFalse(BrazilianValidators.isPhoneValid("11234567890"))
        assertFalse(BrazilianValidators.isPhoneValid("01023456789"))
        assertFalse(BrazilianValidators.isPhoneValid("11111111111"))
        assertFalse(BrazilianValidators.isPhoneValid(""))
    }

    // --- E-mail ---

    @Test
    fun `email valido`() {
        assertTrue(BrazilianValidators.isEmailValid("joao@example.com"))
        assertTrue(BrazilianValidators.isEmailValid("joao.silva@mail.org.br"))
        assertTrue(BrazilianValidators.isEmailValid("usuario+tag@dominio.co"))
        assertTrue(BrazilianValidators.isEmailValid("a.b@sub.dominio.com.br"))
    }

    @Test
    fun `email invalido`() {
        assertFalse(BrazilianValidators.isEmailValid(""))
        assertFalse(BrazilianValidators.isEmailValid("sem-arroba"))
        assertFalse(BrazilianValidators.isEmailValid("@sem-usuario.com"))
        assertFalse(BrazilianValidators.isEmailValid("usuario@semdoponto"))
        assertFalse(BrazilianValidators.isEmailValid("usuario@dominio."))
        assertFalse(BrazilianValidators.isEmailValid("usuario com espaco@dominio.com"))
    }
}
