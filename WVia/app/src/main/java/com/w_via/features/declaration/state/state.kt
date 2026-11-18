package com.w_via.features.declaration.state

import com.w_via.features.declaration.DeclaracaoObitoThresholds
import com.w_via.features.declaration.EstadoCivil
import com.w_via.features.declaration.Sexo
import com.w_via.features.declaration.TipoLocalObito

enum class DeclaracaoObitoStep {
    IDENTIFICACAO,
    ESTADO_CIVIL,
    DOCUMENTOS,
    BENS,
    ENDERECO_CARTORIO_INSS,
    FILIACAO,
    OBITO,
    FILHOS,
    DECLARANTE;

    fun proximo(): DeclaracaoObitoStep? = entries.getOrNull(ordinal + 1)
    fun anterior(): DeclaracaoObitoStep? = entries.getOrNull(ordinal - 1)
}

/**
 * Estado editável do formulário. Campos de texto ficam como String (binding direto
 * com TextField); campos de seleção usam os enums/Boolean do domínio.
 * A conversão para os data classes finais (DadosFalecido, DadosObito, etc.)
 * acontece em DeclaracaoObitoViewModel.toDeclaracaoObito().
 */
data class DeclaracaoObitoFormState(
    val step: DeclaracaoObitoStep = DeclaracaoObitoStep.IDENTIFICACAO,

    // Identificação básica
    val nome: String = "",
    val cpf: String = "",
    val rg: String = "",
    val dataNascimento: String = "",
    val idade: String = "",
    val sexo: Sexo? = null,
    val cor: String = "",
    val profissao: String = "",
    val natural: String = "",
    val ufNatural: String = "",

    // Estado civil
    val estadoCivil: EstadoCivil? = null,
    val nomeConjuge: String = "",
    val casamentoEmCartorio: Boolean? = null,
    val casamentoDia: String = "",
    val casamentoLivro: String = "",
    val casamentoFolha: String = "",
    val casamentoNumero: String = "",

    // Documentos (eleitor / reservista)
    val eleitor: Boolean? = null,
    val eleitorCidade: String = "",
    val eleitorUf: String = "",
    val eleitorZona: String = "",
    val eleitorSecao: String = "",
    val eleitorNumeroTitulo: String = "",
    val reservista: Boolean? = null,
    val reservistaCarteira: String = "",
    val reservistaNumeroSerie: String = "",
    val reservistaCsm: String = "",
    val reservistaRm: String = "",

    // Bens
    val deixaBens: Boolean? = null,
    val testamento: Boolean? = null,

    // Endereço / Cartório / INSS
    val cartorio: String = "",
    val livro: String = "",
    val folha: String = "",
    val numero: String = "",
    val endereco: String = "",
    val cep: String = "",
    val cidade: String = "",
    val uf: String = "",
    val inss: String = "",
    val numeroBeneficio: String = "",

    // Filiação
    val nomePai: String = "",
    val naturalPai: String = "",
    val ufNaturalPai: String = "",
    val estadoCivilPai: String = "",
    val profissaoPai: String = "",
    val nomeMae: String = "",
    val naturalMae: String = "",
    val ufNaturalMae: String = "",
    val estadoCivilMae: String = "",
    val profissaoMae: String = "",

    // Óbito
    val tipoLocalObito: TipoLocalObito? = null,
    val nomeLocalObito: String = "",
    val cidadeObito: String = "",
    val ufObito: String = "",
    val dataObito: String = "",
    val horaFalecimento: String = "",
    val numeroAtestadoObito: String = "",
    val causasMorte: List<String> = emptyList(),
    val cemiterio: String = "",
    val horaSepultamento: String = "",
    val localVelorio: String = "",
    val horaVelorio: String = "",
    val enderecoVelorio: String = "",
    val numeroVelorio: String = "",
    val bairroVelorio: String = "",
    val cidadeVelorio: String = "",
    val cepVelorio: String = "",
    val ufVelorio: String = "",
    val medico1: String = "",
    val crm1: String = "",
    val ufMedico1: String = "",
    val medico2: String = "",
    val crm2: String = "",
    val ufMedico2: String = "",

    // Filhos
    val filhos: List<String> = emptyList(),

    // Indicadores de "caso excepcional": revelam campos ocultos pelas regras de
    // exibição (menor de idade, sexo fora da faixa etc.). São apenas de interface
    // e NÃO entram no modelo de domínio ([DeclaracaoObitoMapper]).
    val excecaoEstadoCivil: Boolean = false,
    val excecaoFilhos: Boolean = false,
    val excecaoReservista: Boolean = false,

    // Declarante
    val declaranteCartorio: String = "",
    val declaranteTelefone: String = "",
    val declaranteEndereco: String = "",
    val nomeDeclarante: String = "",
    val grauParentesco: String = "",
    val profissaoDeclarante: String = "",
    val rgDeclarante: String = "",
    val cpfDeclarante: String = "",
    val residenciaDeclarante: String = "",
    val bairroDeclarante: String = "",
    val numeroDeclarante: String = "",
    val municipioDeclarante: String = "",
    val cepDeclarante: String = "",
    val ufDeclarante: String = "",
    val celularDeclarante: String = "",
    val emailDeclarante: String = "",

    // Cartório (endereço do cartório, separado do endereço do declarante)
    val enderecoCartorio: String = "",
    val numeroCartorio: String = "",
    val ufCartorio: String = "",
    val cidadeCartorio: String = "",
    val emailCartorio: String = "",
    val oficialCartorio: String = "",

    // Observações (opcional)
    val observacoes: String = ""
) {
    private val idadeInt: Int get() = idade.toIntOrNull() ?: 0

    fun perguntarEstadoCivilCasamento(): Boolean =
        idadeInt >= DeclaracaoObitoThresholds.MIN_IDADE_CASAMENTO

    /** Estado civil visível quando a idade permite, ou quando há exceção informada. */
    fun mostrarEstadoCivil(): Boolean =
        perguntarEstadoCivilCasamento() || excecaoEstadoCivil

    /** Menor de 16 anos é marcado como SOLTEIRO automaticamente (salvo exceção). */
    fun estadoCivilAutomaticoSolteiro(): Boolean =
        idadeInt < DeclaracaoObitoThresholds.MIN_IDADE_CASAMENTO && !excecaoEstadoCivil

    fun perguntarCasamentoEmCartorio(): Boolean =
        mostrarEstadoCivil() && estadoCivil == EstadoCivil.CASADO

    fun exibirCamposCasamentoCartorio(): Boolean =
        perguntarCasamentoEmCartorio() && casamentoEmCartorio == true

    /** Cônjuge visível para casado/união estável (obrigatório) e divorciado/viúvo (opcional). */
    fun mostrarConjuge(): Boolean =
        estadoCivil == EstadoCivil.CASADO ||
            estadoCivil == EstadoCivil.UNIAO_ESTAVEL ||
            estadoCivil == EstadoCivil.DIVORCIADO ||
            estadoCivil == EstadoCivil.VIUVO

    /** Cônjuge obrigatório para casado e união estável. */
    fun conjugeObrigatorio(): Boolean =
        estadoCivil == EstadoCivil.CASADO || estadoCivil == EstadoCivil.UNIAO_ESTAVEL

    fun perguntarEleitor(): Boolean =
        idadeInt >= DeclaracaoObitoThresholds.MIN_IDADE_ELEITOR

    fun exibirCamposEleitor(): Boolean = perguntarEleitor() && eleitor == true

    fun perguntarReservista(): Boolean =
        sexo == Sexo.MASCULINO &&
                idadeInt in DeclaracaoObitoThresholds.MIN_IDADE_RESERVISTA..DeclaracaoObitoThresholds.MAX_IDADE_RESERVISTA

    /** Reservista visível na faixa aplicável, ou quando há exceção informada. */
    fun mostrarReservista(): Boolean = perguntarReservista() || excecaoReservista

    fun exibirCamposReservista(): Boolean = mostrarReservista() && reservista == true

    fun exibirTestamento(): Boolean = deixaBens == true

    fun perguntarFilhos(): Boolean =
        idadeInt >= DeclaracaoObitoThresholds.MIN_IDADE_FILHOS_PROVAVEL

    /** Campo de filhos visível pela idade, ou quando há exceção informada. */
    fun mostrarFilhos(): Boolean = perguntarFilhos() || excecaoFilhos
}