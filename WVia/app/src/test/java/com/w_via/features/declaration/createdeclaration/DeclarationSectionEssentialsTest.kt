package com.w_via.features.declaration.createdeclaration

import com.w_via.features.declaration.EstadoCivil
import com.w_via.features.declaration.Sexo
import com.w_via.features.declaration.TipoLocalObito
import com.w_via.features.declaration.state.DeclaracaoObitoFormState
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Testes para os métodos essenciaisMissing e recommendedMissing de [DeclarationSection].
 */
class DeclarationSectionEssentialsTest {

    private lateinit var state: DeclaracaoObitoFormState

    @Before
    fun setUp() {
        state = DeclaracaoObitoFormState()
    }

    private fun fullFalecido() = state.copy(
        nome = "João Silva",
        cpf = "123.456.789-00",
        dataNascimento = "01/01/1980",
        sexo = Sexo.MASCULINO,
        rg = "12.345.678-9",
        cor = "Branca",
        profissao = "Aposentado",
        natural = "São Paulo",
        ufNatural = "SP",
        endereco = "Rua Exemplo, 100",
        cep = "01000-000",
        cidade = "São Paulo",
        uf = "SP"
    )

    private fun fullObito() = state.copy(
        tipoLocalObito = TipoLocalObito.HOSPITAL,
        dataObito = "15/01/2023",
        nomeLocalObito = "Hospital Municipal",
        horaFalecimento = "14:30",
        causasMorte = listOf("Parada cardiorrespiratória"),
        numeroAtestadoObito = "00123",
        ufMedico1 = "SP",
        crm1 = "12345",
        medico1 = "Dr. Roberto"
    )

    private fun fullDeclarante() = state.copy(
        emailDeclarante = "email@exemplo.com",
        profissaoDeclarante = "Do lar",
        declaranteCartorio = "1º Cartório",
        ufCartorio = "SP",
        cidadeCartorio = "São Paulo",
        declaranteTelefone = "(11) 99999-9999",
        enderecoCartorio = "Rua do Cartório, 1",
        emailCartorio = "cartorio@exemplo.com",
        oficialCartorio = "Fulano de Tal"
    )

    @Test
    fun `FALECIDO essentialsMissing retorna todos os campos obrigatorios faltantes`() {
        val missing = DeclarationSection.FALECIDO.essentialsMissing(state)
        assertEquals(
            listOf(
                "Nome", "CPF", "Data de nascimento", "Sexo", "RG", "Cor/Raça",
                "Profissão", "Natural de", "UF natural", "Endereço", "CEP", "Cidade", "UF"
            ),
            missing
        )
    }

    @Test
    fun `FALECIDO essentialsMissing retorna lista vazia quando todos preenchidos`() {
        assertEquals(emptyList<String>(), DeclarationSection.FALECIDO.essentialsMissing(fullFalecido()))
    }

    @Test
    fun `FALECIDO essentialsMissing inclui conjuge quando casado e em branco`() {
        val estadoCivilState = fullFalecido().copy(estadoCivil = EstadoCivil.CASADO)
        assertEquals(listOf("Nome do cônjuge"), DeclarationSection.FALECIDO.essentialsMissing(estadoCivilState))
    }

    @Test
    fun `OBITO essentialsMissing retorna todos os campos obrigatorios faltantes`() {
        val missing = DeclarationSection.OBITO.essentialsMissing(state)
        assertEquals(
            listOf(
                "Tipo do local do óbito", "Data do óbito", "Nome do local", "Hora do óbito",
                "Causa da morte", "N° do atestado", "UF do médico", "CRM do médico", "Nome do médico"
            ),
            missing
        )
    }

    @Test
    fun `OBITO essentialsMissing retorna lista vazia quando todos preenchidos`() {
        assertEquals(emptyList<String>(), DeclarationSection.OBITO.essentialsMissing(fullObito()))
    }

    @Test
    fun `FILIACAO essentialsMissing retorna campos obrigatorios faltantes`() {
        assertEquals(listOf("Nome do pai ou da mãe"), DeclarationSection.FILIACAO.essentialsMissing(state))
    }

    @Test
    fun `FILIACAO essentialsMissing retorna lista vazia quando um dos pais preenchido`() {
        assertEquals(emptyList<String>(), DeclarationSection.FILIACAO.essentialsMissing(state.copy(nomePai = "José Silva")))
    }

    @Test
    fun `CONJUGE_FILHOS essentialsMissing sempre retorna lista vazia`() {
        assertEquals(emptyList<String>(), DeclarationSection.CONJUGE_FILHOS.essentialsMissing(state.copy(estadoCivil = EstadoCivil.CASADO)))
        assertEquals(emptyList<String>(), DeclarationSection.CONJUGE_FILHOS.essentialsMissing(state))
    }

    @Test
    fun `DECLARANTE essentialsMissing retorna campos obrigatorios faltantes`() {
        val missing = DeclarationSection.DECLARANTE.essentialsMissing(state)
        assertEquals(
            listOf(
                "E-mail do declarante", "Profissão do declarante", "Cartório", "UF do cartório",
                "Cidade do cartório", "Endereço do cartório"
            ),
            missing
        )
    }

    @Test
    fun `DECLARANTE essentialsMissing nao exige telefone, email ou oficial do cartorio`() {
        val s = fullDeclarante().copy(
            declaranteTelefone = "",
            emailCartorio = "",
            oficialCartorio = ""
        )
        assertEquals(emptyList<String>(), DeclarationSection.DECLARANTE.essentialsMissing(s))
    }

    @Test
    fun `DECLARANTE essentialsMissing retorna lista vazia quando todos preenchidos`() {
        assertEquals(emptyList<String>(), DeclarationSection.DECLARANTE.essentialsMissing(fullDeclarante()))
    }

    @Test
    fun `OBS essentialsMissing sempre retorna lista vazia`() {
        assertEquals(emptyList<String>(), DeclarationSection.OBS.essentialsMissing(state))
    }

    @Test
    fun `FALECIDO recommendedMissing sempre retorna lista vazia`() {
        assertEquals(emptyList<String>(), DeclarationSection.FALECIDO.recommendedMissing(state))
        assertEquals(emptyList<String>(), DeclarationSection.FALECIDO.recommendedMissing(fullFalecido()))
    }

    @Test
    fun `OBITO recommendedMissing sempre retorna lista vazia`() {
        assertEquals(emptyList<String>(), DeclarationSection.OBITO.recommendedMissing(state))
        assertEquals(emptyList<String>(), DeclarationSection.OBITO.recommendedMissing(fullObito()))
    }

    @Test
    fun `FILIACAO recommendedMissing retorna campos recomendados faltantes`() {
        assertEquals(
            listOf("Naturalidade do pai", "UF natural do pai", "Naturalidade da mãe", "UF natural da mãe"),
            DeclarationSection.FILIACAO.recommendedMissing(state)
        )
    }

    @Test
    fun `FILIACAO recommendedMissing retorna lista vazia quando todos preenchidos`() {
        val updatedState = state.copy(
            naturalPai = "Rio de Janeiro",
            ufNaturalPai = "RJ",
            naturalMae = "São Paulo",
            ufNaturalMae = "SP"
        )
        assertEquals(emptyList<String>(), DeclarationSection.FILIACAO.recommendedMissing(updatedState))
    }

    @Test
    fun `CONJUGE_FILHOS recommendedMissing sempre retorna lista vazia`() {
        assertEquals(emptyList<String>(), DeclarationSection.CONJUGE_FILHOS.recommendedMissing(state.copy(estadoCivil = EstadoCivil.CASADO)))
        assertEquals(emptyList<String>(), DeclarationSection.CONJUGE_FILHOS.recommendedMissing(state))
    }

    @Test
    fun `DECLARANTE recommendedMissing sempre retorna lista vazia`() {
        assertEquals(emptyList<String>(), DeclarationSection.DECLARANTE.recommendedMissing(state))
        assertEquals(emptyList<String>(), DeclarationSection.DECLARANTE.recommendedMissing(fullDeclarante()))
    }

    @Test
    fun `OBS recommendedMissing sempre retorna lista vazia`() {
        assertEquals(emptyList<String>(), DeclarationSection.OBS.recommendedMissing(state))
    }
}
