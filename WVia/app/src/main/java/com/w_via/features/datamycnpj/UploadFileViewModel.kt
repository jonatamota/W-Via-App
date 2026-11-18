package com.w_via.features.datamycnpj

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.w_via.features.authentication.AuthManager
import com.w_via.features.authentication.AuthSessionStore
import com.w_via.features.authentication.data.RegistrationRepository
import com.w_via.features.company.data.EmpresaEntity
import com.w_via.features.company.data.EmpresaRepository
import com.w_via.features.utils.PhoneFormatter
import com.w_via.opencnpj.CNPJValidator
import com.w_via.opencnpj.OpenCNPJClient
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI
import androidx.annotation.RequiresApi
import com.w_via.BuildConfig
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@OptIn(FlowPreview::class)
class UploadFileViewModel(
    application: Application
) : AndroidViewModel(application), KoinComponent {

    private val registrationRepository: RegistrationRepository by inject()
    private val authSessionStore: AuthSessionStore by inject()
    private val empresaRepository: EmpresaRepository by inject()

    private val context = application.applicationContext
    private val dispatcher = kotlinx.coroutines.Dispatchers.IO

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun handleFileSelection(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val fileInfo = getFileInfo(uri)
                if (fileInfo != null) {
                    val fileType = getFileType(uri)

                    // Validate file type (only JPEG and PNG allowed)
                    if (fileType != "image/jpeg" && fileType != "image/png") {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Apenas arquivos JPEG ou PNG são permitidos"
                            )
                        }
                        return@launch
                    }

                    // Validate file size (max 1MB)
                    val maxSize = 1 * 1024 * 1024 // 1MB in bytes
                    if (fileInfo.second > maxSize) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "O arquivo deve ter no máximo 1MB"
                            )
                        }
                        return@launch
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedFileUri = uri,
                            fileName = fileInfo.first,
                            fileSize = fileInfo.second,
                            fileType = fileType
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Arquivo inválido ou não suportado"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Erro ao processar arquivo: ${e.message}"
                    )
                }
            }
        }
    }

    fun saveFileToDevice(uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveFileToDeviceModern(uri)
        } else {
            saveFileToDeviceLegacy(uri)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveFileToDeviceModern(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            _uploadProgress.value = 0f

            try {
                val savedUri = saveToMediaStore(uri)
                val downloadUri = saveToDownloads(uri)

                _uiState.update {
                    it.copy(isSaving = false, savedUri = savedUri, downloadUri = downloadUri, isSaved = true)
                }
                savedUri?.let { LogoStorage.save(context, it) }
                _uploadProgress.value = 1f

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = "Erro ao salvar arquivo: ${e.message}")
                }
                _uploadProgress.value = 0f
            }
        }
    }

    private fun saveFileToDeviceLegacy(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            _uploadProgress.value = 0f

            try {
                val savedUri = saveToDownloadsLegacy(uri)

                _uiState.update {
                    it.copy(isSaving = false, savedUri = savedUri, isSaved = true)
                }
                savedUri?.let { LogoStorage.save(context, it) }
                _uploadProgress.value = 1f

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = "Erro ao salvar arquivo: ${e.message}")
                }
                _uploadProgress.value = 0f
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun saveToMediaStore(sourceUri: Uri): Uri? {
        return withContext(dispatcher) {
            try {
                val fileName = _uiState.value.fileName
                    ?: "file_${System.currentTimeMillis()}"
                val mimeType = _uiState.value.fileType
                    ?: context.contentResolver.getType(sourceUri)
                    ?: "image/jpeg"

                // Tentar recortar a imagem; se falhar, usa o stream original
                val croppedBitmap = cropImageToBoxSize(sourceUri)
                val finalInputStream = if (croppedBitmap != null) {
                    bitmapToInputStream(croppedBitmap, mimeType)
                } else {
                    context.contentResolver.openInputStream(sourceUri)
                } ?: return@withContext null

                // Verificar se é imagem ou outro tipo
                val collection = if (mimeType.startsWith("image/")) {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if (mimeType.startsWith("video/")) {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else {
                    EXTERNAL_CONTENT_URI
                }

                // Salvar no MediaStore
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/WVia"
                    )
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val uri = context.contentResolver.insert(collection, contentValues)
                uri?.let { mediaUri ->
                    context.contentResolver.openOutputStream(mediaUri)?.use { outputStream ->
                        finalInputStream.use { it.copyTo(outputStream, bufferSize = 8192) }
                    }
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    context.contentResolver.update(mediaUri, contentValues, null, null)
                }
                uri

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun cropImageToBoxSize(uri: Uri): Bitmap? {
        return try {
            val maxDimension = 1600 // teto razoável, evita decodificar 4000px+ à toa

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            var inSampleSize = 1
            while (options.outWidth / inSampleSize > maxDimension ||
                options.outHeight / inSampleSize > maxDimension
            ) {
                inSampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun bitmapToInputStream(bitmap: Bitmap, mimeType: String): java.io.InputStream? {
        return try {
            val maxBytes = 1 * 1024 * 1024 // 1MB
            // PNG é lossless — quality é ignorado pelo Android, então força JPEG se precisar comprimir
            val format = if (mimeType == "image/png") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG

            var quality = 90
            var outputStream = java.io.ByteArrayOutputStream()
            bitmap.compress(format, quality, outputStream)

            // Se for PNG e passar do limite, reconverte pra JPEG (única forma de reduzir de fato)
            var effectiveFormat = format
            if (format == Bitmap.CompressFormat.PNG && outputStream.size() > maxBytes) {
                effectiveFormat = Bitmap.CompressFormat.JPEG
                outputStream = java.io.ByteArrayOutputStream()
                bitmap.compress(effectiveFormat, quality, outputStream)
            }

            while (outputStream.size() > maxBytes && quality > 10) {
                quality -= 10
                outputStream = java.io.ByteArrayOutputStream()
                bitmap.compress(effectiveFormat, quality, outputStream)
            }

            java.io.ByteArrayInputStream(outputStream.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun saveToDownloads(sourceUri: Uri): Uri? {
        return withContext(dispatcher) {
            try {
                val fileName = _uiState.value.fileName
                    ?: "file_${System.currentTimeMillis()}"
                val mimeType = _uiState.value.fileType
                    ?: context.contentResolver.getType(sourceUri)
                    ?: "image/jpeg"

                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(
                        MediaStore.Downloads.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS + "/WVia"
                    )
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val uri =
                    resolver.insert(EXTERNAL_CONTENT_URI, contentValues)

                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        resolver.openInputStream(sourceUri)?.use { inputStream ->
                            inputStream.copyTo(outputStream, bufferSize = 8192)
                        }
                    }

                    contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(it, contentValues, null, null)
                }

                uri

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun saveToDownloadsLegacy(sourceUri: Uri): Uri? {
        return withContext(dispatcher) {
            try {
                val fileName = _uiState.value.fileName
                    ?: "file_${System.currentTimeMillis()}"
                val mimeType = _uiState.value.fileType
                    ?: context.contentResolver.getType(sourceUri)
                    ?: "image/jpeg"

                // Usar diretório de Downloads público para Android < Q
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val wviaDir = java.io.File(downloadsDir, "WVia")
                if (!wviaDir.exists()) {
                    wviaDir.mkdirs()
                }

                val outputFile = java.io.File(wviaDir, fileName)

                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    java.io.FileOutputStream(outputFile).use { outputStream ->
                        inputStream.copyTo(outputStream, bufferSize = 8192)
                    }
                }

                // Retornar Uri do arquivo salvo
                android.net.Uri.fromFile(outputFile)

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun getFileInfo(uri: Uri): Pair<String, Long>? {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndex(MediaStore.MediaColumns.SIZE)

                    val name =
                        if (nameIndex != -1) it.getString(nameIndex) else "file_${System.currentTimeMillis()}"
                    val size = if (sizeIndex != -1) it.getLong(sizeIndex) else 0L

                    Pair(name, size)
                } else {
                    null
                }
            } ?: run {
                // Fallback para Uri sem cursor
                val name = uri.lastPathSegment ?: "file_${System.currentTimeMillis()}"
                Pair(name, 0L)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileType(uri: Uri): String? {
        return try {
            context.contentResolver.getType(uri)
        } catch (e: Exception) {
            null
        }
    }

    fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> String.format("%.1f KB", size / 1024.0)
            size < 1024 * 1024 * 1024 -> String.format("%.1f MB", size / (1024.0 * 1024.0))
            else -> String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0))
        }
    }

    fun clearSelection() {
        _uiState.update { UploadUiState() }
        _uploadProgress.value = 0f
    }

    /**
     * Fluxo do botão "Salvar imagem": sempre guarda uma cópia local no
     * dispositivo e envia a imagem para o Supabase mostrando progresso.
     *
     * Em caso de sucesso marca [UploadUiState.isSaved] (navega p/ CNPJ) e o
     * [UploadUiState.logoPath]. Em caso de falha a cópia local é mantida, mas
     * NÃO marca `isSaved`/`logoPath` — a etapa profissional não fica completa e
     * o usuário deverá enviar novamente no próximo login.
     */
    fun salvarImagem(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            _snackbarMessage.value = null
            _uploadProgress.value = 0f
            try {
                // 1) Cópia local permanente (permanece mesmo se o upload falhar).
                val localUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveToMediaStore(uri)
                } else {
                    saveToDownloadsLegacy(uri)
                }
                localUri?.let { LogoStorage.save(context, it) }

                // 2) Upload para o Supabase (animando o progresso).
                val mimeType = _uiState.value.fileType ?: "image/jpeg"
                val uploadJob = viewModelScope.async {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: throw IllegalStateException("Não foi possível ler o arquivo selecionado")
                    registrationRepository.uploadCurrentUserLogo(bytes, mimeType)
                }
                var progress = 0.1f
                while (progress < 0.9f && !uploadJob.isCompleted) {
                    _uploadProgress.value = progress
                    progress = (progress + 0.1f).coerceAtMost(0.9f)
                    delay(60)
                }
                val path = uploadJob.await()

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        savedUri = localUri,
                        isSaved = true,
                        logoPath = path
                    )
                }
                _uploadProgress.value = 1f
            } catch (e: Exception) {
                // Falha: mantém a cópia local salva, mas NÃO marca a etapa completa.
                _uploadProgress.value = 0f
                _uiState.update { it.copy(isSaving = false) }
                _snackbarMessage.value =
                    "Não foi possível enviar a imagem. Ela ficou salva somente neste " +
                        "dispositivo. Você deverá enviá-la novamente no próximo login."
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun updateCnpj(value: String) {
        _uiState.update { it.copy(cnpjNumber = value) }
    }

    fun updateNomeContratante(value: String) {
        _uiState.update { it.copy(nomeContratante = value) }
    }

    fun updateNomeFantasia(value: String) {
        _uiState.update { it.copy(nomeFantasia = value) }
    }

    fun updateEndereco(value: String) {
        _uiState.update { it.copy(endereco = value) }
    }

    fun updateTelefone(value: String) {
        _uiState.update { it.copy(telefone = value) }
    }

    fun updateEmail(value: String) {
        _uiState.update { it.copy(email = value) }
    }

    /** Busca os dados da empresa pelo CNPJ preenchendo Nome Fantasia/Telefone/Email. */
    fun searchCnpj() {
        val cnpj = _uiState.value.cnpjNumber

        if (!isValidCnpj(cnpj)) {
            _snackbarMessage.value = "CNPJ inválido. Verifique e tente novamente."
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            _snackbarMessage.value = null
            try {
                val result = OpenCNPJClient.getInstance().getCompanyInfo(cnpj)
                if (result.isSuccess) {
                    val info = result.getOrNull()
                    val fullAddress = info?.getFullAddress()?.takeIf { it.isNotBlank() }
                    val (cidade, uf) = info?.let { c ->
                        c.city.takeIf { !it.isNullOrBlank() } to c.state.takeIf { !it.isNullOrBlank() }
                    }?.let { (cidadeApi, ufApi) ->
                        // Se a API não trouxer cidade/UF, extrai do endereço completo.
                        if (cidadeApi != null || ufApi != null) {
                            (cidadeApi ?: "") to (ufApi ?: "")
                        } else {
                            extrairCidadeUf(fullAddress.orEmpty())
                        }
                    } ?: extrairCidadeUf(fullAddress.orEmpty())
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            nomeFantasia = info?.tradeName ?: info?.corporateName ?: it.nomeFantasia,
                            telefone = info?.let { c ->
                                PhoneFormatter.format(c.phone ?: "")
                            } ?: it.telefone,
                            email = info?.email ?: it.email,
                            endereco = fullAddress ?: it.endereco,
                            cidade = cidade,
                            uf = uf
                        )
                    }
                } else {
                    val err = result.exceptionOrNull()
                    _uiState.update { it.copy(isLoading = false) }
                    _snackbarMessage.value = when {
                        isNetworkException(err) ->
                            "Sem conexão ou internet lenta. Verifique sua conexão e tente novamente."
                        err is IllegalArgumentException ->
                            "CNPJ inválido. Verifique e tente novamente."
                        else ->
                            "Não foi possível buscar pelo CNPJ. Tente novamente."
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _snackbarMessage.value =
                    if (isNetworkException(e)) {
                        "Sem conexão ou internet lenta. Verifique sua conexão e tente novamente."
                    } else {
                        "Não foi possível buscar pelo CNPJ. Tente novamente."
                    }
            }
        }
    }

    /** True quando CNPJ é válido (numérico ou alfanumérico). */
    fun isValidCnpj(cnpj: String): Boolean = CNPJValidator.isValid(cnpj)

    /**
     * Extrai "Cidade" e "UF" do fim de um endereço completo (ex.:
     * "Rua X, 123 - Bairro - Cidade/UF - CEP: 00000-000"). Usado como fallback
     * quando a API de CNPJ não retorna o município/UF separadamente.
     */
    private fun extrairCidadeUf(endereco: String): Pair<String, String> {
        if (endereco.isBlank()) return "" to ""
        val antesCep = endereco.substringBefore(" - CEP:").trim()
        val parte = antesCep.substringAfterLast(" - ", antesCep)
        val barra = parte.lastIndexOf('/')
        return if (barra in 1 until parte.length - 1) {
            parte.substring(0, barra).trim() to parte.substring(barra + 1).trim().uppercase()
        } else {
            parte.trim() to ""
        }
    }

    /** True quando o telefone tem DDD + 9 dígitos (celular) ou DDD + 8 (fixo). */
    fun isValidPhone(phone: String): Boolean = PhoneFormatter.isValid(phone)

    /** Validação simples de e-mail. */
    fun isValidEmail(email: String): Boolean =
        email.isNotBlank() && Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(email)

    /** Faz o upload da logo selecionada para o Storage e guarda o caminho no estado. */
    fun uploadLogo() {
        val uri = _uiState.value.selectedFileUri ?: return
        val mimeType = _uiState.value.fileType ?: "image/jpeg"
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val bytes = withContext(dispatcher) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                } ?: throw IllegalStateException("Não foi possível ler o arquivo selecionado")
                val path = registrationRepository.uploadCurrentUserLogo(bytes, mimeType)
                _uiState.update { it.copy(isSaving = false, logoPath = path, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = "Erro ao enviar logo: ${e.message}")
                }
            }
        }
    }

    /**
     * Fluxo do botão "Continuar" na tela de CNPJ: envia a logo (se houver) e persiste
     * os dados profissionais no Supabase. Ao final marca [UploadUiState.professionalSaved].
     */
    fun saveAndComplete() {
        viewModelScope.launch {
            val state = _uiState.value

            // Validação dos campos obrigatórios antes de salvar.
            val invalidMessage: String? = when {
                !isValidCnpj(state.cnpjNumber) ->
                    "Informe um CNPJ válido (numérico ou alfanumérico)."
                state.nomeFantasia.isBlank() ->
                    "Informe o nome fantasia."
                !isValidPhone(state.telefone) ->
                    "Informe um telefone válido com DDD."
                !isValidEmail(state.email) ->
                    "Informe um e-mail válido."
                else -> null
            }
            if (invalidMessage != null) {
                _snackbarMessage.value = invalidMessage
                return@launch
            }

            _uiState.update { it.copy(isSavingProfData = true, error = null) }
            try {
                // Reutiliza a logo já enviada (tela de imagem) se existir; caso
                // contrário envia a selecionada.
                val selectedUri = _uiState.value.selectedFileUri
                val existingLogoPath = _uiState.value.logoPath
                val logoPath = if (existingLogoPath.isNullOrBlank() && selectedUri != null) {
                    val mimeType = _uiState.value.fileType ?: "image/jpeg"
                    val bytes = withContext(dispatcher) {
                        context.contentResolver.openInputStream(selectedUri)?.use { it.readBytes() }
                    } ?: throw IllegalStateException("Não foi possível ler o arquivo selecionado")
                    registrationRepository.uploadCurrentUserLogo(bytes, mimeType)
                } else {
                    existingLogoPath
                }

                registrationRepository.saveProfessionalData(
                    cnpj = _uiState.value.cnpjNumber,
                    tradeName = _uiState.value.nomeFantasia,
                    logoUrl = logoPath,
                    phonePro = _uiState.value.telefone.filter { it.isDigit() }.toLongOrNull(),
                    emailPro = _uiState.value.email,
                    address = _uiState.value.endereco,
                    city = _uiState.value.cidade,
                    state = _uiState.value.uf
                )
                // Atualiza o cache local (Room) para a tela de perfil refletir
                // imediatamente, sem depender do sync pós-login.
                val localLogoPath = _uiState.value.savedUri
                    ?.takeIf { logoPath != null }
                    ?.let { saved ->
                        runCatching {
                            context.contentResolver.openInputStream(saved)?.use { it.readBytes() }
                        }.getOrNull()?.let { bytes ->
                            java.io.File(context.filesDir, "logo_prof_local.png")
                                .apply { writeBytes(bytes) }
                                .absolutePath
                        }
                    }
                empresaRepository.save(
                    EmpresaEntity(
                        userId = AuthManager.getCurrentUserId()
                            ?: "local",
                        cnpj = _uiState.value.cnpjNumber,
                        nomeFantasia = _uiState.value.nomeFantasia,
                        telefone = _uiState.value.telefone,
                        email = _uiState.value.email,
                        endereco = _uiState.value.endereco,
                        cidade = _uiState.value.cidade,
                        uf = _uiState.value.uf,
                        logoPath = localLogoPath
                    )
                )
                _uiState.update {
                    it.copy(
                        isSavingProfData = false,
                        logoPath = logoPath,
                        professionalSaved = true
                    )
                }
                // Cadastro 100% completo localmente: agiliza a navegação na próxima abertura.
                authSessionStore.updateRegistration(
                    personalComplete = true,
                    professionalComplete = true
                )
            } catch (e: Exception) {
                val detalhe = "Erro ao salvar dados profissionais: ${e.message}"
                // No debug mostra o erro técnico; na release só uma mensagem amigável.
                val mensagem = if (BuildConfig.DEBUG) {
                    detalhe
                } else {
                    "Não foi possível salvar seus dados. Verifique sua conexão e tente novamente."
                }
                _uiState.update {
                    it.copy(
                        isSavingProfData = false,
                        error = detalhe
                    )
                }
                // Sem essa linha a falha é silenciosa: o spinner some e nada acontece.
                _snackbarMessage.value = mensagem
            }
        }
    }
}

// UploadUiState.kt
data class UploadUiState(
    val selectedFileUri: Uri? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val fileType: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val savedUri: Uri? = null,
    val downloadUri: Uri? = null,
    val error: String? = null,
    // Dados do CNPJ (preenchidos na MyCnpjScreen)
    val cnpjNumber: String = "",
    val nomeContratante: String = "",
    val nomeFantasia: String = "",
    val telefone: String = "",
    val email: String = "",
    val endereco: String = "",
    val cidade: String = "",
    val uf: String = "",
    val isSavingProfData: Boolean = false,
    val logoPath: String? = null,
    val professionalSaved: Boolean = false,
)

// FileUtils.kt - Utilitários adicionais
private val networkExceptions = setOf(
    java.net.SocketTimeoutException::class,
    java.net.UnknownHostException::class,
    java.net.ConnectException::class
)

private fun isNetworkException(e: Throwable?): Boolean {
    if (e == null) return false
    if (networkExceptions.contains(e::class)) return true
    if (e is java.io.IOException) return e.cause?.let { isNetworkException(it) } == true
    return false
}
object FileUtils {
    fun getMimeType(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.getType(uri)
        } catch (e: Exception) {
            null
        }
    }

    fun isImageFile(mimeType: String?): Boolean {
        return mimeType?.startsWith("image/") == true
    }

    fun isVideoFile(mimeType: String?): Boolean {
        return mimeType?.startsWith("video/") == true
    }

    fun isDocumentFile(mimeType: String?): Boolean {
        return mimeType?.let {
            it.startsWith("application/") || it.startsWith("text/") || it.startsWith("pdf/")
        } == true
    }

    fun getFileExtension(fileName: String?): String {
        return fileName?.substringAfterLast('.', "") ?: ""
    }

    fun generateUniqueFileName(originalName: String?): String {
        val timestamp = System.currentTimeMillis()
        val extension = originalName?.substringAfterLast('.', "jpg") ?: "jpg"
        val name = originalName?.substringBeforeLast('.') ?: "file"
        return "${name}_${timestamp}.$extension"
    }
}
