package com.w_via.features.utils

import com.w_via.features.components.AutocompleteLogic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

class MunicipioDataTest {

    private val municipios: Map<String, List<String>> = MunicipioData.fromJson(
        """
        {
          "AC": ["Rio Branco", "Cruzeiro do Sul"],
          "SP": ["São Paulo", "Guarulhos", "Campinas", "Santos", "São José do Rio Preto"],
          "MG": ["Belo Horizonte", "Uberlândia"]
        }
        """.trimIndent()
    )

    // --- fromJson ---

    @Test
    fun `fromJson converte UF em lista de municipios`() {
        assertEquals(
            listOf("São Paulo", "Guarulhos", "Campinas", "Santos", "São José do Rio Preto"),
            municipios["SP"]
        )
    }

    // --- ufs ---

    @Test
    fun `ufs retorna siglas em ordem alfabetica`() {
        assertEquals(listOf("AC", "MG", "SP"), MunicipioData.ufs(municipios))
    }

    // --- citiesForUf ---

    @Test
    fun `citiesForUf retorna municipios da UF selecionada`() {
        assertEquals(
            listOf("São Paulo", "Guarulhos", "Campinas", "Santos", "São José do Rio Preto"),
            MunicipioData.citiesForUf(municipios, "SP")
        )
    }

    @Test
    fun `citiesForUf aceita sigla em minusculas`() {
        assertEquals(municipios["SP"], MunicipioData.citiesForUf(municipios, "sp"))
    }

    @Test
    fun `citiesForUf retorna lista vazia para UF desconhecida ou nula`() {
        assertEquals(emptyList<String>(), MunicipioData.citiesForUf(municipios, "ZZ"))
        assertEquals(emptyList<String>(), MunicipioData.citiesForUf(municipios, null))
        assertEquals(emptyList<String>(), MunicipioData.citiesForUf(municipios, ""))
    }

    @Test
    fun `trocar a UF muda a lista de cidades`() {
        val sp = MunicipioData.citiesForUf(municipios, "SP")
        val mg = MunicipioData.citiesForUf(municipios, "MG")

        assertTrue("São Paulo" in sp)
        assertTrue("São Paulo" !in mg)
        assertTrue("Belo Horizonte" in mg)
    }

    @Test
    fun `filtro de cidade considera apenas os municipios da UF selecionada`() {
        // Quando "São" é pesquisado, as cidades de outras UFs não aparecem.
        val sg = AutocompleteLogic.filterSuggestions(
            "sao",
            MunicipioData.citiesForUf(municipios, "SP")
        )
        assertTrue("São Paulo" in sg)
        assertTrue(sg.all { it in municipios.getValue("SP") })

        // Outra UF com o mesmo prefixo não contamina a lista.
        val mg = AutocompleteLogic.filterSuggestions(
            "Sao",
            MunicipioData.citiesForUf(municipios, "MG")
        )
        assertTrue(mg.isEmpty())
    }

    @Test
    fun `sem UF nao ha municipios para filtrar`() {
        assertTrue(MunicipioData.citiesForUf(municipios, null).isEmpty())
    }

    // --- arquivo real ---

    @Test
    fun `arquivo real de municipios contem as 27 UFs`() {
        val file = File("src/main/assets/municipios_brasil.json")
        assumeTrue("arquivo de assets não encontrado", file.exists())

        val data = MunicipioData.fromJson(file.readText())

        assertEquals(27, data.size)
        assertEquals(27, MunicipioData.ufs(data).size)
        assertTrue("São Paulo" in MunicipioData.citiesForUf(data, "SP"))
    }
}
