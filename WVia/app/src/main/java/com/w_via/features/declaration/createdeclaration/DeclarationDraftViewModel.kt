package com.w_via.features.declaration.createdeclaration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w_via.features.declaration.DeclarationRules
import com.w_via.features.declaration.DeclaracaoObito
import com.w_via.features.declaration.DeclaracaoObitoThresholds
import com.w_via.features.declaration.state.DeclaracaoObitoFormState
import com.w_via.features.declaration.toDeclaracaoObito
import com.w_via.features.declaration.toFormState
import com.w_via.features.utils.EnderecoViaCep
import com.w_via.features.utils.ViaCepService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel do rascunho da Declaração de Óbito, compartilhado entre o grid
 * (DECLARATION_GRID) e as telas de seção. Mantém o estado em memória durante
 * todo o fluxo — daí "dados da memória" para a geração do PDF.
 *
 * Ao ser escopado ao back stack entry do grid, todas as telas de seção
 * compartilham a mesma instância via `viewModel(parentEntry)`.
 */
class DeclarationDraftViewModel : ViewModel() {

    /** Semente de edição: guarda a declaração a ser editada até o grid abrir. */
    companion object {
        @Volatile
        var editSeed: com.w_via.features.declaration.DeclaracaoObito? = null

        @Volatile
        var editSeedUuid: String = ""
    }

    private val _state = MutableStateFlow(DeclaracaoObitoFormState(causasMorte = listOf("")))
    val state: StateFlow<DeclaracaoObitoFormState> = _state.asStateFlow()

    private val _confirmedSections = MutableStateFlow<Set<DeclarationSection>>(emptySet())
    val confirmedSections: StateFlow<Set<DeclarationSection>> = _confirmedSections.asStateFlow()

    /** Resultado da geração do PDF, mantido no ViewModel para sobreviver à rotação. */
    data class PdfResult(val isSuccess: Boolean, val message: String)

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _pdfResult = MutableStateFlow<PdfResult?>(null)
    val pdfResult: StateFlow<PdfResult?> = _pdfResult.asStateFlow()

    /** Mensagem do modal de conformidade quando campos foram limpos por conflito. */
    private val _conflictMessage = MutableStateFlow<String?>(null)
    val conflictMessage: StateFlow<String?> = _conflictMessage.asStateFlow()

    /** Indicador de busca de CEP em andamento (lupa mostra progresso). */
    private val _buscandoCep = MutableStateFlow(false)
    val buscandoCep: StateFlow<Boolean> = _buscandoCep.asStateFlow()

    /** Mensagem de erro da busca de CEP, exibida como texto de apoio do campo. */
    private val _cepBuscaError = MutableStateFlow<String?>(null)
    val cepBuscaError: StateFlow<String?> = _cepBuscaError.asStateFlow()

    fun setGenerating(value: Boolean) {
        _isGenerating.value = value
    }

    fun setPdfResult(result: PdfResult?) {
        _pdfResult.value = result
        if (result != null) _isGenerating.value = false
    }

    /** Descarta a mensagem de conflito (após o usuário ler o modal). */
    fun clearConflict() {
        _conflictMessage.value = null
    }

    /** Descarta a mensagem de erro da busca de CEP (quando o usuário edita o CEP). */
    fun clearCepBuscaError() {
        _cepBuscaError.value = null
    }

    /**
     * Atualização genérica de qualquer campo via copy, evita um setter por campo.
     * O estado é normalizado após a alteração para remover dados que conflitam
     * com as regras de exibição — campos limpos geram a mensagem de conformidade.
     */
    fun update(transform: (DeclaracaoObitoFormState) -> DeclaracaoObitoFormState) {
        val next = transform(_state.value)
        val normalized = DeclarationRules.normalize(next)
        _state.value = normalized.state
        if (normalized.clearedFields.isNotEmpty()) {
            _conflictMessage.value = DeclarationRules.conflictMessage(normalized.clearedFields)
        }
    }

    /**
     * Marca a seção como confirmada se ela estiver completa. Retorna false
     * (e não marca) quando os campos obrigatórios da seção não foram preenchidos.
     */
    fun confirm(section: DeclarationSection): Boolean {
        if (!section.isComplete(_state.value)) return false
        _confirmedSections.value = _confirmedSections.value + section
        return true
    }

    /** Todas as seções obrigatórias (estáticas ou dinâmicas) foram confirmadas e estão completas. */
    fun canSave(): Boolean =
        DeclarationSection.entries.all { section ->
            !section.isRequiredFor(_state.value) ||
                (section in _confirmedSections.value && section.isComplete(_state.value))
        }

    /** Quantidade de seções obrigatórias já confirmadas (para feedback de progresso). */
    fun requiredCompletedCount(): Int =
        DeclarationSection.entries.count { section ->
            section.isRequiredFor(_state.value) &&
                section in _confirmedSections.value &&
                section.isComplete(_state.value)
        }

    /** Total de seções obrigatórias no estado atual. */
    fun requiredTotal(): Int =
        DeclarationSection.entries.count { it.isRequiredFor(_state.value) }

    /** Indica se há conteúdo preenchido ou seções confirmadas (para avisar antes de sair). */
    fun hasContent(): Boolean {
        val vazio = DeclaracaoObitoFormState(causasMorte = listOf(""))
        return _state.value != vazio || _confirmedSections.value.isNotEmpty()
    }

    fun adicionarFilho() = update { state ->
        if (state.filhos.size < DeclaracaoObitoThresholds.MAX_FILHOS) {
            state.copy(filhos = state.filhos + "")
        } else {
            state
        }
    }

    fun atualizarFilho(index: Int, nome: String) {
        update { state ->
            val atualizados = state.filhos.toMutableList()
            if (index in atualizados.indices) {
                atualizados[index] = nome
                state.copy(filhos = atualizados)
            } else {
                state
            }
        }
    }

    fun removerFilho(index: Int) {
        update { state ->
            val atualizados = state.filhos.toMutableList()
            if (index in atualizados.indices) {
                atualizados.removeAt(index)
                state.copy(filhos = atualizados)
            } else {
                state
            }
        }
    }

    fun adicionarCausa() = update { state ->
        if (state.causasMorte.size < DeclaracaoObitoThresholds.MAX_CAUSAS_MORTE) {
            state.copy(causasMorte = state.causasMorte + "")
        } else {
            state
        }
    }

    fun atualizarCausa(index: Int, texto: String) {
        update { state ->
            val atualizadas = state.causasMorte.toMutableList()
            if (index in atualizadas.indices) {
                atualizadas[index] = texto
                state.copy(causasMorte = atualizadas)
            } else {
                state
            }
        }
    }

    fun removerCausa(index: Int) {
        update { state ->
            val atualizadas = state.causasMorte.toMutableList()
            if (index in atualizadas.indices) {
                atualizadas.removeAt(index)
                // Causa 1 sempre visível: nunca deixa a lista vazia.
                state.copy(causasMorte = atualizadas.ifEmpty { listOf("") })
            } else {
                state
            }
        }
    }

    /** Busca o endereço do CEP do falecido e preenche os campos correspondentes. */
    fun buscarCepFalecido() {
        buscarCep(_state.value.cep) { endereco ->
            update { state ->
                state.copy(
                    endereco = endereco.logradouro,
                    cidade = endereco.localidade,
                    uf = endereco.uf
                )
            }
        }
    }

    /** Busca o endereço do CEP do declarante e preenche os campos correspondentes. */
    fun buscarCepDeclarante() {
        buscarCep(_state.value.cepDeclarante) { endereco ->
            update { state ->
                state.copy(
                    residenciaDeclarante = endereco.logradouro,
                    bairroDeclarante = endereco.bairro,
                    municipioDeclarante = endereco.localidade,
                    ufDeclarante = endereco.uf
                )
            }
        }
    }

    /** Busca o endereço do CEP do velório e preenche os campos correspondentes. */
    fun buscarCepVelorio() {
        buscarCep(_state.value.cepVelorio) { endereco ->
            update { state ->
                state.copy(
                    enderecoVelorio = endereco.logradouro,
                    bairroVelorio = endereco.bairro,
                    cidadeVelorio = endereco.localidade,
                    ufVelorio = endereco.uf
                )
            }
        }
    }

    private fun buscarCep(cep: String, onResult: (EnderecoViaCep) -> Unit) {
        if (_buscandoCep.value) return
        viewModelScope.launch {
            _buscandoCep.value = true
            _cepBuscaError.value = null
            ViaCepService.buscar(cep)
                .onSuccess(onResult)
                .onFailure { error ->
                    _cepBuscaError.value = if (error is IllegalArgumentException) {
                        "CEP não encontrado"
                    } else {
                        "Erro ao buscar CEP"
                    }
                }
            _buscandoCep.value = false
        }
    }

    /** Converte o rascunho em memória para o modelo de domínio da Declaração de Óbito. */
    fun toDeclaracaoObito(): DeclaracaoObito = _state.value.toDeclaracaoObito()

    /**
     * Carrega uma declaração salva no rascunho para o fluxo de edição.
     * Mantém o uuid original para que o salvamento gere um novo registro
     * (o original é preservado como desatualizado, se desejado).
     */
    fun loadFromSaved(declaracao: DeclaracaoObito) {
        _state.value = declaracao.toFormState()
        _confirmedSections.value = emptySet()
        editSeed = null
        editSeedUuid = ""
    }

    /** Limpa o rascunho (após salvar/gerar o PDF). */
    fun reset() {
        _state.value = DeclaracaoObitoFormState(causasMorte = listOf(""))
        _confirmedSections.value = emptySet()
        _isGenerating.value = false
        _pdfResult.value = null
        _conflictMessage.value = null
        _buscandoCep.value = false
        _cepBuscaError.value = null
    }
}
