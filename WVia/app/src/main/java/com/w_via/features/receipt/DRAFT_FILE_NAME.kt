package com.w_via.features.receipt

import android.content.Context
import com.w_via.features.authentication.AuthManager
import org.json.JSONObject
import java.io.File
import kotlinx.datetime.LocalDate

private const val DRAFT_FILE_NAME = "recibo_draft.json"

data class ReciboDraft(
    val currentStep: Int = 1,
    val a: String = "00001",
    val cpf_cnpj_mode: String = "CNPJ",
    val nomeContratante: String = "",
    val cpf_cnpj: String = "",
    val cep_contratante: String = "",
    val endereco_contratante: String = "",
    val uf_contratante: String = "",
    val cidade_contratante: String = "",
    val numero_contratante: String = "",
    val bairro_contratante: String = "",
    val rg_ie: String = "",
    val tel: String = "",
    val email: String = "",
    val nome_falecido: String = "",
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
    val tipo_local_saida: String = "",
    val uf_local_saida: String = "",
    val cidade_local_saida: String = "",
    val hora_saida_funeral: String = "",
    val dia_funeral: String? = null, // ISO yyyy-MM-dd
    val cemiterio_sepultamento: String = "",
    val tipo_sepultamento: String = "",
    val uf_sepultamento: String = "",
    val cidade_sepultamento: String = "",
    val descricao: String = "",
    val quantidade: Int = 0,
    val unidade: Int = 0,
    val valor: Double = 0.0,
    val total_pago: Double = 0.0,
    val valor_cheque: Double = 0.0,
    val rg_contratante: String = "",
    val data_pagamento_epoch: Long = System.currentTimeMillis() / 1000,
    val data_pagamento: String = "",
    val editing_recibo_id: Long = 0,
)

object ReciboDraftStorage {

    /** Nome do arquivo de draft vinculado ao id do usuário logado (fallback "local"). */
    private fun draftFileName(): String {
        val userId = AuthManager.getCurrentUserId() ?: "local"
        return "${DRAFT_FILE_NAME.replace(".json", "")}_${userId}.json"
    }

    private fun draftFile(context: Context): File = File(context.filesDir, draftFileName())

    fun hasDraft(context: Context): Boolean = draftFile(context).exists()

    fun save(context: Context, draft: ReciboDraft) {
        val json = JSONObject().apply {
            put("currentStep", draft.currentStep)
            put("a", draft.a)
            put("cpf_cnpj_mode", draft.cpf_cnpj_mode)
            put("nomeContratante", draft.nomeContratante)
            put("cpf_cnpj", draft.cpf_cnpj)
            put("cep_contratante", draft.cep_contratante)
            put("endereco_contratante", draft.endereco_contratante)
            put("uf_contratante", draft.uf_contratante)
            put("cidade_contratante", draft.cidade_contratante)
            put("numero_contratante", draft.numero_contratante)
            put("bairro_contratante", draft.bairro_contratante)
            put("rg_ie", draft.rg_ie)
            put("tel", draft.tel)
            put("email", draft.email)
            put("nome_falecido", draft.nome_falecido)
            put("idade_falecido", draft.idade_falecido)
            put("sexo_falecido", draft.sexo_falecido)
            put("naturalidade_falecido", draft.naturalidade_falecido)
            put("uf_falecido", draft.uf_falecido)
            put("rg_falecido", draft.rg_falecido)
            put("cpf_falecido", draft.cpf_falecido)
            put("filiacao_falecido", draft.filiacao_falecido)
            put("pai_falecido", draft.pai_falecido)
            put("mae_falecido", draft.mae_falecido)
            put("local_funeral", draft.local_funeral)
            put("tipo_local_saida", draft.tipo_local_saida)
            put("uf_local_saida", draft.uf_local_saida)
            put("cidade_local_saida", draft.cidade_local_saida)
            put("hora_saida_funeral", draft.hora_saida_funeral)
            put("dia_funeral", draft.dia_funeral ?: "")
            put("cemiterio_sepultamento", draft.cemiterio_sepultamento)
            put("tipo_sepultamento", draft.tipo_sepultamento)
            put("uf_sepultamento", draft.uf_sepultamento)
            put("cidade_sepultamento", draft.cidade_sepultamento)
            put("descricao", draft.descricao)
            put("quantidade", draft.quantidade)
            put("unidade", draft.unidade)
            put("valor", draft.valor)
            put("total_pago", draft.total_pago)
            put("valor_cheque", draft.valor_cheque)
            put("rg_contratante", draft.rg_contratante)
            put("data_pagamento_epoch", draft.data_pagamento_epoch)
            put("data_pagamento", draft.data_pagamento)
            put("editing_recibo_id", draft.editing_recibo_id)
        }
        try {
            draftFile(context).writeText(json.toString())
        } catch (_: Exception) {
            // falha silenciosa de escrita não deve travar a UI
        }
    }

    fun load(context: Context): ReciboDraft? {
        val file = draftFile(context)
        if (!file.exists()) return null
        return try {
            val json = JSONObject(file.readText())
            ReciboDraft(
                currentStep = json.optInt("currentStep", 1),
                a = json.optString("a", "00001"),
                cpf_cnpj_mode = json.optString("cpf_cnpj_mode", "CNPJ"),
                nomeContratante = json.optString("nomeContratante", ""),
                cpf_cnpj = json.optString("cpf_cnpj", ""),
                cep_contratante = json.optString("cep_contratante", ""),
                endereco_contratante = json.optString("endereco_contratante", ""),
                uf_contratante = json.optString("uf_contratante", ""),
                cidade_contratante = json.optString("cidade_contratante", ""),
                numero_contratante = json.optString("numero_contratante", ""),
                bairro_contratante = json.optString("bairro_contratante", ""),
                rg_ie = json.optString("rg_ie", ""),
                tel = json.optString("tel", ""),
                email = json.optString("email", ""),
                nome_falecido = json.optString("nome_falecido", ""),
                idade_falecido = json.optString("idade_falecido", ""),
                sexo_falecido = json.optString("sexo_falecido", ""),
                naturalidade_falecido = json.optString("naturalidade_falecido", ""),
                uf_falecido = json.optString("uf_falecido", ""),
                rg_falecido = json.optString("rg_falecido", ""),
                cpf_falecido = json.optString("cpf_falecido", ""),
                filiacao_falecido = json.optString("filiacao_falecido", ""),
                pai_falecido = json.optString("pai_falecido", ""),
                mae_falecido = json.optString("mae_falecido", ""),
                local_funeral = json.optString("local_funeral", ""),
                tipo_local_saida = json.optString("tipo_local_saida", ""),
                uf_local_saida = json.optString("uf_local_saida", ""),
                cidade_local_saida = json.optString("cidade_local_saida", ""),
                hora_saida_funeral = json.optString("hora_saida_funeral", ""),
                dia_funeral = json.optString("dia_funeral", "").ifBlank { null },
                cemiterio_sepultamento = json.optString("cemiterio_sepultamento", ""),
                tipo_sepultamento = json.optString("tipo_sepultamento", ""),
                uf_sepultamento = json.optString("uf_sepultamento", ""),
                cidade_sepultamento = json.optString("cidade_sepultamento", ""),
                descricao = json.optString("descricao", ""),
                quantidade = json.optInt("quantidade", 0),
                unidade = json.optInt("unidade", 0),
                valor = json.optDouble("valor", 0.0),
                total_pago = json.optDouble("total_pago", 0.0),
                valor_cheque = json.optDouble("valor_cheque", 0.0),
                rg_contratante = json.optString("rg_contratante", ""),
                data_pagamento_epoch = json.optLong(
                    "data_pagamento_epoch",
                    System.currentTimeMillis() / 1000
                ),
                data_pagamento = json.optString("data_pagamento", ""),
                editing_recibo_id = json.optLong("editing_recibo_id", 0),
            )
        } catch (_: Exception) {
            null
        }
    }

    fun delete(context: Context) {
        val file = draftFile(context)
        if (file.exists()) file.delete()
    }

    /**
     * Semeia o rascunho a partir de um recibo já salvo para o fluxo de edição.
     * O [editingReciboId] marca o recibo original para ser sinalizado como
     * desatualizado quando o novo recibo for salvo.
     */
    fun saveFromRecibo(context: Context, recibo: com.w_via.features.receipt.Recibo, editingReciboId: Long = 0) {
        val dia = recibo.dia_funeral.let { raw ->
            raw.trim().takeIf { it.isNotBlank() }?.let {
                runCatching {
                    val parts = it.split("/")
                    LocalDate(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
                }.getOrNull()
            }
        }
        val draft = ReciboDraft(
            currentStep = 1,
            a = recibo.numero.ifBlank { "00001" },
            nomeContratante = recibo.nomeContratante,
            cpf_cnpj = recibo.cpf_cnpj,
            cpf_cnpj_mode = if (recibo.cpf_cnpj.trim().isNotEmpty() && isCnpj(recibo.cpf_cnpj)) "CNPJ" else "CPF",
            rg_ie = recibo.rg_ie,
            endereco_contratante = recibo.endereco_contratante,
            tel = recibo.tel,
            email = recibo.email,
            nome_falecido = recibo.nome_falecido,
            idade_falecido = recibo.idade_falecido,
            sexo_falecido = recibo.sexo_falecido,
            naturalidade_falecido = recibo.naturalidade_falecido,
            uf_falecido = recibo.uf_falecido,
            rg_falecido = recibo.rg_falecido,
            cpf_falecido = recibo.cpf_falecido,
            pai_falecido = recibo.pai_falecido,
            mae_falecido = recibo.mae_falecido,
            filiacao_falecido = recibo.filiacao_falecido,
            local_funeral = recibo.local_funeral,
            hora_saida_funeral = recibo.hora_saida_funeral,
            dia_funeral = dia?.toString(),
            cemiterio_sepultamento = recibo.cemiterio_sepultamento,
            descricao = recibo.descricao,
            quantidade = recibo.quantidade.toIntOrNull() ?: 1,
            unidade = 0,
            valor = recibo.valor.toDoubleOrNull() ?: 0.0,
            total_pago = recibo.total_pago.toDoubleOrNull() ?: 0.0,
            valor_cheque = recibo.valor_cheque.toDoubleOrNull() ?: 0.0,
            rg_contratante = recibo.rg_contratante,
            data_pagamento = recibo.data_pagamento,
            editing_recibo_id = editingReciboId
        )
        save(context, draft)
    }

    private fun isCnpj(value: String): Boolean {
        val digits = value.filter { it.isDigit() }
        return digits.length == 14
    }
}
