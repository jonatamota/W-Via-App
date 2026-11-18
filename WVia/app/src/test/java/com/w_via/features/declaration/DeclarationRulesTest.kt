package com.w_via.features.declaration

import com.w_via.features.declaration.state.DeclaracaoObitoFormState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeclarationRulesTest {

    // --- Estado civil automático (menor de 16) ---

    @Test
    fun `menor de 16 e marcado como solteiro automaticamente`() {
        val s = DeclaracaoObitoFormState().copy(idade = "10", estadoCivil = EstadoCivil.CASADO)
        val result = DeclarationRules.normalize(s)
        assertEquals(EstadoCivil.SOLTEIRO, result.state.estadoCivil)
        assertEquals(listOf("Estado civil"), result.clearedFields)
    }

    @Test
    fun `menor de 16 ja solteiro nao gera conflito`() {
        val s = DeclaracaoObitoFormState().copy(idade = "10", estadoCivil = EstadoCivil.SOLTEIRO)
        val result = DeclarationRules.normalize(s)
        assertEquals(EstadoCivil.SOLTEIRO, result.state.estadoCivil)
        assertTrue(result.clearedFields.isEmpty())
    }

    @Test
    fun `excecao de estado civil impede marcacao automatica`() {
        val s = DeclaracaoObitoFormState().copy(idade = "10", estadoCivil = EstadoCivil.CASADO, excecaoEstadoCivil = true)
        val result = DeclarationRules.normalize(s)
        assertEquals(EstadoCivil.CASADO, result.state.estadoCivil)
        assertTrue(result.clearedFields.isEmpty())
    }

    // --- Cônjuge ---

    @Test
    fun `solteiro limpa o nome do conjuge`() {
        val s = DeclaracaoObitoFormState().copy(estadoCivil = EstadoCivil.SOLTEIRO, nomeConjuge = "Maria")
        val result = DeclarationRules.normalize(s)
        assertTrue(result.state.nomeConjuge.isBlank())
        assertEquals(listOf("Nome do cônjuge"), result.clearedFields)
    }

    @Test
    fun `casado mantem o nome do conjuge`() {
        val s = DeclaracaoObitoFormState().copy(estadoCivil = EstadoCivil.CASADO, nomeConjuge = "Maria")
        val result = DeclarationRules.normalize(s)
        assertEquals("Maria", result.state.nomeConjuge)
        assertTrue(result.clearedFields.isEmpty())
    }

    // --- Casamento em cartório ---

    @Test
    fun `nao casado limpa dados de casamento em cartorio`() {
        val s = DeclaracaoObitoFormState().copy(
            estadoCivil = EstadoCivil.SOLTEIRO,
            casamentoEmCartorio = true,
            casamentoLivro = "3"
        )
        val result = DeclarationRules.normalize(s)
        assertNull(result.state.casamentoEmCartorio)
        assertTrue(result.state.casamentoLivro.isBlank())
        assertEquals(listOf("Casamento em cartório"), result.clearedFields)
    }

    // --- Filhos (menor de 10) ---

    @Test
    fun `menor de 10 limpa a lista de filhos`() {
        val s = DeclaracaoObitoFormState().copy(idade = "8", filhos = listOf("Pedro"))
        val result = DeclarationRules.normalize(s)
        assertTrue(result.state.filhos.isEmpty())
        assertEquals(listOf("Filhos"), result.clearedFields)
    }

    @Test
    fun `excecao de filhos impede a limpeza`() {
        val s = DeclaracaoObitoFormState().copy(idade = "8", filhos = listOf("Pedro"), excecaoFilhos = true)
        val result = DeclarationRules.normalize(s)
        assertEquals(listOf("Pedro"), result.state.filhos)
        assertTrue(result.clearedFields.isEmpty())
    }

    // --- Reservista ---

    @Test
    fun `sexo feminino limpa dados de reservista`() {
        val s = DeclaracaoObitoFormState().copy(
            idade = "30",
            sexo = Sexo.FEMININO,
            reservista = true,
            reservistaCarteira = "C123"
        )
        val result = DeclarationRules.normalize(s)
        assertNull(result.state.reservista)
        assertTrue(result.state.reservistaCarteira.isBlank())
        assertEquals(listOf("Dados de reservista"), result.clearedFields)
    }

    @Test
    fun `masculino na faixa mantem dados de reservista`() {
        val s = DeclaracaoObitoFormState().copy(
            idade = "30",
            sexo = Sexo.MASCULINO,
            reservista = true,
            reservistaCarteira = "C123"
        )
        val result = DeclarationRules.normalize(s)
        assertEquals(true, result.state.reservista)
        assertEquals("C123", result.state.reservistaCarteira)
        assertTrue(result.clearedFields.isEmpty())
    }

    @Test
    fun `excecao de reservista impede a limpeza`() {
        val s = DeclaracaoObitoFormState().copy(
            idade = "30",
            sexo = Sexo.FEMININO,
            reservista = true,
            excecaoReservista = true
        )
        val result = DeclarationRules.normalize(s)
        assertEquals(true, result.state.reservista)
        assertTrue(result.clearedFields.isEmpty())
    }

    // --- Confirmação de que campos aplicáveis não são tocados ---

    @Test
    fun `estado completo e valido nao altera nada`() {
        val s = DeclaracaoObitoFormState().copy(
            idade = "45",
            sexo = Sexo.MASCULINO,
            estadoCivil = EstadoCivil.CASADO,
            nomeConjuge = "Maria",
            casamentoEmCartorio = true,
            casamentoLivro = "3",
            reservista = true,
            reservistaCarteira = "C123",
            filhos = listOf("Pedro")
        )
        val result = DeclarationRules.normalize(s)
        assertEquals(s, result.state)
        assertTrue(result.clearedFields.isEmpty())
    }

    // --- Mensagem de conformidade ---

    @Test
    fun `conflictMessage cita os campos atualizados`() {
        val msg = DeclarationRules.conflictMessage(listOf("Nome do cônjuge", "Filhos"))
        assertTrue(msg.contains("Nome do cônjuge"))
        assertTrue(msg.contains("Filhos"))
    }
}
