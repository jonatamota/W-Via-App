package com.w_via.features.declaration

import com.w_via.features.declaration.DeclaracaoObitoThresholds as T
import kotlinx.serialization.Serializable

/** Preenchido apenas se casamentoEmCartorio == true */
@Serializable
data class CasamentoCartorioInfo(
    val dia: String,
    val livro: String,
    val folha: String,
    val numero: String
)

/** Preenchido apenas se eleitor == true */
@Serializable
data class EleitorInfo(
    val cidade: String,
    val uf: String = "",
    val zona: String,
    val secao: String,
    val numeroTitulo: String
)

/** Preenchido apenas se reservista == true */
@Serializable
data class ReservistaInfo(
    val carteira: String,
    val numeroSerie: String,
    val csm: String,
    val rm: String
)

@Serializable
data class Filiacao(
    val nomePai: String?,
    val naturalPai: String?,
    val ufNaturalPai: String? = null,
    val estadoCivilPai: String?,
    val profissaoPai: String?,
    val nomeMae: String?,
    val naturalMae: String?,
    val ufNaturalMae: String? = null,
    val estadoCivilMae: String?,
    val profissaoMae: String?
)

@Serializable
data class DadosFalecido(
    // Identificação básica — sempre visível
    val nome: String,
    val cpf: String,
    val rg: String,
    val dataNascimento: String,
    val idade: Int,
    val sexo: Sexo,
    val cor: String,
    val profissao: String,
    val natural: String,
    val ufNatural: String = "",
    val cartorio: String,
    val livro: String,
    val folha: String,
    val numero: String,
    val endereco: String,
    val cep: String,
    val cidade: String,
    val uf: String = "",

    // Estado civil — gate de idade
    val estadoCivil: EstadoCivil?,
    val nomeConjuge: String? = null,

    // Casamento em cartório — sub-gate de estadoCivil == CASADO
    val casamentoEmCartorio: Boolean? = null,
    val casamentoCartorioInfo: CasamentoCartorioInfo? = null,

    // Bens — gate independente
    val deixaBens: Boolean? = null,
    val testamento: Boolean? = null,

    // INSS — sempre visível
    val inss: String? = null,
    val numeroBeneficio: String? = null,

    // Eleitor — gate de idade
    val eleitor: Boolean? = null,
    val eleitorInfo: EleitorInfo? = null,

    // Reservista — gate de sexo + idade
    val reservista: Boolean? = null,
    val reservistaInfo: ReservistaInfo? = null,

    val filiacao: Filiacao
) {

    /** true = pergunta é exibida; false = campo é ocultado/inferido como não aplicável */
    fun perguntarEstadoCivilCasamento(): Boolean = idade >= T.MIN_IDADE_CASAMENTO

    fun perguntarEleitor(): Boolean = idade >= T.MIN_IDADE_ELEITOR

    fun perguntarReservista(): Boolean =
        sexo == Sexo.MASCULINO && idade in T.MIN_IDADE_RESERVISTA..T.MAX_IDADE_RESERVISTA

    fun perguntarFilhos(): Boolean = idade >= T.MIN_IDADE_FILHOS_PROVAVEL

    fun alertaFiliacaoPodeSerDesconhecida(): Boolean =
        idade >= T.IDADE_ALERTA_FILIACAO_DESCONHECIDA

    /** Sub-gate: só exibe campos de cartório se estadoCivil == CASADO */
    fun perguntarCasamentoEmCartorio(): Boolean =
        perguntarEstadoCivilCasamento() && estadoCivil == EstadoCivil.CASADO

    fun exibirCamposCasamentoCartorio(): Boolean =
        perguntarCasamentoEmCartorio() && casamentoEmCartorio == true

    fun exibirCamposEleitor(): Boolean = perguntarEleitor() && eleitor == true

    fun exibirCamposReservista(): Boolean = perguntarReservista() && reservista == true

    fun exibirTestamento(): Boolean = deixaBens == true
}