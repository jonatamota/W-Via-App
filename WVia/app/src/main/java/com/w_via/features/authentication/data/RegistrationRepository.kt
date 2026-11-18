package com.w_via.features.authentication.data

import com.w_via.features.authentication.AuthManager
import com.w_via.features.authentication.data.model.ProfessionalDataDto
import com.w_via.features.authentication.data.model.UserRowDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.core.component.KoinComponent

/**
 * Escreve os dados de cadastro (pessoais e profissionais) diretamente no Supabase
 * via Postgrest + RLS.
 *
 * Usa UPSERT (com onConflict = "user_id"): as linhas NÃO são criadas pelo trigger
 * do auth, então o upsert cria na primeira execução e atualiza nas seguintes.
 * Os índices únicos em `user_id` são criados pelo script SQL entregue.
 */
class RegistrationRepository : KoinComponent {

    private val client get() = AuthManager.getClient()

    /** UUID do usuário (auth.users) registrado no `public.users`. */
    private fun currentUserId(): String =
        AuthManager.getCurrentUserId()
            ?: error("Usuário não autenticado ao persistir dados de cadastro")

    /** BigInt (chave primária de `public.users`) usado como FK nas demais tabelas. */
    suspend fun resolveUserId(): Long = withContext(Dispatchers.IO) {
        client.postgrest["users"]
            .select { filter { eq("user_id", currentUserId()) } }
            .decodeSingle<UserRowDto>().id
    }

    suspend fun savePersonalData(birthday: String, phone: String) {
        val userId = resolveUserId()
        withContext(Dispatchers.IO) {
            client.postgrest["personal_data"].upsert(
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put("user_id", userId)
                            put("birthday", birthday)
                            // No Supabase `phone` é BIGINT; o app mantém a String formatada
                            // apenas para exibição/PDF, convertendo aqui na gravação.
                            put("phone", sanitizePhoneToLong(phone))
                        }
                    )
                }
            ) {
                onConflict = "user_id"
            }
        }
        markPersonalComplete(userId)
    }

    suspend fun saveProfessionalData(
        cnpj: String,
        tradeName: String,
        logoUrl: String?,
        phonePro: Long? = null,
        emailPro: String? = null,
        address: String = "",
        city: String = "",
        state: String = ""
    ) {
        val userId = resolveUserId()
        withContext(Dispatchers.IO) {
            client.postgrest["professional_data"].upsert(
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put("user_id", userId)
                            put("cnpj", cnpj)
                            put("company_name", tradeName)
                            put("image_logo", logoUrl)
                            phonePro?.let { put("phone_prof", it) }
                            emailPro?.let { put("email_prof", it) }
                            put("address", address)
                            put("city", city)
                            put("state", state)
                        }
                    )
                }
            ) {
                onConflict = "user_id"
            }
        }
        markProfessionalComplete(userId)
    }

    suspend fun uploadLogo(userId: String, bytes: ByteArray, mimeType: String): String {
        val path = "image_logo/$userId/logo_prof"
        withContext(Dispatchers.IO) {
            client.storage.from("W-Via-images")
                .upload(path, bytes) {
                    upsert = true
                    contentType = ContentType.parse(mimeType)
                }
        }
        return path
    }

    /** Faz o upload da logo e já retorna o caminho armazenado (UUID do auth na pasta). */
    suspend fun uploadCurrentUserLogo(bytes: ByteArray, mimeType: String): String =
        uploadLogo(currentUserId(), bytes, mimeType)

    suspend fun downloadLogo(userId: String): ByteArray = withContext(Dispatchers.IO) {
        client.storage.from("W-Via-images")
            .downloadAuthenticated("image_logo/$userId/logo_prof")
    }

    /** Busca os dados profissionais (logo, nome, CNPJ, telefone, e-mail) do usuário. */
    suspend fun fetchProfessionalData(): ProfessionalDataDto? = withContext(Dispatchers.IO) {
        val userId = resolveUserId()
        android.util.Log.d("RegistrationRepo", "fetchProfessionalData resolveUserId=$userId")
        runCatching {
            client.postgrest["professional_data"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<ProfessionalDataDto>()
                .maxByOrNull { it.user_id }
        }.onSuccess { android.util.Log.d("RegistrationRepo", "fetchProfessionalData OK -> $it") }
            .onFailure { android.util.Log.e("RegistrationRepo", "fetchProfessionalData falhou", it) }
            .getOrNull()
    }

    /** Atualiza `personal_complete` em `status_registration` (upsert, 1 linha/usuário). */
    private suspend fun markPersonalComplete(userId: Long) {
        withContext(Dispatchers.IO) {
            client.postgrest["status_registration"].upsert(
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put("user_id", userId)
                            put("personal_complete", true)
                        }
                    )
                }
            ) {
                onConflict = "user_id"
            }
        }
    }

    /** Atualiza `professional_complete` em `status_registration` (upsert, 1 linha/usuário). */
    private suspend fun markProfessionalComplete(userId: Long) {
        withContext(Dispatchers.IO) {
            client.postgrest["status_registration"].upsert(
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put("user_id", userId)
                            put("professional_complete", true)
                        }
                    )
                }
            ) {
                onConflict = "user_id"
            }
        }
    }

    /** Converte o telefone formatado (ex.: "(11) 99999-8888") para Long, removendo não-dígitos. */
    private fun sanitizePhoneToLong(phone: String): Long? =
        phone.filter { it.isDigit() }.toLongOrNull()
}
