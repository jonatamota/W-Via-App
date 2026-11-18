package com.w_via.features.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

class LocaisBrasilDataTest {

    private val locais: Map<String, Map<String, LocaisBrasilData.LocaisCidade>> =
        LocaisBrasilData.fromJson(
            """
            {
              "SP": {
                "São Paulo": {
                  "hospitais": ["Hospital das Clínicas", "Santa Casa de Misericórdia"],
                  "cemiterios": ["Cemitério da Consolação"]
                },
                "Campinas": {
                  "hospitais": ["Hospital das Clínicas de Campinas"],
                  "cemiterios": ["Cemitério da Saudade de Campinas", "Cemitério Monte d'Alva"]
                }
              },
              "MG": {
                "Belo Horizonte": {
                  "hospitais": ["Hospital das Clínicas da UFMG"],
                  "cemiterios": ["Cemitério do Bonfim"]
                }
              }
            }
            """.trimIndent()
        )

    // --- fromJson ---

    @Test
    fun `fromJson separa hospitais e cemiterios por cidade`() {
        val sp = locais.getValue("SP")
        assertEquals(
            listOf("Hospital das Clínicas", "Santa Casa de Misericórdia"),
            sp.getValue("São Paulo").hospitais
        )
        assertEquals(
            listOf("Cemitério da Consolação"),
            sp.getValue("São Paulo").cemiterios
        )
        assertEquals(
            listOf(
                "Cemitério da Saudade de Campinas",
                "Cemitério Monte d'Alva"
            ),
            sp.getValue("Campinas").cemiterios
        )
        assertTrue(locais["MG"].orEmpty()["Belo Horizonte"]?.hospitais.orEmpty()
            .contains("Hospital das Clínicas da UFMG"))
    }

    @Test
    fun `ufs retorna em ordem alfabetica`() {
        assertEquals(listOf("MG", "SP"), LocaisBrasilData.ufs(locais))
    }

    @Test
    fun `cidadesPara lista apenas cidades com locais`() {
        assertEquals(
            listOf("Campinas", "São Paulo"),
            LocaisBrasilData.cidadesPara(locais, "SP")
        )
    }

    // --- hospitaisPara / cemiteriosPara ---

    @Test
    fun `hospitaisPara filtra pela cidade`() {
        assertEquals(
            listOf("Hospital das Clínicas de Campinas"),
            LocaisBrasilData.hospitaisPara(locais, "SP", "Campinas")
        )
    }

    @Test
    fun `cemiteriosPara filtra pela cidade`() {
        assertEquals(
            listOf("Cemitério da Consolação"),
            LocaisBrasilData.cemiteriosPara(locais, "SP", "São Paulo")
        )
    }

    @Test
    fun `busca ignora caixa e acentuacao`() {
        assertEquals(
            listOf("Hospital das Clínicas de Campinas"),
            LocaisBrasilData.hospitaisPara(locais, "sp", "campinas")
        )
        // cidade digitada fora do padrão ainda deve casar (acento/caixa)
        assertEquals(
            locais.getValue("SP").getValue("São Paulo").hospitais,
            LocaisBrasilData.hospitaisPara(locais, "SP", "SAO PAULO")
        )
        assertEquals(
            locais.getValue("SP").getValue("São Paulo").cemiterios,
            LocaisBrasilData.cemiteriosPara(locais, "SP", "são paulo")
        )
    }

    @Test
    fun `cidade ausente retorna lista vazia`() {
        assertEquals(
            emptyList<String>(),
            LocaisBrasilData.hospitaisPara(locais, "SP", "Cidade Inexistente")
        )
        assertEquals(
            emptyList<String>(),
            LocaisBrasilData.cemiteriosPara(locais, "SP", "")
        )
        assertEquals(
            emptyList<String>(),
            LocaisBrasilData.hospitaisPara(locais, null, "São Paulo")
        )
        assertEquals(
            emptyList<String>(),
            LocaisBrasilData.hospitaisPara(locais, "SP", null)
        )
    }

    @Test
    fun `mesmo hospital de outra cidade nao vaza`() {
        assertEquals(
            emptyList<String>(),
            LocaisBrasilData.hospitaisPara(locais, "MG", "São Paulo")
        )
    }

    // --- arquivo real ---

    @Test
    fun `arquivo real de locais cobre as 27 UFs sem duplicar nomes`() {
        val file = File("src/main/assets/locais_brasil.json")
        assumeTrue("arquivo de assets não encontrado", file.exists())

        val data = LocaisBrasilData.fromJson(file.readText())

        assertEquals(27, data.size)
        assertTrue(data.getValue("SP").containsKey("São Paulo"))

        for (uf in data.values) {
            for (locaisCidade in uf.values) {
                assertEquals(
                    locaisCidade.hospitais.distinct(),
                    locaisCidade.hospitais
                )
                assertEquals(
                    locaisCidade.cemiterios.distinct(),
                    locaisCidade.cemiterios
                )
            }
        }
        assertTrue(
            LocaisBrasilData
                .hospitaisPara(data, "SP", "São Paulo")
                .isNotEmpty()
        )
    }
}