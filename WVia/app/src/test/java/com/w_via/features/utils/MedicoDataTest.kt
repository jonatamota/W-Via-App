package com.w_via.features.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MedicoDataTest {

    private val medicos = listOf(
        MedicoBasico(estado = "SP", nCRM = "123456", nome = "Dr. Carlos Alberto Ferreira"),
        MedicoBasico(estado = "SP", nCRM = "098765", nome = "Dra. Mariana Souza Lima"),
        MedicoBasico(estado = "RJ", nCRM = "554433", nome = "Dr. Pedro Henrique Alves"),
        MedicoBasico(estado = "MG", nCRM = "887766", nome = "Dr. Ricardo Nunes")
    )

    @Test
    fun `buscarPor retorna nome quando UF e CRM coincidem`() {
        assertEquals(
            "Dr. Carlos Alberto Ferreira",
            MedicoData.buscarPor(medicos, "SP", "123456")
        )
    }

    @Test
    fun `buscarPor ignora caixa da UF`() {
        assertEquals(
            "Dr. Carlos Alberto Ferreira",
            MedicoData.buscarPor(medicos, "sp", "123456")
        )
    }

    @Test
    fun `buscarPor ignora pontuacao e zeros a esquerda do CRM`() {
        assertEquals(
            "Dra. Mariana Souza Lima",
            MedicoData.buscarPor(medicos, "SP", "98-765")
        )
    }

    @Test
    fun `buscarPor retorna null quando UF nao coincide`() {
        assertNull(MedicoData.buscarPor(medicos, "BA", "123456"))
    }

    @Test
    fun `buscarPor retorna null quando CRM nao coincide`() {
        assertNull(MedicoData.buscarPor(medicos, "SP", "999999"))
    }

    @Test
    fun `buscarPor retorna null quando UF ou CRM vazios`() {
        assertNull(MedicoData.buscarPor(medicos, "", "123456"))
        assertNull(MedicoData.buscarPor(medicos, "SP", ""))
        assertNull(MedicoData.buscarPor(medicos, null, "123456"))
    }

    @Test
    fun `loadFromJson converte o JSON em lista`() {
        val json = """
            [
              { "estado": "SP", "nCRM": "123456", "nome": "Dr. Exemplo" }
            ]
        """.trimIndent()
        val lista = MedicoData.loadFromJson(json)
        assertEquals(1, lista.size)
        assertEquals("SP", lista[0].estado)
        assertEquals("123456", lista[0].nCRM)
        assertEquals("Dr. Exemplo", lista[0].nome)
    }
}
