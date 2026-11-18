package com.w_via.features.declaration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeclarationFieldErrorsTest {

    // --- CPF ---

    @Test
    fun `cpf vazio nao gera erro`() {
        assertNull(DeclarationFieldErrors.cpf(""))
    }

    @Test
    fun `cpf em digitacao nao gera erro`() {
        assertNull(DeclarationFieldErrors.cpf("123"))
        assertNull(DeclarationFieldErrors.cpf("123.456.78"))
    }

    @Test
    fun `cpf invalido com digitos suficientes gera erro`() {
        assertEquals("CPF inválido — informe os 11 dígitos", DeclarationFieldErrors.cpf("123456789012"))
    }

    @Test
    fun `cpf valido nao gera erro`() {
        assertNull(DeclarationFieldErrors.cpf("123.456.789-00"))
    }

    // --- CEP ---

    @Test
    fun `cep vazio nao gera erro`() {
        assertNull(DeclarationFieldErrors.cep(""))
    }

    @Test
    fun `cep em digitacao nao gera erro`() {
        assertNull(DeclarationFieldErrors.cep("010"))
    }

    @Test
    fun `cep invalido com digitos suficientes gera erro`() {
        assertEquals("CEP inválido — informe os 8 dígitos", DeclarationFieldErrors.cep("010000009"))
    }

    @Test
    fun `cep valido nao gera erro`() {
        assertNull(DeclarationFieldErrors.cep("01000-000"))
    }

    // --- Telefone ---

    @Test
    fun `telefone vazio nao gera erro`() {
        assertNull(DeclarationFieldErrors.phone(""))
    }

    @Test
    fun `telefone em digitacao nao gera erro`() {
        assertNull(DeclarationFieldErrors.phone("(11) 2345"))
    }

    @Test
    fun `telefone invalido com digitos suficientes gera erro`() {
        assertEquals("Telefone inválido — informe DDD + número", DeclarationFieldErrors.phone("(11) 2345-678901"))
    }

    @Test
    fun `telefone valido nao gera erro`() {
        assertNull(DeclarationFieldErrors.phone("(11) 98765-4321"))
    }

    // --- E-mail ---

    @Test
    fun `email vazio nao gera erro`() {
        assertNull(DeclarationFieldErrors.email(""))
    }

    @Test
    fun `email invalido gera erro`() {
        assertEquals("E-mail inválido", DeclarationFieldErrors.email("joao@"))
        assertEquals("E-mail inválido", DeclarationFieldErrors.email("sem-arroba"))
    }

    @Test
    fun `email valido nao gera erro`() {
        assertNull(DeclarationFieldErrors.email("joao@example.com"))
    }
}
