package com.w_via.features.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccordionSelectStateTest {

    @Test
    fun `comeca recolhido`() {
        val state = AccordionSelectState()
        assertFalse(state.expanded)
    }

    @Test
    fun `pode comecar expandido`() {
        val state = AccordionSelectState(initialExpanded = true)
        assertTrue(state.expanded)
    }

    @Test
    fun `toggle expande quando recolhido`() {
        val state = AccordionSelectState()
        state.toggle()
        assertTrue(state.expanded)
    }

    @Test
    fun `toggle recolhe quando expandido`() {
        val state = AccordionSelectState(initialExpanded = true)
        state.toggle()
        assertFalse(state.expanded)
    }

    @Test
    fun `toggle alterna o estado`() {
        val state = AccordionSelectState()
        assertFalse(state.expanded)
        state.toggle()
        assertTrue(state.expanded)
        state.toggle()
        assertFalse(state.expanded)
    }

    @Test
    fun `expand e collapse forcam o estado`() {
        val state = AccordionSelectState()
        state.expand()
        assertTrue(state.expanded)
        state.expand()
        assertTrue(state.expanded)
        state.collapse()
        assertFalse(state.expanded)
        state.collapse()
        assertFalse(state.expanded)
    }

    @Test
    fun `selecionar opcao notifica e recolhe`() {
        val state = AccordionSelectState(initialExpanded = true)
        val selected = mutableListOf<String>()

        state.onOptionSelected("São Paulo") { selected.add(it) }

        assertEquals(listOf("São Paulo"), selected)
        assertFalse(state.expanded)
    }

    @Test
    fun `selecionar com callback vazio ainda recolhe`() {
        val state = AccordionSelectState(initialExpanded = true)

        state.onOptionSelected("MG") { }

        assertFalse(state.expanded)
    }
}
