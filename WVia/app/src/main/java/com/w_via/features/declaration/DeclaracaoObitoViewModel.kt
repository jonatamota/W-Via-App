package com.w_via.features.declaration

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import com.w_via.features.declaration.*
import com.w_via.features.declaration.state.*
import com.w_via.features.declaration.createdeclaration.DeclarationSection

class DeclaracaoObitoViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(DeclaracaoObitoFormState(causasMorte = listOf("")))
    val state: StateFlow<DeclaracaoObitoFormState> = _state.asStateFlow()

    /** Atualização genérica de qualquer campo via copy, evita um setter por campo. */
    fun update(transform: (DeclaracaoObitoFormState) -> DeclaracaoObitoFormState) {
        _state.value = transform(_state.value)
    }

    fun avancar() {
        _state.value.step.proximo()?.let { proximo ->
            _state.value = _state.value.copy(step = proximo)
        }
    }

    fun voltar() {
        _state.value.step.anterior()?.let { anterior ->
            _state.value = _state.value.copy(step = anterior)
        }
    }

    fun irParaStep(step: DeclaracaoObitoStep) {
        _state.value = _state.value.copy(step = step)
    }

    fun adicionarFilho() {
        _state.value = _state.value.copy(filhos = _state.value.filhos + "")
    }

    fun atualizarFilho(index: Int, nome: String) {
        val atualizados = _state.value.filhos.toMutableList()
        if (index in atualizados.indices) {
            atualizados[index] = nome
            _state.value = _state.value.copy(filhos = atualizados)
        }
    }

    fun removerFilho(index: Int) {
        val atualizados = _state.value.filhos.toMutableList()
        if (index in atualizados.indices) {
            atualizados.removeAt(index)
            _state.value = _state.value.copy(filhos = atualizados)
        }
    }

    fun adicionarCausa() {
        if (_state.value.causasMorte.size < DeclaracaoObitoThresholds.MAX_CAUSAS_MORTE) {
            _state.value = _state.value.copy(causasMorte = _state.value.causasMorte + "")
        }
    }

    fun atualizarCausa(index: Int, texto: String) {
        val atualizadas = _state.value.causasMorte.toMutableList()
        if (index in atualizadas.indices) {
            atualizadas[index] = texto
            _state.value = _state.value.copy(causasMorte = atualizadas)
        }
    }

    fun removerCausa(index: Int) {
        val atualizadas = _state.value.causasMorte.toMutableList()
        if (index in atualizadas.indices) {
            atualizadas.removeAt(index)
            // Causa 1 sempre visível: nunca deixa a lista vazia.
            _state.value = _state.value.copy(causasMorte = atualizadas.ifEmpty { listOf("") })
        }
    }

    /**
     * Validação mínima por step — habilita/desabilita o botão Avançar.
     * As regras são delegadas às seções correspondentes ([DeclarationSection.isComplete])
     * para manter uma única fonte de verdade com o fluxo por seções.
     */
    fun stepAtualValido(): Boolean {
        val s = _state.value
        return s.step.toDeclarationSection()?.isComplete(s) ?: true
    }

    /** Converte o estado editável (Strings) para os data classes finais do domínio. */
    fun toDeclaracaoObito(): DeclaracaoObito = _state.value.toDeclaracaoObito()
}

/**
 * Mapeia os steps do fluxo linear para as seções do fluxo por seções.
 * Steps sem seção equivalente (etapas de preenchimento livre) retornam null,
 * mantendo o botão Avançar sempre habilitado nesses passos.
 */
private fun DeclaracaoObitoStep.toDeclarationSection(): DeclarationSection? = when (this) {
    DeclaracaoObitoStep.IDENTIFICACAO -> DeclarationSection.FALECIDO
    DeclaracaoObitoStep.FILIACAO -> DeclarationSection.FILIACAO
    DeclaracaoObitoStep.OBITO -> DeclarationSection.OBITO
    DeclaracaoObitoStep.DECLARANTE -> DeclarationSection.DECLARANTE
    else -> null
}