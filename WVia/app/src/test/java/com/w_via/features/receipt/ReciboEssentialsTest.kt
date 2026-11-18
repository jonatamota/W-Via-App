package com.w_via.features.receipt

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Testes para [ReciboEssentials].
 */
class ReciboEssentialsTest {

    @Test
    fun `step1Blocking retorna campos obrigatórios faltantes`() {
        val blocking = ReciboEssentials.step1Blocking("", "", "")
        assertEquals(listOf("Nome do contratante", "CPF/CNPJ do contratante", "Endereço do contratante"), blocking)
    }

    @Test
    fun `step1Blocking retorna lista vazia quando todos preenchidos`() {
        val blocking = ReciboEssentials.step1Blocking("João", "123.456.789-00", "Rua A, 123")
        assertEquals(emptyList<String>(), blocking)
    }

    @Test
    fun `step1Recommended retorna campos recomendados faltantes`() {
        val recommended = ReciboEssentials.step1Recommended("", "")
        assertEquals(listOf("Telefone do contratante", "E-mail do contratante"), recommended)
    }

    @Test
    fun `step2Blocking retorna campos obrigatórios faltantes`() {
        val blocking = ReciboEssentials.step2Blocking("", "", "", "", "", "", "")
        assertEquals(listOf(
            "Nome do falecido",
            "CPF ou RG do falecido",
            "Naturalidade do falecido",
            "UF do falecido",
            "Filiação do falecido (pai ou mãe)"
        ), blocking)
    }

    @Test
    fun `step2Blocking retorna lista vazia quando nome e um documento preenchidos`() {
        val blocking = ReciboEssentials.step2Blocking("Maria", "123.456.789-00", "", "São Paulo", "SP", "João", "")
        assertEquals(listOf("Naturalidade do falecido", "UF do falecido"), blocking)
    }

    @Test
    fun `step2Blocking retorna lista vazia quando nome e RG preenchidos`() {
        val blocking = ReciboEssentials.step2Blocking("Maria", "", "12.345.678-9", "São Paulo", "SP", "", "Maria Souza")
        assertEquals(listOf("Naturalidade do falecido", "UF do falecido"), blocking)
    }

    @Test
    fun `step2Recommended retorna campos recomendados faltantes`() {
        val recommended = ReciboEssentials.step2Recommended("", "")
        assertEquals(listOf("Idade do falecido", "Sexo do falecido"), recommended)
    }

    @Test
    fun `step3Blocking retorna campos obrigatórios faltantes`() {
        val blocking = ReciboEssentials.step3Blocking("", null, "")
        assertEquals(listOf(
            "Local de saída do funeral",
            "Dia do funeral",
            "Cemitério"
        ), blocking)
    }

    @Test
    fun `step3Blocking retorna lista vazia quando todos preenchidos`() {
        val blocking = ReciboEssentials.step3Blocking("Hospital A", LocalDate(2023, 1, 15), "Cemitério João Paulo II")
        assertEquals(emptyList<String>(), blocking)
    }

    @Test
    fun `step3Recommended retorna campos recomendados faltantes`() {
        val recommended = ReciboEssentials.step3Recommended("")
        assertEquals(listOf("Hora de saída do funeral"), recommended)
    }

    @Test
    fun `step4Blocking retorna campos obrigatórios faltantes`() {
        val blocking = ReciboEssentials.step4Blocking("", 0.0, "")
        assertEquals(listOf(
            "Descrição do serviço",
            "Valor total (maior que zero)",
            "Data de pagamento"
        ), blocking)
    }

    @Test
    fun `step4Blocking retorna lista vazia quando todos preenchidos`() {
        val blocking = ReciboEssentials.step4Blocking("Consulta médica", 150.0, "15/01/2023")
        assertEquals(emptyList<String>(), blocking)
    }

    @Test
    fun `step4Recommended retorna lista vazia`() {
        val recommended = ReciboEssentials.step4Recommended()
        assertEquals(emptyList<String>(), recommended)
    }
}