package com.w_via.features.declaration

import com.w_via.features.declaration.state.DeclaracaoObitoFormState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class DeclaracaoObitoMapperTest {

    private val base = DeclaracaoObitoFormState()

    // --- Mapeamento básico ---

    @Test
    fun `toDeclaracaoObito mapeia campos basicos`() {
        val s = base.copy(
            nome = "João da Silva",
            cpf = "123.456.789-00",
            rg = "12.345.678-9",
            idade = "45",
            sexo = Sexo.MASCULINO,
            cor = "Branca",
            profissao = "Aposentado",
            natural = "São Paulo"
        )
        val d = s.toDeclaracaoObito()
        assertEquals("João da Silva", d.falecido.nome)
        assertEquals(45, d.falecido.idade)
        assertEquals(Sexo.MASCULINO, d.falecido.sexo)
        assertEquals("São Paulo", d.falecido.natural)
    }

    @Test
    fun `idade invalida vira zero`() {
        val s = base.copy(idade = "abc")
        assertEquals(0, s.toDeclaracaoObito().falecido.idade)
    }

    // --- Idade calculada pela data do óbito ---

    @Test
    fun `idade usa a data do obito como referencia, nao a data atual`() {
        // Nascido em 1990, óbito em 2026 -> 36 anos, independentemente de hoje.
        val s = base.copy(
            dataNascimento = "10/05/1990",
            dataObito = "09/05/2026",
            idade = "99"
        )
        // O valor digitado (idade) é sobrescrito pela data de óbito presente.
        assertEquals(35, s.toDeclaracaoObito().falecido.idade)
    }

    @Test
    fun `idade conta o aniversario ja completado no dia do obito`() {
        val s = base.copy(dataNascimento = "10/05/1990", dataObito = "10/05/2026")
        assertEquals(36, s.toDeclaracaoObito().falecido.idade)
    }

    @Test
    fun `declaracao lavrada em outro dia usa a idade do dia do obito`() {
        // Nascido 01/01/1980; óbito 03/08/2026. Mesmo que a declaração seja
        // lavrada depois, a idade impressa é a do dia do óbito (46 anos).
        val s = base.copy(dataNascimento = "01/01/1980", dataObito = "03/08/2026")
        assertEquals(46, s.toDeclaracaoObito().falecido.idade)
    }

    @Test
    fun `idade sem data de obito usa valor do estado`() {
        val s = base.copy(idade = "45", dataNascimento = "10/05/1990")
        assertEquals(45, s.toDeclaracaoObito().falecido.idade)
    }

    @Test
    fun `sexo null assume masculino no dominio`() {
        val s = base.copy(idade = "30")
        assertEquals(Sexo.MASCULINO, s.toDeclaracaoObito().falecido.sexo)
    }

    // --- Opcionais em branco viram null ---

    @Test
    fun `conjuge em branco vira null`() {
        assertNull(base.toDeclaracaoObito().falecido.nomeConjuge)
    }

    @Test
    fun `conjuge preenchido e mantido`() {
        val s = base.copy(nomeConjuge = "Maria da Silva")
        assertEquals("Maria da Silva", s.toDeclaracaoObito().falecido.nomeConjuge)
    }

    @Test
    fun `inss e beneficio em branco viram null`() {
        val d = base.toDeclaracaoObito().falecido
        assertNull(d.inss)
        assertNull(d.numeroBeneficio)
    }

    @Test
    fun `medico2 em branco vira null`() {
        assertNull(base.toDeclaracaoObito().obito.medico2)
    }

    // --- Filhos ---

    @Test
    fun `filhos em branco sao filtrados`() {
        val s = base.copy(filhos = listOf("Pedro", "", "Paula", " "))
        val d = s.toDeclaracaoObito()
        assertEquals(2, d.filhos.size)
        assertEquals(listOf("Pedro", "Paula"), d.filhos.map { it.nome })
    }

    // --- Observações ---

    @Test
    fun `observacoes em branco usa mensagem padrao`() {
        assertEquals(
            "Informações passadas pela declarante",
            base.toDeclaracaoObito().observacoes
        )
    }

    @Test
    fun `observacoes preenchida e preservada`() {
        val s = base.copy(observacoes = "Declarante abalada emocionalmente")
        assertEquals("Declarante abalada emocionalmente", s.toDeclaracaoObito().observacoes)
    }

    // --- Casamento em cartório (gate) ---

    @Test
    fun `casamentoCartorioInfo null quando casado mas sem cartorio confirmado`() {
        val s = base.copy(idade = "45", estadoCivil = EstadoCivil.CASADO, casamentoEmCartorio = false)
        assertNull(s.toDeclaracaoObito().falecido.casamentoCartorioInfo)
    }

    @Test
    fun `casamentoCartorioInfo preenchido apenas quando casado e em cartorio`() {
        val s = base.copy(
            idade = "45",
            estadoCivil = EstadoCivil.CASADO,
            casamentoEmCartorio = true,
            casamentoDia = "10/05/2000",
            casamentoLivro = "1",
            casamentoFolha = "2",
            casamentoNumero = "3"
        )
        val info = s.toDeclaracaoObito().falecido.casamentoCartorioInfo
        assertEquals("10/05/2000", info?.dia)
        assertEquals("1", info?.livro)
    }

    @Test
    fun `casamentoCartorioInfo null quando solteiro`() {
        val s = base.copy(idade = "45", estadoCivil = EstadoCivil.SOLTEIRO, casamentoEmCartorio = true)
        assertNull(s.toDeclaracaoObito().falecido.casamentoCartorioInfo)
    }

    // --- Eleitor (gate de idade) ---

    @Test
    fun `eleitorInfo apenas quando eleitor true`() {
        val s = base.copy(idade = "30", eleitor = true, eleitorZona = "001")
        assertEquals("001", s.toDeclaracaoObito().falecido.eleitorInfo?.zona)
    }

    @Test
    fun `eleitorInfo null quando nao eleitor`() {
        val s = base.copy(idade = "30", eleitor = false)
        assertNull(s.toDeclaracaoObito().falecido.eleitorInfo)
    }

    // --- Reservista (gate sexo + idade) ---

    @Test
    fun `reservistaInfo null para sexo feminino`() {
        val s = base.copy(idade = "30", sexo = Sexo.FEMININO, reservista = true)
        assertNull(s.toDeclaracaoObito().falecido.reservistaInfo)
    }

    @Test
    fun `reservistaInfo presente para masculino na faixa`() {
        val s = base.copy(idade = "30", sexo = Sexo.MASCULINO, reservista = true, reservistaCarteira = "C123")
        assertEquals("C123", s.toDeclaracaoObito().falecido.reservistaInfo?.carteira)
    }

    // --- Regras condicionais (gates) do formulário ---

    @Test
    fun `perguntarEstadoCivilCasamento false abaixo do limiar`() {
        assertFalse(base.copy(idade = "15").perguntarEstadoCivilCasamento())
    }

    @Test
    fun `perguntarEstadoCivilCasamento true no limiar de 16`() {
        assertTrue(base.copy(idade = "16").perguntarEstadoCivilCasamento())
    }

    @Test
    fun `perguntarEleitor true no limiar de 16`() {
        assertTrue(base.copy(idade = "16").perguntarEleitor())
    }

    @Test
    fun `perguntarReservista true apenas no intervalo masculine`() {
        assertTrue(base.copy(idade = "18", sexo = Sexo.MASCULINO).perguntarReservista())
        assertTrue(base.copy(idade = "45", sexo = Sexo.MASCULINO).perguntarReservista())
        assertFalse(base.copy(idade = "46", sexo = Sexo.MASCULINO).perguntarReservista())
        assertFalse(base.copy(idade = "18", sexo = Sexo.FEMININO).perguntarReservista())
    }

    @Test
    fun `perguntarFilhos true no limiar de 10`() {
        assertTrue(base.copy(idade = "10").perguntarFilhos())
        assertFalse(base.copy(idade = "9").perguntarFilhos())
    }

    @Test
    fun `exibirCamposCasamentoCartorio exige casado e cartorio`() {
        assertFalse(base.copy(idade = "45", estadoCivil = EstadoCivil.SOLTEIRO, casamentoEmCartorio = true).exibirCamposCasamentoCartorio())
        assertTrue(base.copy(idade = "45", estadoCivil = EstadoCivil.CASADO, casamentoEmCartorio = true).exibirCamposCasamentoCartorio())
    }

    @Test
    fun `exibirTestamento so quando deixa bens`() {
        assertTrue(base.copy(deixaBens = true).exibirTestamento())
        assertFalse(base.copy(deixaBens = false).exibirTestamento())
    }

    // --- Regras de exibição/exclusão (estado civil, cônjuge, filhos, reservista) ---

    @Test
    fun `mostrarEstadoCivil normal para idade maior que 15`() {
        assertTrue(base.copy(idade = "16").mostrarEstadoCivil())
        assertFalse(base.copy(idade = "15").mostrarEstadoCivil())
    }

    @Test
    fun `mostrarEstadoCivil habilitado por excecao`() {
        assertTrue(base.copy(idade = "10", excecaoEstadoCivil = true).mostrarEstadoCivil())
    }

    @Test
    fun `estadoCivilAutomaticoSolteiro para menor de 16`() {
        assertTrue(base.copy(idade = "15").estadoCivilAutomaticoSolteiro())
        assertFalse(base.copy(idade = "16").estadoCivilAutomaticoSolteiro())
    }

    @Test
    fun `estadoCivilAutomaticoSolteiro desativado por excecao`() {
        assertFalse(base.copy(idade = "15", excecaoEstadoCivil = true).estadoCivilAutomaticoSolteiro())
    }

    @Test
    fun `mostrarConjuge conforme o estado civil`() {
        assertTrue(base.copy(estadoCivil = EstadoCivil.CASADO).mostrarConjuge())
        assertTrue(base.copy(estadoCivil = EstadoCivil.UNIAO_ESTAVEL).mostrarConjuge())
        assertTrue(base.copy(estadoCivil = EstadoCivil.DIVORCIADO).mostrarConjuge())
        assertTrue(base.copy(estadoCivil = EstadoCivil.VIUVO).mostrarConjuge())
        assertFalse(base.copy(estadoCivil = EstadoCivil.SOLTEIRO).mostrarConjuge())
        assertFalse(base.copy().mostrarConjuge())
    }

    @Test
    fun `conjugeObrigatorio apenas para casado e uniao estavel`() {
        assertTrue(base.copy(estadoCivil = EstadoCivil.CASADO).conjugeObrigatorio())
        assertTrue(base.copy(estadoCivil = EstadoCivil.UNIAO_ESTAVEL).conjugeObrigatorio())
        assertFalse(base.copy(estadoCivil = EstadoCivil.DIVORCIADO).conjugeObrigatorio())
        assertFalse(base.copy(estadoCivil = EstadoCivil.VIUVO).conjugeObrigatorio())
    }

    @Test
    fun `mostrarFilhos por idade ou excecao`() {
        assertTrue(base.copy(idade = "10").mostrarFilhos())
        assertFalse(base.copy(idade = "9").mostrarFilhos())
        assertTrue(base.copy(idade = "9", excecaoFilhos = true).mostrarFilhos())
    }

    @Test
    fun `mostrarReservista por faixa ou excecao`() {
        assertTrue(base.copy(idade = "18", sexo = Sexo.MASCULINO).mostrarReservista())
        assertFalse(base.copy(idade = "18", sexo = Sexo.FEMININO).mostrarReservista())
        assertTrue(base.copy(idade = "18", sexo = Sexo.FEMININO, excecaoReservista = true).mostrarReservista())
    }
}