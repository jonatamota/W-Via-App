package com.w_via.features.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AutocompleteLogicTest {

    private val options = listOf(
        "São Paulo",
        "São José do Rio Preto",
        "Santos",
        "Santa Catarina",
        "Salvador",
        "Rio de Janeiro",
        "Recife",
        "Belo Horizonte",
        "Brasília"
    )

    // --- normalize ---

    @Test
    fun `normalize remove acentos e minusculas`() {
        assertEquals("sao paulo", AutocompleteLogic.normalize("São Paulo"))
        assertEquals("brasilia", AutocompleteLogic.normalize("BRASÍLIA"))
        assertEquals("", AutocompleteLogic.normalize(""))
    }

    // --- filterSuggestions ---

    @Test
    fun `consulta vazia nao retorna sugestoes`() {
        assertEquals(emptyList<String>(), AutocompleteLogic.filterSuggestions("", options))
        assertEquals(emptyList<String>(), AutocompleteLogic.filterSuggestions("   ", options))
    }

    @Test
    fun `filtro e case insensitive`() {
        assertEquals(
            listOf("São Paulo", "São José do Rio Preto"),
            AutocompleteLogic.filterSuggestions("sao", options)
        )
    }

    @Test
    fun `filtro ignora acentos`() {
        val result = AutocompleteLogic.filterSuggestions("são", options)
        assertEquals(listOf("São Paulo", "São José do Rio Preto"), result)
    }

    @Test
    fun `opcoes que comecam com a consulta vem antes das que contem`() {
        val result = AutocompleteLogic.filterSuggestions("sa", options)
        assertEquals(
            listOf("São Paulo", "São José do Rio Preto", "Santos", "Santa Catarina", "Salvador"),
            result
        )
    }

    @Test
    fun `filtro retorna apenas opcoes que contem a consulta`() {
        val result = AutocompleteLogic.filterSuggestions("horizonte", options)
        assertEquals(listOf("Belo Horizonte"), result)
    }

    @Test
    fun `maxSuggestions limita o resultado`() {
        val result = AutocompleteLogic.filterSuggestions("s", options, maxSuggestions = 2)
        assertEquals(2, result.size)
    }

    @Test
    fun `filtro nao duplica opcoes`() {
        val duplicated = options + "São Paulo"
        val result = AutocompleteLogic.filterSuggestions("sao", duplicated)
        assertEquals(1, result.count { it == "São Paulo" })
    }

    @Test
    fun `sem correspondencia retorna lista vazia`() {
        assertEquals(emptyList<String>(), AutocompleteLogic.filterSuggestions("xyz", options))
    }

    // --- currentSuggestion ---

    @Test
    fun `sugestao inline para prefixo`() {
        assertEquals("São Paulo", AutocompleteLogic.currentSuggestion("sao", options))
    }

    @Test
    fun `sugestao inline ignora acentos e caixa`() {
        assertEquals("São Paulo", AutocompleteLogic.currentSuggestion("SÃ", options))
    }

    @Test
    fun `consulta vazia nao gera sugestao`() {
        assertNull(AutocompleteLogic.currentSuggestion("", options))
        assertNull(AutocompleteLogic.currentSuggestion("  ", options))
    }

    @Test
    fun `texto igual a opcao nao gera sugestao`() {
        assertNull(AutocompleteLogic.currentSuggestion("São Paulo", options))
    }

    @Test
    fun `consulta maior que todas as opcoes nao gera sugestao`() {
        assertNull(AutocompleteLogic.currentSuggestion("Belo Horizonte", options))
    }

    @Test
    fun `sem correspondencia nao gera sugestao`() {
        assertNull(AutocompleteLogic.currentSuggestion("xyz", options))
    }

    @Test
    fun `sugestao inline prefere inicio exato`() {
        val localOptions = listOf(
            "Tremembé",
            "Monte Belo",
            "Abre Campo"
        )
        assertEquals("Tremembé", AutocompleteLogic.currentSuggestion("trem", localOptions))
    }
}
