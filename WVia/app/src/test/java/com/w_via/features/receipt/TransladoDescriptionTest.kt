package com.w_via.features.receipt

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Testes para [buildTransladoDescription].
 */
class TransladoDescriptionTest {

    @Test
    fun `constroi translado completo com nome cidade e uf`() {
        val texto = buildTransladoDescription(
            localSaida = "Hospital Santa Casa",
            cidadeSaida = "São Paulo",
            ufSaida = "SP",
            localSepultamento = "Cemitério da Paz",
            cidadeSepultamento = "Campinas",
            ufSepultamento = "SP"
        )
        assertEquals(
            "TRANSLADO DE HOSPITAL SANTA CASA, SÃO PAULO - SP À CEMITÉRIO DA PAZ, CAMPINAS - SP",
            texto
        )
    }

    @Test
    fun `usa somente o local de saida quando ha apenas saída`() {
        val texto = buildTransladoDescription(
            localSaida = "Residência",
            cidadeSaida = "",
            ufSaida = "",
            localSepultamento = "",
            cidadeSepultamento = "",
            ufSepultamento = ""
        )
        assertEquals("TRANSLADO DE RESIDÊNCIA", texto)
    }

    @Test
    fun `usa somente o local de sepultamento quando ha apenas sepultamento`() {
        val texto = buildTransladoDescription(
            localSaida = "",
            cidadeSaida = "",
            ufSaida = "",
            localSepultamento = "Cemitério Municipal",
            cidadeSepultamento = "Piraju",
            ufSepultamento = "SP"
        )
        assertEquals("TRANSLADO À CEMITÉRIO MUNICIPAL, PIRAJU - SP", texto)
    }

    @Test
    fun `inclui cidade sem uf quando uf ausente`() {
        val texto = buildTransladoDescription(
            localSaida = "Hospital da Cidade",
            cidadeSaida = "São José dos Campos",
            ufSaida = "",
            localSepultamento = "Cemitério",
            cidadeSepultamento = "",
            ufSepultamento = "MG"
        )
        assertEquals(
            "TRANSLADO DE HOSPITAL DA CIDADE, SÃO JOSÉ DOS CAMPOS À CEMITÉRIO, MG",
            texto
        )
    }

    @Test
    fun `retorna vazio quando nao ha nenhum dado`() {
        val texto = buildTransladoDescription("", "", "", "", "", "")
        assertEquals("", texto)
    }

    @Test
    fun `usa apenas cidade e uf quando nome do local ausente`() {
        val texto = buildTransladoDescription(
            localSaida = "",
            cidadeSaida = "Itapetininga",
            ufSaida = "SP",
            localSepultamento = "",
            cidadeSepultamento = "",
            ufSepultamento = ""
        )
        assertEquals("TRANSLADO DE ITAPETININGA - SP", texto)
    }

    @Test
    fun `funeraria em branco nao gera virgula no translado`() {
        val texto = buildTransladoDescription(
            localSaida = "Hospital Santa Casa",
            cidadeSaida = "São Paulo",
            ufSaida = "SP",
            localSepultamento = "",
            cidadeSepultamento = "Campinas",
            ufSepultamento = "SP"
        )
        assertEquals(
            "TRANSLADO DE HOSPITAL SANTA CASA, SÃO PAULO - SP À CAMPINAS - SP",
            texto
        )
    }
}