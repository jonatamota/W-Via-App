package com.w_via.features.declaration.createdeclaration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cobre a estrutura das três abas de "Dados do Óbito": Local, Sepultamento e
 * Médico. A tela (DadosObitoSectionScreen) usa [OBITO_TABS] tanto para renderizar
 * a ScrollableTabRow quanto para decidir qual conteúdo exibir pelo índice.
 */
class DadosObitoTabsTest {

    @Test
    fun `as abas de dados do obito sao exatamente tres`() {
        assertEquals(3, OBITO_TABS.size)
    }

    @Test
    fun `abas de dados do obito seguem a ordem Local, Sepultamento, Medico`() {
        assertEquals(listOf("Local", "Sepultamento", "Médico"), OBITO_TABS)
    }

    @Test
    fun `todas as abas possuem rotulo nao vazio`() {
        assertTrue(OBITO_TABS.all { it.isNotBlank() })
    }
}