package com.w_via.features.receipt.data

import android.net.Uri
import com.w_via.features.receipt.Recibo
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import androidx.core.net.toUri

/**
 * Serializador contextual para [Uri], que kotlinx.serialization não conhece
 * nativamente. O valor é persistido como a string do Uri.
 */
object UriSerializer : KSerializer<Uri> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("android.net.Uri", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Uri) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Uri = decoder.decodeString().toUri()
}

/**
 * Conversão entre o modelo de domínio ([Recibo]) e a entidade persistida.
 * O payload é serializado com kotlinx.serialization; o logo (Uri) usa o
 * [UriSerializer] registrado no módulo contextual.
 */
object ReciboMapper {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        serializersModule = SerializersModule {
            contextual(UriSerializer)
        }
    }

    /** Cria a entidade a partir do modelo de domínio (para salvar). */
    fun toEntity(
        userId: String,
        recibo: Recibo,
        now: Long = System.currentTimeMillis(),
        id: Long = 0,
        synced: Boolean = false,
        isDeleted: Boolean = false,
        isObsolete: Boolean = false
    ): ReciboEntity {
        val total = recibo.total_pago.trim().toDoubleOrNull()
            ?.takeIf { it != 0.0 }
            ?.let { "R$ %.2f".format(it) }
            ?: ""
        return ReciboEntity(
            id = id,
            userId = userId,
            uuid = recibo.uuid,
            nomeContratante = recibo.nomeContratante,
            dataPagamento = recibo.data_pagamento,
            totalPago = total,
            payloadJson = toJson(recibo),
            createdAt = now,
            updatedAt = now,
            synced = synced,
            isDeleted = isDeleted,
            isObsolete = isObsolete
        )
    }

    /** Reconstrói o modelo de domínio a partir da entidade (para exibir/gerar PDF). */
    fun fromEntity(entity: ReciboEntity): Recibo {
        val recibo = fromJson(entity.payloadJson)
        return if (recibo.uuid.isBlank()) recibo.copy(uuid = entity.uuid) else recibo
    }

    fun toJson(recibo: Recibo): String = json.encodeToString(recibo)

    /**
     * Decodifica o payload, tolerando dados legados em que campos String foram
     * gravados como Number (ex.: `"rg_ie":0`). Números são convertidos para String.
     */
    fun fromJson(payload: String): Recibo {
        val element = json.parseToJsonElement(payload)
        return json.decodeFromJsonElement(normalizeNumbers(element))
    }

    private fun normalizeNumbers(element: JsonElement): JsonElement = when (element) {
        is JsonNull -> element
        is JsonPrimitive -> if (element.isString) element else JsonPrimitive(element.content)
        is JsonObject -> JsonObject(element.mapValues { normalizeNumbers(it.value) })
        else -> element
    }
}
