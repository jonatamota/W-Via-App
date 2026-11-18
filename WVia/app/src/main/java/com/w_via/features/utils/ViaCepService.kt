package com.w_via.features.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Resposta bruta da API ViaCEP (https://viacep.com.br). */
@Serializable
internal data class ViaCepResponse(
    val cep: String? = null,
    val logradouro: String? = null,
    val complemento: String? = null,
    val bairro: String? = null,
    val localidade: String? = null,
    val uf: String? = null,
    val erro: Boolean? = null
)

/** Endereço resolvido a partir de um CEP, com os campos usados pela declaração. */
data class EnderecoViaCep(
    val logradouro: String,
    val bairro: String,
    val localidade: String,
    val uf: String
)

/**
 * Consulta de CEP na API pública ViaCEP usando Ktor.
 *
 * A busca acontece em [Dispatchers.IO]; a decodificação da resposta fica em
 * [parse], isolada para ser testável em JVM sem cliente HTTP.
 */
object ViaCepService {

    private val json = Json { ignoreUnknownKeys = true }

    private val client: HttpClient by lazy {
        HttpClient(Android) {
            install(HttpTimeout) {
                requestTimeoutMillis = 20_000
                connectTimeoutMillis = 20_000
                socketTimeoutMillis = 20_000
            }
        }
    }

    /**
     * Busca o endereço do [cep] (com ou sem máscara). Retorna [Result.failure]
     * quando o CEP é inválido, não encontrado ou a requisição falha.
     */
    suspend fun buscar(cep: String): Result<EnderecoViaCep> = withContext(Dispatchers.IO) {
        try {
            val digits = CepFormatter.unformat(cep)
            if (!CepFormatter.isValid(cep)) {
                return@withContext Result.failure(IllegalArgumentException("CEP inválido"))
            }
            val body = client.get("https://viacep.com.br/ws/$digits/json/").bodyAsText()
            parse(body)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Decodifica a resposta JSON do ViaCEP. CEPs inexistentes retornam
     * `{"erro": true}` ou sem `logradouro`.
     */
    fun parse(jsonText: String): Result<EnderecoViaCep> {
        return try {
            val parsed = json.decodeFromString<ViaCepResponse>(jsonText)
            val logradouro = parsed.logradouro?.trim().orEmpty()
            val bairro = parsed.bairro?.trim().orEmpty()
            val localidade = parsed.localidade?.trim().orEmpty()
            val uf = parsed.uf?.trim().orEmpty()
            if (parsed.erro == true || logradouro.isEmpty() || localidade.isEmpty()) {
                Result.failure(IllegalArgumentException("CEP não encontrado"))
            } else {
                Result.success(EnderecoViaCep(logradouro, bairro, localidade, uf))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
