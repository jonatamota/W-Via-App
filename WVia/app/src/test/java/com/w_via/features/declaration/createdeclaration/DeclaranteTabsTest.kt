package com.w_via.features.declaration.createdeclaration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cobre a estrutura das duas abas de "Dados do Declarante": Declarante e
 * Cartório. A tela (DeclaranteSectionScreen) usa [DECLARANTE_TABS] tanto para
 * renderizar a ScrollableTabRow quanto para decidir qual conteúdo exibir.
 */
class DeclaranteTabsTest {

    @Test
    fun `abas do declarante sao exatamente duas`() {
        assertEquals(2, DECLARANTE_TABS.size)
    }

    @Test
    fun `abas do declarante seguem a ordem Declarante, Cartorio`() {
        assertEquals(listOf("Declarante", "Cartório"), DECLARANTE_TABS)
    }

    @Test
    fun `todas as abas possuem rotulo nao vazio`() {
        assertTrue(DECLARANTE_TABS.all { it.isNotBlank() })
    }
}
