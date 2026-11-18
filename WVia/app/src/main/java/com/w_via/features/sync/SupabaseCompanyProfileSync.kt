package com.w_via.features.sync

import android.content.Context
import android.net.Uri
import com.w_via.features.authentication.AuthManager
import com.w_via.features.authentication.data.RegistrationRepository
import com.w_via.features.authentication.data.model.ProfessionalDataDto
import com.w_via.features.company.data.EmpresaEntity
import com.w_via.features.company.data.EmpresaRepository
import com.w_via.features.datamycnpj.LogoStorage

/**
 * Baixa do Supabase os dados profissionais da empresa (logo, nome, CNPJ,
 * telefone e e-mail) e os guarda no cache local (Room + LogoStorage).
 *
 * A logo é baixada do Storage e salva em arquivo interno, cuja URI alimenta o
 * [LogoStorage] para que recibos/PDFs usem a imagem oficial.
 */
class SupabaseCompanyProfileSync(
    private val context: Context,
    private val registrationRepository: RegistrationRepository,
    private val empresaRepository: EmpresaRepository
) : CompanyProfileSync {

    override suspend fun sync(): SyncOutcome {
        val userId = AuthManager.getCurrentUserId() ?: return SyncOutcome.Success
        return try {
            val data: ProfessionalDataDto? = registrationRepository.fetchProfessionalData()
            android.util.Log.d("CompanySync", "fetchProfessionalData userId=$userId -> $data")
            if (data == null) return SyncOutcome.Success

            // Preserva valores locais quando o remoto vier vazio (ex.: campo
            // adicionado depois do cadastro ou push incompleto), evitando que o
            // upsert apague endereço/telefone/e-mail já preenchidos no app.
            val local = empresaRepository.get()

            val logoPath = data.image_logo
                ?.let { path -> downloadLogoToFile(userId, path) }

            val empresa = EmpresaEntity(
                userId = userId,
                cnpj = data.cnpj.ifBlank { local?.cnpj.orEmpty() },
                nomeFantasia = data.company_name.ifBlank { local?.nomeFantasia.orEmpty() },
                telefone = data.phone_prof?.toString().orEmpty().ifBlank { local?.telefone.orEmpty() },
                email = data.email_prof.orEmpty().ifBlank { local?.email.orEmpty() },
                endereco = data.address.orEmpty().ifBlank { local?.endereco.orEmpty() },
                cidade = data.city.orEmpty().ifBlank { local?.cidade.orEmpty() },
                uf = data.state.orEmpty().ifBlank { local?.uf.orEmpty() },
                logoPath = logoPath ?: local?.logoPath,
                updatedAt = System.currentTimeMillis()
            )
            empresaRepository.save(empresa)
            logoPath?.let { LogoStorage.save(context, Uri.fromFile(java.io.File(it))) }
            SyncOutcome.Success
        } catch (e: Exception) {
            SyncOutcome.Failure(e.message ?: "Erro ao sincronizar dados da empresa")
        }
    }

    /** Baixa a logo do Storage e salva em arquivo interno; retorna o caminho. */
    private suspend fun downloadLogoToFile(userId: String, path: String): String? {
        return try {
            val bytes = registrationRepository.downloadLogo(userId)
            val ext = path.substringAfterLast('.', "png")
            val file = java.io.File(context.filesDir, "logo_prof_$userId.$ext")
            file.writeBytes(bytes)
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
