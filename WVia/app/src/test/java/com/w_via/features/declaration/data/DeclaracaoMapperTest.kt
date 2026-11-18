package com.w_via.features.declaration.data

import com.w_via.features.declaration.EstadoCivil
import com.w_via.features.declaration.Sexo
import com.w_via.features.declaration.TipoLocalObito
import com.w_via.features.declaration.state.DeclaracaoObitoFormState
import com.w_via.features.declaration.toDeclaracaoObito
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeclaracaoMapperTest {

    private fun sampleDeclaracao() = DeclaracaoObitoFormState().copy(
        nome = "João da Silva",
        cpf = "123.456.789-00",
        idade = "45",
        sexo = Sexo.MASCULINO,
        estadoCivil = EstadoCivil.CASADO,
        nomeConjuge = "Maria",
        tipoLocalObito = TipoLocalObito.HOSPITAL,
        nomeLocalObito = "Hospital Central",
        dataObito = "03/08/2026",
        nomeDeclarante = "Maria da Silva",
        cpfDeclarante = "987.654.321-00",
        filhos = listOf("Pedro", "Paula")
    ).toDeclaracaoObito()

    @Test
    fun `roundtrip entidade preserva o modelo de dominio`() {
        val declaracao = sampleDeclaracao()
        val entity = DeclaracaoMapper.toEntity(userId = "user-1", declaracao = declaracao, now = 123L)
        val restored = DeclaracaoMapper.fromEntity(entity)
        assertEquals(declaracao, restored)
    }

    @Test
    fun `entidade expoe os metadados usados na listagem`() {
        val entity = DeclaracaoMapper.toEntity("user-1", sampleDeclaracao(), now = 123L)
        assertEquals("João da Silva", entity.nome)
        assertEquals("03/08/2026", entity.dataObito)
        assertEquals("Hospital Central", entity.localObito)
        assertEquals("user-1", entity.userId)
        assertEquals(123L, entity.createdAt)
        assertEquals(123L, entity.updatedAt)
        assertTrue(entity.payloadJson.isNotBlank())
    }

    @Test
    fun `localObito usa tipoLocal quando nome em branco`() {
        val declaracao = sampleDeclaracao().let {
            it.copy(obito = it.obito.copy(nomeLocal = ""))
        }
        val entity = DeclaracaoMapper.toEntity("user-1", declaracao)
        assertEquals(declaracao.obito.tipoLocal, entity.localObito)
    }

    @Test
    fun `json roundtrip direto preserva o modelo`() {
        val declaracao = sampleDeclaracao()
        val json = DeclaracaoMapper.toJson(declaracao)
        assertEquals(declaracao, DeclaracaoMapper.fromJson(json))
    }
}
