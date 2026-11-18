package com.w_via.features.declaration

import com.w_via.features.declaration.state.DeclaracaoObitoFormState

/**
 * Regras de exibição/exclusão de campos da Declaração de Óbito, puras e testáveis.
 *
 * O [normalize] é aplicado a cada atualização de estado e garante que dados de
 * campos que deixaram de ser aplicáveis (pela idade, sexo ou estado civil) sejam
 * removidos — o que também alimenta o modal de conformidade quando algo foi limpo.
 *
 * Regras implementadas:
 *  - menor de 16 anos → estado civil marcado automaticamente como SOLTEIRO;
 *  - menor de 10 anos → campo de filhos não aplicável;
 *  - sexo feminino (ou idade fora da faixa) → campos de reservista não aplicáveis;
 *  - estado civil solteiro (ou não informado) → cônjuge não aplicável;
 *  - apenas casado → dados de casamento em cartório aplicáveis.
 *
 * As flags `excecao*` do estado permitem revelar (informar mesmo assim) campos
 * ocultos em casos excepcionais, impedindo a limpeza automática.
 */
object DeclarationRules {

    data class NormalizationResult(
        val state: DeclaracaoObitoFormState,
        val clearedFields: List<String>
    )

    fun normalize(state: DeclaracaoObitoFormState): NormalizationResult {
        var result = state
        val cleared = mutableListOf<String>()

        val idadeInt = result.idade.toIntOrNull()

        // --- Estado civil automático (menor de 16) ---
        if (idadeInt != null && idadeInt < DeclaracaoObitoThresholds.MIN_IDADE_CASAMENTO) {
            if (result.excecaoEstadoCivil) {
                // Exceção mantida: usuário optou por informar mesmo assim.
            } else if (result.estadoCivil != null && result.estadoCivil != EstadoCivil.SOLTEIRO) {
                cleared += "Estado civil"
                result = result.copy(estadoCivil = EstadoCivil.SOLTEIRO)
            } else {
                result = result.copy(estadoCivil = EstadoCivil.SOLTEIRO)
            }
        } else if (idadeInt != null) {
            result = result.copy(excecaoEstadoCivil = false)
        }

        // --- Cônjuge ---
        if (!result.mostrarConjuge() && result.nomeConjuge.isNotBlank()) {
            cleared += "Nome do cônjuge"
            result = result.copy(nomeConjuge = "")
        }

        // --- Casamento em cartório (apenas casado) ---
        if (
            result.estadoCivil != EstadoCivil.CASADO &&
            (result.casamentoEmCartorio != null ||
                result.casamentoDia.isNotBlank() ||
                result.casamentoLivro.isNotBlank() ||
                result.casamentoFolha.isNotBlank() ||
                result.casamentoNumero.isNotBlank())
        ) {
            cleared += "Casamento em cartório"
            result = result.copy(
                casamentoEmCartorio = null,
                casamentoDia = "",
                casamentoLivro = "",
                casamentoFolha = "",
                casamentoNumero = ""
            )
        }

        // --- Filhos (menor de 10) ---
        if (idadeInt != null && idadeInt < DeclaracaoObitoThresholds.MIN_IDADE_FILHOS_PROVAVEL) {
            if (!result.excecaoFilhos && result.filhos.isNotEmpty()) {
                cleared += "Filhos"
                result = result.copy(filhos = emptyList())
            }
        } else if (idadeInt != null) {
            result = result.copy(excecaoFilhos = false)
        }

        // --- Reservista (apenas masculino na faixa etária) ---
        val reservistaAplicavel =
            result.sexo == Sexo.MASCULINO &&
                idadeInt != null &&
                idadeInt in DeclaracaoObitoThresholds.MIN_IDADE_RESERVISTA..DeclaracaoObitoThresholds.MAX_IDADE_RESERVISTA
        val temDadosReservista =
            result.reservista != null ||
                result.reservistaCarteira.isNotBlank() ||
                result.reservistaNumeroSerie.isNotBlank() ||
                result.reservistaCsm.isNotBlank() ||
                result.reservistaRm.isNotBlank()
        if (!reservistaAplicavel) {
            if (!result.excecaoReservista && temDadosReservista) {
                cleared += "Dados de reservista"
                result = result.copy(
                    reservista = null,
                    reservistaCarteira = "",
                    reservistaNumeroSerie = "",
                    reservistaCsm = "",
                    reservistaRm = ""
                )
            }
        } else {
            result = result.copy(excecaoReservista = false)
        }

        return NormalizationResult(state = result, clearedFields = cleared)
    }

    /** Mensagem exibida no modal de conformidade quando campos foram limpos. */
    fun conflictMessage(clearedFields: List<String>): String {
        val lista = clearedFields.joinToString(", ")
        return "A nova informação entra em conflito com os dados preenchidos anteriormente. " +
            "Para manter a conformidade, o(s) campo(s) \"$lista\" foi(foram) atualizado(s) — revise se necessário."
    }
}
