package com.w_via.features.utils

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ViaCepServiceTest {

    @Test
    fun `parse converte resposta valida do ViaCEP`() {
        val json = """
            {
              "cep": "01001-000",
              "logradouro": "Praça da Sé",
              "complemento": "lado ímpar",
              "bairro": "Sé",
              "localidade": "São Paulo",
              "uf": "SP"
            }
        """.trimIndent()

        val result = ViaCepService.parse(json)

        assertTrue(result.isSuccess)
        val endereco = result.getOrNull()!!
        assertEquals("Praça da Sé", endereco.logradouro)
        assertEquals("Sé", endereco.bairro)
        assertEquals("São Paulo", endereco.localidade)
        assertEquals("SP", endereco.uf)
    }

    @Test
    fun `parse falha quando o ViaCEP retorna erro`() {
        val json = """{"erro": true}"""
        val result = ViaCepService.parse(json)
        assertTrue(result.isFailure)
    }

    @Test
    fun `parse falha quando nao ha logradouro`() {
        val json = """{"cep":"00000-000","bairro":"","localidade":"","uf":""}"""
        val result = ViaCepService.parse(json)
        assertTrue(result.isFailure)
    }

    @Test
    fun `parse falha com json malformado`() {
        val result = ViaCepService.parse("não é json")
        assertTrue(result.isFailure)
    }

    @Test
    fun `buscar falha com CEP invalido sem chamar a rede`() {
        val result = runBlocking { ViaCepService.buscar("123") }
        assertTrue(result.isFailure)
    }

    @Test
    fun `buscar falha com CEP em branco`() {
        val result = runBlocking { ViaCepService.buscar("") }
        assertTrue(result.isFailure)
    }
}
