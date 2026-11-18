package com.w_via.features.declaration.createdeclaration

import com.w_via.features.declaration.Sexo
import com.w_via.features.declaration.state.DeclaracaoObitoFormState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cobre as regras de liberação das abas de "Dados do Falecido": a aba
 * Identificação é sempre visível; Estado civil exige data de nascimento;
 * Documentos exige sexo; e Endereço + Cartório/INSS exige identificação completa.
 */
class FalecidoTabsTest {

    private val base = DeclaracaoObitoFormState()

    private fun fullIdentificacao() = base.copy(
        nome = "João da Silva",
        cpf = "123.456.789-00",
        dataNascimento = "03/08/1980",
        sexo = Sexo.MASCULINO,
        rg = "12.345.678-9",
        cor = "Branca",
        profissao = "Aposentado",
        natural = "São Paulo",
        ufNatural = "SP"
    )

    @Test
    fun `apenas a aba de identificacao esta visivel no inicio`() {
        assertEquals(listOf(FalecidoTabs.IDENTIFICACAO), FalecidoTabs.availableTabs(base))
    }

    @Test
    fun `data de nascimento libera a aba de estado civil`() {
        val s = base.copy(dataNascimento = "03/08/1980")
        assertEquals(
            listOf(FalecidoTabs.IDENTIFICACAO, FalecidoTabs.ESTADO_CIVIL),
            FalecidoTabs.availableTabs(s)
        )
    }

    @Test
    fun `sexo libera a aba de documentos`() {
        val s = base.copy(sexo = Sexo.MASCULINO)
        assertEquals(
            listOf(FalecidoTabs.IDENTIFICACAO, FalecidoTabs.DOCUMENTOS),
            FalecidoTabs.availableTabs(s)
        )
    }

    @Test
    fun `identificacao completa libera endereco e cartorio`() {
        val s = fullIdentificacao()
        val tabs = FalecidoTabs.availableTabs(s)
        assertTrue(FalecidoTabs.ENDERECO in tabs)
        assertTrue(FalecidoTabs.CARTORIO_INSS in tabs)
        assertEquals(5, tabs.size)
    }

    @Test
    fun `identificacao incompleta nao libera endereco e cartorio`() {
        val s = base.copy(
            nome = "João da Silva",
            cpf = "123.456.789-00",
            dataNascimento = "03/08/1980",
            sexo = Sexo.MASCULINO
        )
        val tabs = FalecidoTabs.availableTabs(s)
        assertTrue(FalecidoTabs.ENDERECO !in tabs)
        assertTrue(FalecidoTabs.CARTORIO_INSS !in tabs)
    }

    @Test
    fun `abas seguem a ordem fixa`() {
        val s = fullIdentificacao()
        assertEquals(
            listOf(
                FalecidoTabs.IDENTIFICACAO,
                FalecidoTabs.ESTADO_CIVIL,
                FalecidoTabs.DOCUMENTOS,
                FalecidoTabs.ENDERECO,
                FalecidoTabs.CARTORIO_INSS
            ),
            FalecidoTabs.availableTabs(s)
        )
    }
}
