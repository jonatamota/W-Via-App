package com.w_via.features.receipt.data

import com.w_via.features.receipt.Recibo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReciboMapperTest {

    private fun sampleRecibo() = Recibo(
        uuid = "550e8400-e29b-41d4-a716-446655440000",
        nomeContratante = "Funerária Estrela",
        cpf_cnpj = "12345678",
        nome_falecido = "João da Silva",
        total_pago = "1250.50",
        data_pagamento = "04/08/2026"
    )

    @Test
    fun `roundtrip entidade preserva o modelo de dominio`() {
        val recibo = sampleRecibo()
        val entity = ReciboMapper.toEntity(userId = "user-1", recibo = recibo, now = 123L)
        val restored = ReciboMapper.fromEntity(entity)
        assertEquals(recibo, restored)
    }

    @Test
    fun `entidade expoe os metadados usados na listagem`() {
        val entity = ReciboMapper.toEntity("user-1", sampleRecibo(), now = 123L)
        assertEquals("Funerária Estrela", entity.nomeContratante)
        assertEquals("04/08/2026", entity.dataPagamento)
        assertEquals("user-1", entity.userId)
        assertEquals(123L, entity.createdAt)
        assertEquals("550e8400-e29b-41d4-a716-446655440000", entity.uuid)
        assertTrue(entity.totalPago.startsWith("R$"))
        assertTrue(entity.payloadJson.isNotBlank())
    }

    @Test
    fun `fromEntity usa o uuid da coluna quando o payload nao o tem`() {
        val entity = ReciboMapper.toEntity("user-1", sampleRecibo().copy(uuid = ""), now = 123L)
            .copy(uuid = "uuid-da-coluna")
        assertEquals("uuid-da-coluna", ReciboMapper.fromEntity(entity).uuid)
    }

    @Test
    fun `totalPago fica vazio quando total zero`() {
        val entity = ReciboMapper.toEntity("user-1", sampleRecibo().copy(total_pago = ""))
        assertEquals("", entity.totalPago)
    }

    @Test
    fun `json roundtrip direto preserva o modelo`() {
        val recibo = sampleRecibo()
        val json = ReciboMapper.toJson(recibo)
        assertEquals(recibo, ReciboMapper.fromJson(json))
    }
}
