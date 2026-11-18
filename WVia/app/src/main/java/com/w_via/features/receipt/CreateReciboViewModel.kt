package com.w_via.features.receipt

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.w_via.features.datamycnpj.LogoStorage
import com.w_via.features.utils.valorPorExtenso
import com.w_via.features.utils.ViaCepService
import com.w_via.opencnpj.OpenCNPJClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CreateReciboViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext get() = getApplication<Application>()

    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _isLoadingCNPJ = MutableStateFlow(false)
    val isLoadingCNPJ: StateFlow<Boolean> = _isLoadingCNPJ.asStateFlow()

    private val _cnpjError = MutableStateFlow<String?>(null)
    val cnpjError: StateFlow<String?> = _cnpjError.asStateFlow()

    private val _a = MutableStateFlow("00001")
    val a: StateFlow<String> = _a.asStateFlow()

    private val _nomeContratante = MutableStateFlow("")
    val nomeContratante: StateFlow<String> = _nomeContratante.asStateFlow()

    private val _cpf_cnpj = MutableStateFlow("")
    val cpf_cnpj: StateFlow<String> = _cpf_cnpj.asStateFlow()

    private val _cpf_cnpj_mode = MutableStateFlow("CNPJ")
    val cpf_cnpj_mode: StateFlow<String> = _cpf_cnpj_mode.asStateFlow()

    private val _cep_contratante = MutableStateFlow("")
    val cep_contratante: StateFlow<String> = _cep_contratante.asStateFlow()

    private val _buscandoCepContratante = MutableStateFlow(false)
    val buscandoCepContratante: StateFlow<Boolean> = _buscandoCepContratante.asStateFlow()

    private val _cepContratanteError = MutableStateFlow<String?>(null)
    val cepContratanteError: StateFlow<String?> = _cepContratanteError.asStateFlow()

    private val _endereco_contratante = MutableStateFlow("")
    val endereco_contratante: StateFlow<String> = _endereco_contratante.asStateFlow()

    private val _uf_contratante = MutableStateFlow("")
    val uf_contratante: StateFlow<String> = _uf_contratante.asStateFlow()

    private val _cidade_contratante = MutableStateFlow("")
    val cidade_contratante: StateFlow<String> = _cidade_contratante.asStateFlow()

    private val _numero_contratante = MutableStateFlow("")
    val numero_contratante: StateFlow<String> = _numero_contratante.asStateFlow()

    private val _bairro_contratante = MutableStateFlow("")
    val bairro_contratante: StateFlow<String> = _bairro_contratante.asStateFlow()

    private val _rg_ie = MutableStateFlow("")
    val rg_ie: StateFlow<String> = _rg_ie.asStateFlow()

    private val _tel = MutableStateFlow("")
    val tel: StateFlow<String> = _tel.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _nome_falecido = MutableStateFlow("")
    val nome_falecido: StateFlow<String> = _nome_falecido.asStateFlow()

    private val _idade_falecido = MutableStateFlow("")
    val idade_falecido: StateFlow<String> = _idade_falecido.asStateFlow()

    private val _sexo_falecido = MutableStateFlow("")
    val sexo_falecido: StateFlow<String> = _sexo_falecido.asStateFlow()

    private val _naturalidade_falecido = MutableStateFlow("")
    val naturalidade_falecido: StateFlow<String> = _naturalidade_falecido.asStateFlow()

    private val _uf_falecido = MutableStateFlow("")
    val uf_falecido: StateFlow<String> = _uf_falecido.asStateFlow()

    private val _rg_falecido = MutableStateFlow("")
    val rg_falecido: StateFlow<String> = _rg_falecido.asStateFlow()

    private val _cpf_falecido = MutableStateFlow("")
    val cpf_falecido: StateFlow<String> = _cpf_falecido.asStateFlow()

    private val _filiacao_falecido = MutableStateFlow("")
    val filiacao_falecido: StateFlow<String> = _filiacao_falecido.asStateFlow()

    private val _pai_falecido = MutableStateFlow("")
    val pai_falecido: StateFlow<String> = _pai_falecido.asStateFlow()

    private val _mae_falecido = MutableStateFlow("")
    val mae_falecido: StateFlow<String> = _mae_falecido.asStateFlow()

    private val _local_funeral = MutableStateFlow("")
    val local_funeral: StateFlow<String> = _local_funeral.asStateFlow()

    private val _tipo_local_saida = MutableStateFlow<TipoLocalSaida?>(null)
    val tipo_local_saida: StateFlow<TipoLocalSaida?> = _tipo_local_saida.asStateFlow()

    private val _uf_local_saida = MutableStateFlow("")
    val uf_local_saida: StateFlow<String> = _uf_local_saida.asStateFlow()

    private val _cidade_local_saida = MutableStateFlow("")
    val cidade_local_saida: StateFlow<String> = _cidade_local_saida.asStateFlow()

    private val _hora_saida_funeral = MutableStateFlow("")
    val hora_saida_funeral: StateFlow<String> = _hora_saida_funeral.asStateFlow()

    private val _dia_funeral = MutableStateFlow<LocalDate?>(null)
    val dia_funeral: StateFlow<LocalDate?> = _dia_funeral.asStateFlow()

    private val _cemiterio_sepultamento = MutableStateFlow("")
    val cemiterio_sepultamento: StateFlow<String> = _cemiterio_sepultamento.asStateFlow()

    private val _tipo_sepultamento = MutableStateFlow<TipoSepultamento?>(null)
    val tipo_sepultamento: StateFlow<TipoSepultamento?> = _tipo_sepultamento.asStateFlow()

    private val _uf_sepultamento = MutableStateFlow("")
    val uf_sepultamento: StateFlow<String> = _uf_sepultamento.asStateFlow()

    private val _cidade_sepultamento = MutableStateFlow("")
    val cidade_sepultamento: StateFlow<String> = _cidade_sepultamento.asStateFlow()

    private val _descricao = MutableStateFlow("")
    val descricao: StateFlow<String> = _descricao.asStateFlow()

    private val _quantidade = MutableStateFlow(0)
    val quantidade: StateFlow<Int> = _quantidade.asStateFlow()

    private val _unidade = MutableStateFlow(0)
    val unidade: StateFlow<Int> = _unidade.asStateFlow()

    private val _valor = MutableStateFlow(0.0)
    val valor: StateFlow<Double> = _valor.asStateFlow()

    private val _total_pago = MutableStateFlow(0.0)
    val total_pago: StateFlow<Double> = _total_pago.asStateFlow()

    private val _valor_por_extenso = MutableStateFlow("")
    val valor_por_extenso: StateFlow<String> = _valor_por_extenso.asStateFlow()

    private val _valor_cheque = MutableStateFlow(0.0)
    val valor_cheque: StateFlow<Double> = _valor_cheque.asStateFlow()

    private val _rg_contratante = MutableStateFlow("")
    val rg_contratante: StateFlow<String> = _rg_contratante.asStateFlow()

    private val _data_pagamento = MutableStateFlow("") // Mudado para String
    val data_pagamento: StateFlow<String> = _data_pagamento.asStateFlow()

    private val _logoUri = MutableStateFlow<Uri?>(null)
    val logoUri: StateFlow<Uri?> = _logoUri.asStateFlow()

    private val _editingReciboId = MutableStateFlow(0L)
    val editingReciboId: StateFlow<Long> = _editingReciboId.asStateFlow()

    init {
        _logoUri.value = LogoStorage.load(appContext)
        loadDraftIfExists()
        if (_data_pagamento.value.isBlank()) {
            _data_pagamento.value = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date())
        }
    }

    /** Atualiza a logo (ex.: após forçar o download antes de emitir o recibo). */
    fun refreshLogo(uri: Uri?) {
        _logoUri.value = uri
    }

    private fun loadDraftIfExists() {
        val draft = ReciboDraftStorage.load(appContext) ?: return
        _currentStep.value = draft.currentStep
        _a.value = draft.a
        _nomeContratante.value = draft.nomeContratante
        _cpf_cnpj.value = draft.cpf_cnpj
        _cpf_cnpj_mode.value = draft.cpf_cnpj_mode
        _cep_contratante.value = draft.cep_contratante
        _endereco_contratante.value = draft.endereco_contratante
        _uf_contratante.value = draft.uf_contratante
        _cidade_contratante.value = draft.cidade_contratante
        _numero_contratante.value = draft.numero_contratante
        _bairro_contratante.value = draft.bairro_contratante
        _rg_ie.value = draft.rg_ie
        _tel.value = draft.tel
        _email.value = draft.email
        _nome_falecido.value = draft.nome_falecido
        _idade_falecido.value = draft.idade_falecido
        _sexo_falecido.value = draft.sexo_falecido
        _naturalidade_falecido.value = draft.naturalidade_falecido
        _uf_falecido.value = draft.uf_falecido
        _rg_falecido.value = draft.rg_falecido
        _cpf_falecido.value = draft.cpf_falecido
        _filiacao_falecido.value = draft.filiacao_falecido
        _pai_falecido.value = draft.pai_falecido
        _mae_falecido.value = draft.mae_falecido
        _local_funeral.value = draft.local_funeral
        _tipo_local_saida.value = runCatching {
            TipoLocalSaida.valueOf(draft.tipo_local_saida)
        }.getOrNull()
        _uf_local_saida.value = draft.uf_local_saida
        _cidade_local_saida.value = draft.cidade_local_saida
        _hora_saida_funeral.value = draft.hora_saida_funeral
        _dia_funeral.value = draft.dia_funeral?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        _cemiterio_sepultamento.value = draft.cemiterio_sepultamento
        _tipo_sepultamento.value = runCatching {
            TipoSepultamento.valueOf(draft.tipo_sepultamento)
        }.getOrNull()
        _uf_sepultamento.value = draft.uf_sepultamento
        _cidade_sepultamento.value = draft.cidade_sepultamento
        _descricao.value = draft.descricao
        _quantidade.value = draft.quantidade
        _unidade.value = draft.unidade
        _valor.value = draft.valor
        _total_pago.value = draft.total_pago
        if (_valor_por_extenso.value.isBlank()) {
            _valor_por_extenso.value = valorPorExtenso(_total_pago.value)
        }
        _valor_cheque.value = draft.valor_cheque
        _rg_contratante.value = draft.rg_contratante
        _data_pagamento.value = draft.data_pagamento
        _editingReciboId.value = draft.editing_recibo_id
    }

    private fun persistDraft() {
        val draft = ReciboDraft(
            currentStep = _currentStep.value,
            a = _a.value,
            nomeContratante = _nomeContratante.value,
            cpf_cnpj = _cpf_cnpj.value,
            cpf_cnpj_mode = _cpf_cnpj_mode.value,
            cep_contratante = _cep_contratante.value,
            endereco_contratante = _endereco_contratante.value,
            uf_contratante = _uf_contratante.value,
            cidade_contratante = _cidade_contratante.value,
            numero_contratante = _numero_contratante.value,
            bairro_contratante = _bairro_contratante.value,
            rg_ie = _rg_ie.value,
            tel = _tel.value,
            email = _email.value,
            nome_falecido = _nome_falecido.value,
            idade_falecido = _idade_falecido.value,
            sexo_falecido = _sexo_falecido.value,
            naturalidade_falecido = _naturalidade_falecido.value,
            uf_falecido = _uf_falecido.value,
            rg_falecido = _rg_falecido.value,
            cpf_falecido = _cpf_falecido.value,
            filiacao_falecido = _filiacao_falecido.value,
            pai_falecido = _pai_falecido.value,
            mae_falecido = _mae_falecido.value,
            local_funeral = _local_funeral.value,
            tipo_local_saida = _tipo_local_saida.value?.name.orEmpty(),
            uf_local_saida = _uf_local_saida.value,
            cidade_local_saida = _cidade_local_saida.value,
            hora_saida_funeral = _hora_saida_funeral.value,
            dia_funeral = _dia_funeral.value?.toString(),
            cemiterio_sepultamento = _cemiterio_sepultamento.value,
            tipo_sepultamento = _tipo_sepultamento.value?.name.orEmpty(),
            uf_sepultamento = _uf_sepultamento.value,
            cidade_sepultamento = _cidade_sepultamento.value,
            descricao = _descricao.value,
            quantidade = _quantidade.value,
            unidade = _unidade.value,
            valor = _valor.value,
            total_pago = _total_pago.value,
            valor_cheque = _valor_cheque.value,
            rg_contratante = _rg_contratante.value,
            data_pagamento = _data_pagamento.value,
            editing_recibo_id = _editingReciboId.value
        )
        ReciboDraftStorage.save(appContext, draft)
    }

    /**
     * Após salvar o novo recibo (fluxo de edição), marca o recibo original como
     * desatualizado (faixa vermelha) e limpa o flag de edição do rascunho.
     */
    suspend fun markObsoleteOnEdit(repository: com.w_via.features.receipt.data.ReciboRepository) {
        val id = _editingReciboId.value
        if (id != 0L) {
            runCatching { repository.markObsolete(id) }
            _editingReciboId.value = 0L
            persistDraft()
        }
    }

    fun clearEditingFlag() {
        if (_editingReciboId.value != 0L) {
            _editingReciboId.value = 0L
            persistDraft()
        }
    }

    fun deleteDraft() {
        ReciboDraftStorage.delete(appContext)
    }

    fun updateA(value: String) { _a.value = value; persistDraft() }
    fun updateCpfCnpjMode(value: String) { _cpf_cnpj_mode.value = value; persistDraft() }
    fun updateNomeContratante(value: String) { _nomeContratante.value = value; persistDraft() }
    fun updateCpfCnpj(value: String) { _cpf_cnpj.value = value; persistDraft() }
    fun updateCepContratante(value: String) {
        _cep_contratante.value = value
        _cepContratanteError.value = null
        persistDraft()
    }
    fun clearCepContratanteError() { _cepContratanteError.value = null }
    fun buscarCepContratante() {
        _cepContratanteError.value = null
        viewModelScope.launch {
            _buscandoCepContratante.value = true
            ViaCepService.buscar(_cep_contratante.value)
                .onSuccess { endereco ->
                    _endereco_contratante.value = listOfNotNull(
                        endereco.logradouro.takeIf { it.isNotBlank() },
                        endereco.bairro.takeIf { it.isNotBlank() },
                        endereco.localidade
                            .takeIf { it.isNotBlank() }
                            ?.let { localidade ->
                                if (endereco.uf.isNotBlank()) "$localidade - ${endereco.uf}" else localidade
                            }
                    ).joinToString(", ")
                    _bairro_contratante.value = endereco.bairro.trim()
                    _cidade_contratante.value = endereco.localidade.trim()
                    _uf_contratante.value = endereco.uf.trim()
                    persistDraft()
                }
                .onFailure { error ->
                    _cepContratanteError.value = if (error is IllegalArgumentException) {
                        "CEP não encontrado"
                    } else {
                        "Erro ao buscar CEP"
                    }
                }
            _buscandoCepContratante.value = false
        }
    }
    fun updateEnderecoContratante(value: String) { _endereco_contratante.value = value; persistDraft() }
    fun updateUfContratante(value: String) { _uf_contratante.value = value.uppercase(); persistDraft() }
    fun updateCidadeContratante(value: String) { _cidade_contratante.value = value.uppercase(); persistDraft() }
    fun updateNumeroContratante(value: String) { _numero_contratante.value = value.uppercase(); persistDraft() }
    fun updateBairroContratante(value: String) { _bairro_contratante.value = value.uppercase(); persistDraft() }
    fun updateRgIe(value: String) { _rg_ie.value = value; persistDraft() }
    fun updateTel(value: String) { _tel.value = value; persistDraft() }
    fun updateEmail(value: String) { _email.value = value; persistDraft() }
    fun updateNomeFalecido(value: String) { _nome_falecido.value = value; persistDraft() }
    fun updateIdadeFalecido(value: String) { _idade_falecido.value = value; persistDraft() }
    fun updateSexoFalecido(value: String) { _sexo_falecido.value = value; persistDraft() }
    fun updateNaturalidadeFalecido(value: String) { _naturalidade_falecido.value = value; persistDraft() }
    fun updateUfFalecido(value: String) { _uf_falecido.value = value; persistDraft() }
    fun updateRgFalecido(value: String) { _rg_falecido.value = value; persistDraft() }
    fun updateCpfFalecido(value: String) { _cpf_falecido.value = value; persistDraft() }
    fun updateFiliacaoFalecido(value: String) { _filiacao_falecido.value = value; persistDraft() }
    fun updatePaiFalecido(value: String) { _pai_falecido.value = value; persistDraft() }
    fun updateMaeFalecido(value: String) { _mae_falecido.value = value; persistDraft() }

    /**
     * Composição da filiação para o PDF/recibo legado: junta Pai e Mãe separados
     * por vírgula quando preenchidos, mantendo o valor antigo como fallback.
     */
    private fun comporFiliacao(): String {
        val partes = buildList {
            _pai_falecido.value.trim().takeIf { it.isNotBlank() }?.let { add(it) }
            _mae_falecido.value.trim().takeIf { it.isNotBlank() }?.let { add(it) }
        }
        return if (partes.isNotEmpty()) partes.joinToString(", ") else _filiacao_falecido.value
    }
    fun updateLocalFuneral(value: String) { _local_funeral.value = value; persistDraft() }
    fun updateTipoLocalSaida(value: TipoLocalSaida) { _tipo_local_saida.value = value; persistDraft() }
    fun updateUfLocalSaida(value: String) { _uf_local_saida.value = value; persistDraft() }
    fun updateCidadeLocalSaida(value: String) { _cidade_local_saida.value = value; persistDraft() }
    fun updateHoraSaidaFuneral(value: String) { _hora_saida_funeral.value = value; persistDraft() }
    fun updateDiaFuneral(value: LocalDate?) { _dia_funeral.value = value; persistDraft() }
    fun updateCemiterioSepultamento(value: String) { _cemiterio_sepultamento.value = value; persistDraft() }
    fun updateTipoSepultamento(value: TipoSepultamento) { _tipo_sepultamento.value = value; persistDraft() }
    fun updateUfSepultamento(value: String) { _uf_sepultamento.value = value; persistDraft() }
    fun updateCidadeSepultamento(value: String) { _cidade_sepultamento.value = value; persistDraft() }
    fun updateDescricao(value: String) { _descricao.value = value; persistDraft() }
    fun updateQuantidade(value: Int) { _quantidade.value = value; persistDraft() }
    fun updateUnidade(value: Int) { _unidade.value = value; persistDraft() }
    fun updateValor(value: Double) { _valor.value = value; persistDraft() }
    fun updateTotalPago(value: Double) {
        _total_pago.value = value
        _valor_por_extenso.value = valorPorExtenso(value)
        persistDraft()
    }
    fun updateValorPorExtenso(value: String) { _valor_por_extenso.value = value; persistDraft() }
    fun updateValorCheque(value: Double) { _valor_cheque.value = value; persistDraft() }
    fun updateRgContratante(value: String) { _rg_contratante.value = value; persistDraft() }
    fun updateDataPagamento(value: String) { _data_pagamento.value = value; persistDraft() }

    fun nextStep() {
        if (_currentStep.value < 4) {
            _currentStep.value++
            persistDraft()
        }
    }

    fun previousStep() {
        if (_currentStep.value > 1) {
            _currentStep.value--
            persistDraft()
        }
    }

    fun goToStep(step: Int) {
        if (step in 1..4) {
            _currentStep.value = step
            persistDraft()
        }
    }

    fun searchCNPJ() {
        val cnpj = _cpf_cnpj.value
        if (cnpj.isBlank()) {
            _cnpjError.value = "Digite um CNPJ"
            return
        }

        viewModelScope.launch {
            _isLoadingCNPJ.value = true
            _cnpjError.value = null

            try {
                val client = OpenCNPJClient.getInstance()
                val result = client.getCompanyInfo(cnpj)

                if (result.isSuccess) {
                    val companyInfo = result.getOrNull()
                    companyInfo?.let { info ->
                        _nomeContratante.value = info.corporateName ?: info.tradeName ?: ""
                        val address = listOfNotNull(
                            info.street, info.number, info.complement,
                            info.neighborhood, info.city, info.state
                        ).filter { it.isNotBlank() }.joinToString(", ")
                        _endereco_contratante.value = address
                        _numero_contratante.value = info.number.orEmpty().trim()
                        _bairro_contratante.value = info.neighborhood.orEmpty().trim()
                        _cidade_contratante.value = info.city.orEmpty().trim()
                        _uf_contratante.value = info.state.orEmpty().trim()
                        _tel.value = info.phone?.filter { it.isDigit() } ?: ""
                        _email.value = info.email ?: ""
                        persistDraft()
                    }
                } else {
                    _cnpjError.value = result.exceptionOrNull()?.message ?: "Erro ao buscar CNPJ"
                }
            } catch (e: Exception) {
                _cnpjError.value = "Erro: ${e.message}"
            } finally {
                _isLoadingCNPJ.value = false
            }
        }
    }

    /** Preenche a descrição do serviço com o texto de translado montado a partir
     *  do local de saída e do local de sepultamento (com cidade e UF, quando houver).
     *  Quando a descrição já contém texto, o novo translado é acrescentado com ", ". */
    fun adicionarTranslado() {
        val texto = buildTransladoDescription(
            localSaida = _local_funeral.value,
            cidadeSaida = _cidade_local_saida.value,
            ufSaida = _uf_local_saida.value,
            localSepultamento = _cemiterio_sepultamento.value,
            cidadeSepultamento = _cidade_sepultamento.value,
            ufSepultamento = _uf_sepultamento.value
        )
        if (texto.isNotBlank()) {
            _descricao.value = if (_descricao.value.isNotBlank()) {
                "${_descricao.value.trimEnd()}, $texto"
            } else {
                texto
            }
            persistDraft()
        }
    }

    fun criarRecibo(
        numero: String = "",
        enderecoAgente: String = "",
        cnpjAgente: String = ""
    ): Recibo {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

        val diaFuneralStr = _dia_funeral.value?.let {
            "%02d/%02d/%04d".format(it.dayOfMonth, it.monthNumber, it.year)
        } ?: ""

        return Recibo(
            uuid = UUID.randomUUID().toString(),
            numero = numero,
            logoUri = _logoUri.value,
            nomeContratante = _nomeContratante.value,
            rg_ie = _rg_ie.value,
            cpf_cnpj = _cpf_cnpj.value,
            endereco_contratante = _endereco_contratante.value,
            tel = _tel.value,
            email = _email.value,
            nome_falecido = _nome_falecido.value,
            estado_civil_falecido = "",
            idade_falecido = _idade_falecido.value,
            sexo_falecido = _sexo_falecido.value,
            naturalidade_falecido = _naturalidade_falecido.value,
            uf_falecido = _uf_falecido.value,
            rg_falecido = _rg_falecido.value,
            cpf_falecido = _cpf_falecido.value,
            pai_falecido = _pai_falecido.value,
            mae_falecido = _mae_falecido.value,
            filiacao_falecido = comporFiliacao(),
            local_funeral = _local_funeral.value,
            hora_saida_funeral = _hora_saida_funeral.value,
            dia_funeral = diaFuneralStr,
            cemiterio_sepultamento = _cemiterio_sepultamento.value,
            quantidade = "1",
            descricao = _descricao.value,
            unidade = "",
            valor = _total_pago.value.toString(),
            total_pago = _total_pago.value.toString(),
            valor_por_extenso = _valor_por_extenso.value,
            data_pagamento = _data_pagamento.value,
            ass_contratante1 = "",
            ass_contratante2 = "",
            ass_contratante3 = "",
            n_cheque = "",
            valor_cheque = _valor_cheque.value.toString(),
            banco = "",
            agencia = "",
            rg_contratante = _rg_contratante.value,
            emitente_cheque = "",
            endereco_agente = enderecoAgente,
            cnpj_agente = cnpjAgente
        )
    }
}
