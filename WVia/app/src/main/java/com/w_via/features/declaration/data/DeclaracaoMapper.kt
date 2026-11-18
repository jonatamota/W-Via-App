package com.w_via.features.declaration.data

import com.w_via.features.declaration.DeclaracaoObito
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * Conversão entre o modelo de domínio ([DeclaracaoObito]) e a entidade persistida.
 * O payload é serializado com kotlinx.serialization (o plugin já está aplicado e o
 * domínio é anotado com @Serializable).
 */
object DeclaracaoMapper {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /** Cria a entidade a partir do modelo de domínio (para salvar). */
    fun toEntity(
        userId: String,
        declaracao: DeclaracaoObito,
        now: Long = System.currentTimeMillis(),
        id: Long = 0,
        synced: Boolean = false,
        isDeleted: Boolean = false
    ): DeclaracaoEntity {
        val declaracaoWithUuid = declaracao.withUuid()
        val falecido = declaracaoWithUuid.falecido
        val obito = declaracaoWithUuid.obito
        return DeclaracaoEntity(
            id = id,
            userId = userId,
            uuid = declaracaoWithUuid.uuid,
            nome = falecido.nome,
            dataObito = obito.dataObito,
            localObito = obito.nomeLocal.ifBlank { obito.tipoLocal },
            payloadJson = toJson(declaracaoWithUuid),
            createdAt = now,
            updatedAt = now,
            synced = synced,
            isDeleted = isDeleted
        )
    }

    /** Reconstrói o modelo de domínio a partir da entidade (para exibir/gerar PDF). */
    fun fromEntity(entity: DeclaracaoEntity): DeclaracaoObito = fromJson(entity.payloadJson)

    fun toJson(declaracao: DeclaracaoObito): String = json.encodeToString(declaracao)

    fun fromJson(payload: String): DeclaracaoObito = json.decodeFromString(payload)

    /** Garante um [uuid] preenchido (estável) para a sincronização. */
    fun DeclaracaoObito.withUuid(): DeclaracaoObito =
        if (uuid.isBlank()) copy(uuid = UUID.randomUUID().toString()) else this
}
