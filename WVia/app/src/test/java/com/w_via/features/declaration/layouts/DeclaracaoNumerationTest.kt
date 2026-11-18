package com.w_via.features.declaration.layouts

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Garante que a numeração progressiva da declaração é monotônica: cada id do
 * banco (que nunca é reutilizado, pois soft delete mantém a linha) gera um
 * número exibido consecutivo, mesmo com declarações excluídas.
 */
class DeclaracaoNumerationTest {

    @Test
    fun `numeroExibido comeca no numero inicial`() {
        assertEquals(DECLARACAO_NUMERO_INICIAL, numeroExibido(1))
    }

    @Test
    fun `numeroExibido avanca uma unidade por id`() {
        assertEquals(101L, numeroExibido(1))
        assertEquals(102L, numeroExibido(2))
        assertEquals(103L, numeroExibido(3))
    }

    @Test
    fun `numeroExibido nunca repete numero para ids diferentes`() {
        val ids = (1..20L).map { numeroExibido(it) }
        assertEquals(ids.toSet().size, ids.size)
        assertEquals(ids, ids.sorted())
    }
}
