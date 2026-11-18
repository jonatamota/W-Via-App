package com.w_via.features.receipt

import android.net.Uri
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/** Serializador tolerante: aceita String ou Number/null no JSON, retorna String. */
object StringOrNumberSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("string_or_number", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }

    override fun deserialize(decoder: Decoder): String {
        val element: JsonElement = (decoder as JsonDecoder).decodeJsonElement()
        return when {
            element is JsonNull -> ""
            element is JsonPrimitive -> element.content
            else -> ""
        }
    }
}

@Serializable
data class Recibo(
    // IDENTIFICADOR ÚNICO
    val uuid: String = "",
    // NUMERAÇÃO CRESCENTE EXIBIDA NO CABEÇALHO
    val numero: String = "",

    // DADOS DO CONTRATANTE
    val nomeContratante: String = "",
    @Serializable(with = StringOrNumberSerializer::class)
    val rg_ie: String = "",
    @Serializable(with = StringOrNumberSerializer::class)
    val cpf_cnpj: String = "",
    val endereco_contratante: String = "",
    val tel: String = "",
    val email: String = "",
    @Contextual val logoUri: Uri? = null,

    // DADOS DO FALECIDO
    val nome_falecido: String = "",
    val estado_civil_falecido: String = "",
    val idade_falecido: String = "",
    val sexo_falecido: String = "",
    val naturalidade_falecido: String = "",
    val uf_falecido: String = "",
    val rg_falecido: String = "",
    val cpf_falecido: String = "",
    val filiacao_falecido: String = "",
    val pai_falecido: String = "",
    val mae_falecido: String = "",
    val local_funeral: String = "",
    val hora_saida_funeral: String = "",
    val dia_funeral: String = "",
    val cemiterio_sepultamento: String = "",

    // DADOS DO SERVIÇO
    val quantidade: String = "",
    val descricao: String = "",
    val unidade: String = "",
    val valor: String = "",

    // DADOS DE PAGAMENTO TOTAL
    val total_pago: String = "",
    val valor_por_extenso: String = "",
    val data_pagamento: String = "",

    // DADOS DE PAGAMENTO POR CHEQUE
    val ass_contratante1: String = "",
    val ass_contratante2: String = "",
    val ass_contratante3: String = "",
    val n_cheque: String = "",
    val valor_cheque: String = "",
    val banco: String = "",
    val agencia: String = "",
    val rg_contratante: String = "",
    val emitente_cheque: String = "",

    // DADOS DO AGENTE (funerária) EXIBIDOS NO CABEÇALHO
    val endereco_agente: String = "",
    val cnpj_agente: String = ""
)
