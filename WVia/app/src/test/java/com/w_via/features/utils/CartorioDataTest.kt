package com.w_via.features.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

class CartorioDataTest {

    private val cartorios: Map<String, Map<String, List<CartorioInfo>>> =
        CartorioData.fromJson(
            """
            {
              "SP": {
                "São Paulo": [
                  {
                    "cartorio": "São Paulo -  1º Subdistrito - Sé",
                    "telefone": "(11) 1111-1111",
                    "endereco": "Praça da Sé, 100",
                    "email": "se@cartorio.com.br",
                    "oficial": "JOAO DA SILVA",
                    "num_cnj": "100001"
                  },
                  {
                    "cartorio": "São Paulo -  2º Subdistrito - Liberdade",
                    "telefone": "(11) 2222-2222",
                    "endereco": "Rua da Liberdade, 200",
                    "email": "liberdade@cartorio.com.br",
                    "oficial": "MARIA DE SOUZA",
                    "num_cnj": "100002"
                  }
                ],
                "Campinas": [
                  {
                    "cartorio": "1º Subdistrito - Campinas",
                    "telefone": "(19) 3333-3333",
                    "endereco": "Rua Barão de Jaguara, 300",
                    "email": "campinas@cartorio.com.br",
                    "oficial": "PEDRO OLIVEIRA",
                    "num_cnj": "100003"
                  }
                ]
              },
              "MG": {
                "Belo Horizonte": [
                  {
                    "cartorio": "1º Ofício de Registro Civil de Belo Horizonte",
                    "telefone": "(31) 4444-4444",
                    "endereco": "Av. Afonso Pena, 400",
                    "email": "bh@cartorio.com.br",
                    "oficial": "ANA PEREIRA",
                    "num_cnj": "100004"
                  }
                ]
              }
            }
            """.trimIndent()
        )

    // --- fromJson ---

    @Test
    fun `fromJson mantem cartorios por cidade com dados completos`() {
        val sp = cartorios.getValue("SP")
        assertEquals(
            listOf(
                CartorioInfo(
                    nome = "São Paulo -  1º Subdistrito - Sé",
                    telefone = "(11) 1111-1111",
                    endereco = "Praça da Sé, 100",
                    email = "se@cartorio.com.br",
                    oficial = "JOAO DA SILVA",
                    numCnj = "100001"
                ),
                CartorioInfo(
                    nome = "São Paulo -  2º Subdistrito - Liberdade",
                    telefone = "(11) 2222-2222",
                    endereco = "Rua da Liberdade, 200",
                    email = "liberdade@cartorio.com.br",
                    oficial = "MARIA DE SOUZA",
                    numCnj = "100002"
                )
            ),
            sp.getValue("São Paulo")
        )
        val bh = cartorios.getValue("MG").getValue("Belo Horizonte")
        assertEquals("(31) 4444-4444", bh[0].telefone)
        assertEquals("ANA PEREIRA", bh[0].oficial)
    }

    // --- ufs / cidadesPara ---

    @Test
    fun `ufs retorna em ordem alfabetica`() {
        assertEquals(listOf("MG", "SP"), CartorioData.ufs(cartorios))
    }

    @Test
    fun `cidadesPara lista apenas cidades com cartorio`() {
        assertEquals(
            listOf("Campinas", "São Paulo"),
            CartorioData.cidadesPara(cartorios, "SP")
        )
    }

    // --- cartoriosPara / nomesPara ---

    @Test
    fun `cartoriosPara filtra pela cidade`() {
        assertEquals(
            listOf("1º Subdistrito - Campinas"),
            CartorioData.cartoriosPara(cartorios, "SP", "Campinas").map { it.nome }
        )
    }

    @Test
    fun `nomesPara retorna apenas nomes`() {
        assertEquals(
            listOf("São Paulo -  1º Subdistrito - Sé", "São Paulo -  2º Subdistrito - Liberdade"),
            CartorioData.nomesPara(cartorios, "SP", "São Paulo")
        )
    }

    @Test
    fun `busca ignora caixa e acentuacao`() {
        assertEquals(
            cartorios.getValue("SP").getValue("São Paulo"),
            CartorioData.cartoriosPara(cartorios, "sp", "SAO PAULO")
        )
        assertEquals(
            cartorios.getValue("SP").getValue("Campinas"),
            CartorioData.cartoriosPara(cartorios, "SP", "campinas")
        )
    }

    @Test
    fun `cidade ausente retorna lista vazia`() {
        assertEquals(
            emptyList<CartorioInfo>(),
            CartorioData.cartoriosPara(cartorios, "SP", "Cidade Inexistente")
        )
        assertEquals(
            emptyList<CartorioInfo>(),
            CartorioData.cartoriosPara(cartorios, "SP", "")
        )
        assertEquals(
            emptyList<CartorioInfo>(),
            CartorioData.cartoriosPara(cartorios, null, "São Paulo")
        )
        assertEquals(
            emptyList<CartorioInfo>(),
            CartorioData.cartoriosPara(cartorios, "SP", null)
        )
    }

    @Test
    fun `cartorio de outra cidade nao vaza`() {
        assertEquals(
            emptyList<CartorioInfo>(),
            CartorioData.cartoriosPara(cartorios, "MG", "São Paulo")
        )
    }

    // --- arquivo real ---

    @Test
    fun `arquivo real de cartorios cobre as 27 UFs sem duplicar nomes`() {
        val file = File("src/main/assets/cartorios.json")
        assumeTrue("arquivo de assets não encontrado", file.exists())

        val data = CartorioData.fromJson(file.readText())

        assertEquals(27, data.size)
        assertTrue(data.getValue("SP").containsKey("São Paulo"))

        var comDados = 0
        for (uf in data.values) {
            for (infos in uf.values) {
                assertEquals(infos.map { it.nome }.distinct().size, infos.size)
                assertTrue(infos.isNotEmpty())
                if (infos.any { it.telefone.isNotBlank() || it.email.isNotBlank() }) comDados++
            }
        }
        assertTrue(
            CartorioData
                .cartoriosPara(data, "SP", "São Paulo")
                .isNotEmpty()
        )
        assertTrue("nenhum cartório com dados de contato", comDados > 0)
    }
}
