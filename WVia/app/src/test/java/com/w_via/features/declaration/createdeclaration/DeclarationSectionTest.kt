package com.w_via.features.declaration.createdeclaration

import com.w_via.features.declaration.EstadoCivil
import com.w_via.features.declaration.Sexo
import com.w_via.features.declaration.TipoLocalObito
import com.w_via.features.declaration.state.DeclaracaoObitoFormState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeclarationSectionTest {

    private val state = DeclaracaoObitoFormState()

    private fun fullFalecido() = state.copy(
        nome = "João da Silva",
        cpf = "123.456.789-00",
        dataNascimento = "03/08/1980",
        sexo = Sexo.MASCULINO,
        rg = "12.345.678-9",
        cor = "Branca",
        profissao = "Aposentado",
        natural = "São Paulo",
        ufNatural = "SP",
        endereco = "Rua Exemplo, 100",
        cep = "01000-000",
        cidade = "São Paulo",
        uf = "SP",
        estadoCivil = EstadoCivil.SOLTEIRO
    )

    private fun fullObito() = state.copy(
        tipoLocalObito = TipoLocalObito.CASA,
        dataObito = "03/08/2026",
        nomeLocalObito = "Residência",
        horaFalecimento = "14:30",
        causasMorte = listOf("Parada cardiorrespiratória"),
        numeroAtestadoObito = "00123",
        ufMedico1 = "SP",
        crm1 = "12345",
        medico1 = "Dr. Roberto"
    )

    private fun fullDeclarante() = state.copy(
        emailDeclarante = "maria@exemplo.com",
        profissaoDeclarante = "Do lar",
        declaranteCartorio = "1º Cartório",
        ufCartorio = "SP",
        cidadeCartorio = "São Paulo",
        declaranteTelefone = "(11) 99999-9999",
        enderecoCartorio = "Rua do Cartório, 1",
        emailCartorio = "cartorio@exemplo.com",
        oficialCartorio = "Fulano de Tal"
    )

    // --- FALECIDO ---

    @Test
    fun `FALECIDO incompleta quando todos os campos vazios`() {
        assertFalse(DeclarationSection.FALECIDO.isComplete(state))
    }

    @Test
    fun `FALECIDO incompleta quando nome em branco`() {
        assertFalse(DeclarationSection.FALECIDO.isComplete(fullFalecido().copy(nome = "")))
    }

    @Test
    fun `FALECIDO incompleta quando data de nascimento invalida`() {
        assertFalse(DeclarationSection.FALECIDO.isComplete(fullFalecido().copy(dataNascimento = "abc")))
    }

    @Test
    fun `FALECIDO incompleta quando sexo nao informado`() {
        assertFalse(DeclarationSection.FALECIDO.isComplete(fullFalecido().copy(sexo = null)))
    }

    @Test
    fun `FALECIDO completa com todos os campos de identificacao, endereco e estado civil`() {
        assertTrue(DeclarationSection.FALECIDO.isComplete(fullFalecido()))
    }

    @Test
    fun `FALECIDO exige endereco, cep, cidade e uf`() {
        val semEndereco = fullFalecido().copy(endereco = "", cep = "", cidade = "", uf = "")
        assertFalse(DeclarationSection.FALECIDO.isComplete(semEndereco))
    }

    @Test
    fun `FALECIDO exige rg, cor, profissao, natural e uf natural`() {
        val semIdentificacao = fullFalecido().copy(rg = "", cor = "", profissao = "", natural = "", ufNatural = "")
        assertFalse(DeclarationSection.FALECIDO.isComplete(semIdentificacao))
    }

    @Test
    fun `FALECIDO nao exige campos de cartorio e inss`() {
        val s = fullFalecido().copy(
            cartorio = "",
            livro = "",
            folha = "",
            numero = "",
            inss = "",
            numeroBeneficio = ""
        )
        assertTrue(DeclarationSection.FALECIDO.isComplete(s))
    }

    @Test
    fun `FALECIDO nao exige idade preenchida manualmente`() {
        val s = fullFalecido()
        assertTrue(s.idade.isBlank())
        assertTrue(DeclarationSection.FALECIDO.isComplete(s))
    }

    @Test
    fun `FALECIDO incompleto quando casado sem nome do conjuge`() {
        val s = fullFalecido().copy(estadoCivil = EstadoCivil.CASADO)
        assertFalse(DeclarationSection.FALECIDO.isComplete(s))
    }

    @Test
    fun `FALECIDO completo quando casado com nome do conjuge`() {
        val s = fullFalecido().copy(estadoCivil = EstadoCivil.CASADO, nomeConjuge = "Maria")
        assertTrue(DeclarationSection.FALECIDO.isComplete(s))
    }

    // --- OBITO ---

    @Test
    fun `OBITO incompleta sem local e data`() {
        assertFalse(DeclarationSection.OBITO.isComplete(state))
    }

    @Test
    fun `OBITO incompleta sem data do obito`() {
        assertFalse(DeclarationSection.OBITO.isComplete(fullObito().copy(dataObito = "")))
    }

    @Test
    fun `OBITO incompleta sem nome do local, hora, causa ou dados do medico`() {
        val s = fullObito().copy(nomeLocalObito = "", horaFalecimento = "", causasMorte = listOf(""))
        assertFalse(DeclarationSection.OBITO.isComplete(s))
    }

    @Test
    fun `OBITO completa com local, data, nome, hora, causa e medico`() {
        assertTrue(DeclarationSection.OBITO.isComplete(fullObito()))
    }

    // --- FILIACAO ---

    @Test
    fun `FILIACAO incompleta sem nenhum genitor`() {
        assertFalse(DeclarationSection.FILIACAO.isComplete(state))
    }

    @Test
    fun `FILIACAO completa com apenas o pai`() {
        val s = state.copy(nomePai = "José da Silva")
        assertTrue(DeclarationSection.FILIACAO.isComplete(s))
    }

    @Test
    fun `FILIACAO completa com apenas a mae`() {
        val s = state.copy(nomeMae = "Ana da Silva")
        assertTrue(DeclarationSection.FILIACAO.isComplete(s))
    }

    // --- CONJUGE_FILHOS (opcional — apenas filhos) ---

    @Test
    fun `CONJUGE_FILHOS sempre completa por ser opcional`() {
        assertTrue(DeclarationSection.CONJUGE_FILHOS.isComplete(state))
        assertTrue(DeclarationSection.CONJUGE_FILHOS.isComplete(state.copy(estadoCivil = EstadoCivil.CASADO)))
        assertTrue(DeclarationSection.CONJUGE_FILHOS.isComplete(state.copy(estadoCivil = EstadoCivil.UNIAO_ESTAVEL)))
    }

    @Test
    fun `CONJUGE_FILHOS nunca obrigatoria`() {
        assertFalse(DeclarationSection.CONJUGE_FILHOS.isRequiredFor(state))
        assertFalse(DeclarationSection.CONJUGE_FILHOS.isRequiredFor(state.copy(estadoCivil = EstadoCivil.CASADO)))
        assertFalse(DeclarationSection.CONJUGE_FILHOS.isRequiredFor(state.copy(estadoCivil = EstadoCivil.UNIAO_ESTAVEL)))
        assertFalse(DeclarationSection.CONJUGE_FILHOS.isRequiredFor(state.copy(estadoCivil = EstadoCivil.DIVORCIADO)))
    }

    @Test
    fun `isRequiredFor mantem as secoes estaticas`() {
        assertTrue(DeclarationSection.FALECIDO.isRequiredFor(state))
        assertTrue(DeclarationSection.OBITO.isRequiredFor(state))
        assertTrue(DeclarationSection.FILIACAO.isRequiredFor(state))
        assertTrue(DeclarationSection.DECLARANTE.isRequiredFor(state))
        assertFalse(DeclarationSection.OBS.isRequiredFor(state))
    }

    // --- DECLARANTE ---

    @Test
    fun `DECLARANTE incompleta sem email e profissao`() {
        val s = fullDeclarante().copy(emailDeclarante = "", profissaoDeclarante = "")
        assertFalse(DeclarationSection.DECLARANTE.isComplete(s))
    }

    @Test
    fun `DECLARANTE incompleta sem os campos do cartorio`() {
        val s = fullDeclarante().copy(declaranteCartorio = "", ufCartorio = "", cidadeCartorio = "")
        assertFalse(DeclarationSection.DECLARANTE.isComplete(s))
    }

    @Test
    fun `DECLARANTE completa com email, profissao e cartorio`() {
        assertTrue(DeclarationSection.DECLARANTE.isComplete(fullDeclarante()))
    }

    @Test
    fun `DECLARANTE nao exige telefone, email e oficial do cartorio`() {
        val s = fullDeclarante().copy(
            declaranteTelefone = "",
            emailCartorio = "",
            oficialCartorio = ""
        )
        assertTrue(DeclarationSection.DECLARANTE.isComplete(s))
    }

    @Test
    fun `DECLARANTE nao exige nome, cpf, rg ou endereco residencial`() {
        val s = fullDeclarante().copy(
            nomeDeclarante = "",
            cpfDeclarante = "",
            rgDeclarante = "",
            residenciaDeclarante = "",
            celularDeclarante = ""
        )
        assertTrue(DeclarationSection.DECLARANTE.isComplete(s))
    }

    // --- OBS (opcional) ---

    @Test
    fun `OBS sempre completa por ser opcional`() {
        assertTrue(DeclarationSection.OBS.isComplete(state))
    }

    // --- Listas auxiliares ---

    @Test
    fun `REQUIRED contem apenas as secoes obrigatorias`() {
        val required = DeclarationSection.REQUIRED.map { it.name }
        assertTrue(required.containsAll(listOf("FALECIDO", "OBITO", "FILIACAO", "DECLARANTE")))
        assertFalse(required.contains("OBS"))
        assertFalse(required.contains("CONJUGE_FILHOS"))
    }
}
