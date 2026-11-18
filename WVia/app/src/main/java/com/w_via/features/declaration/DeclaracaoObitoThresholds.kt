package com.w_via.features.declaration

/**
 * Constantes centralizadas para as regras de exclusão condicional
 * dos campos da Declaração de Óbito. Ajuste aqui, sem tocar na lógica.
 */
object DeclaracaoObitoThresholds {
    const val MIN_IDADE_CASAMENTO = 16
    const val MIN_IDADE_ELEITOR = 16
    const val MIN_IDADE_RESERVISTA = 18
    const val MAX_IDADE_RESERVISTA = 45
    const val MIN_IDADE_FILHOS_PROVAVEL = 10
    const val MAX_FILHOS = 20
    const val MAX_CAUSAS_MORTE = 10
    const val IDADE_ALERTA_FILIACAO_DESCONHECIDA = 90
}