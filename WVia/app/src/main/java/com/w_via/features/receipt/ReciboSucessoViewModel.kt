package com.w_via.features.receipt

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w_via.features.datamycnpj.LogoStorage
import com.w_via.features.datamycnpj.logoDisponivel
import com.w_via.features.receipt.composeToPdf.PdfDocumentGenerator
import com.w_via.features.receipt.composeToPdf.PdfPageSize
import com.w_via.features.receipt.composeToPdf.pdfSizeKb
import com.w_via.features.receipt.data.ReciboRepository
import com.w_via.features.receipt.layouts.ReciboPDFLayout
import com.w_via.features.sync.CompanyProfileSync
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReciboSucessoViewModel(
    private val repository: ReciboRepository,
    private val companyProfileSync: CompanyProfileSync
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Ready(
            val pdfUri: Uri,
            val pdfName: String,
            val pdfSize: String,
            val generatedAt: String
        ) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Fluxo de criação: PDF já foi gerado, só exibe os dados
    fun setGeneratedPdf(uri: Uri, name: String, size: String, generatedAt: String) {
        _uiState.value = UiState.Ready(uri, name, size, generatedAt)
    }

    fun setError(message: String) {
        _uiState.value = UiState.Error(message)
    }

    // Fluxo de recibo existente: busca no Room e regenera o PDF
    fun loadExistingRecibo(context: Context, reciboId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val id = reciboId.toLongOrNull() ?: throw Exception("Recibo não encontrado")
                val recibo = repository.getById(id) ?: throw Exception("Recibo não encontrado")

                // Garante a logo da funerária antes de emitir o recibo.
                var logo = recibo.logoUri
                if (!logoDisponivel(context, logo)) {
                    runCatching { companyProfileSync.sync() }
                    logo = LogoStorage.load(context) ?: recibo.logoUri
                }
                val reciboParaPdf = recibo.copy(logoUri = logo)

                val fileName = "recibo_$reciboId.pdf"
                val pdfGenerator = PdfDocumentGenerator(context)
                val uri = pdfGenerator.generate(
                    fileName = fileName,
                    pageSize = PdfPageSize.A4(72),
                    pages = listOf { ReciboPDFLayout(reciboParaPdf) }
                ) ?: throw Exception("Erro ao gerar PDF")

                val sizeKb = pdfSizeKb(context, uri, fileName)
                val name = uri.lastPathSegment ?: "recibo.pdf"

                _uiState.value = UiState.Ready(uri, name, sizeKb, "")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Erro ao carregar recibo")
            }
        }
    }
}
