package com.w_via.features.declaration

import kotlinx.serialization.Serializable

@Serializable
data class DadosObito(
    val tipoLocal: String,
    val nomeLocal: String,
    val cidade: String,
    val uf: String,
    val dataObito: String,
    val horaFalecimento: String,
    val numeroAtestadoObito: String,
    val causaMorte: String,
    val cemiterio: String,
    val horaSepultamento: String,
    val localVelorio: String,
    val horaVelorio: String,
    val endereco: String,
    val numero: String,
    val bairro: String,
    val cidadeVelorio: String,
    val cep: String,
    val uf2: String,
    val medico1: String,
    val crm1: String,
    val ufMedico1: String,
    val medico2: String? = null,
    val crm2: String? = null,
    val ufMedico2: String? = null
)

@Serializable
data class Filho(
    val nome: String
)

@Serializable
data class Declarante(
    val cartorio: String,
    val telefone: String,
    val endereco: String,
    val nomeDeclarante: String,
    val grauParentesco: String,
    val profissao: String,
    val rg: String,
    val cpf: String,
    val residencia: String,
    val bairro: String,
    val numero: String = "",
    val municipio: String,
    val cep: String,
    val uf: String,
    val celular: String,
    val email: String,
    val enderecoCartorio: String = "",
    val numeroCartorio: String = "",
    val ufCartorio: String = "",
    val cidadeCartorio: String = "",
    val emailCartorio: String = "",
    val oficialCartorio: String = ""
)

@Serializable
data class DeclaracaoObito(
    val falecido: DadosFalecido,
    val obito: DadosObito,
    val filhos: List<Filho> = emptyList(),
    val observacoes: String = "Informações passadas pela declarante",
    val declarante: Declarante,
    /** Identificador estável usado na sincronização local ↔ remota. */
    val uuid: String = "",
    /** "Cidade/UF" da funerária, estampado no cabeçalho junto da data de emissão. */
    val cidadeEstado: String = "",
    /** Data de emissão por extenso (ex.: "11 de agosto de 2026"), no cabeçalho. */
    val dataEmissaoPorExtenso: String = "",
    /** Endereço completo da funerária (rodapé, 1ª linha). */
    val enderecoFuneraria: String = "",
    /** Telefone profissional + "Atendimento 24h" (rodapé, 2ª linha). */
    val telefoneFuneraria: String = ""
)