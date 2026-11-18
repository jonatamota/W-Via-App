package com.w_via.features.declaration

import com.w_via.features.declaration.state.DeclaracaoObitoStep
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Cobre a validação unificada do fluxo linear: as regras de `stepAtualValido()`
 * são delegadas às seções correspondentes do fluxo por seções.
 */
class DeclaracaoObitoViewModelTest {

    private val vm = DeclaracaoObitoViewModel()

    private fun irPara(step: DeclaracaoObitoStep) = vm.irParaStep(step)

    // --- IDENTIFICACAO -> FALECIDO ---

    @Test
    fun `identificacao incompleta bloqueia avancar`() {
        irPara(DeclaracaoObitoStep.IDENTIFICACAO)
        vm.update { it.copy(nome = "João da Silva") }
        assertFalse(vm.stepAtualValido())
    }

    @Test
    fun `identificacao completa libera avancar`() {
        irPara(DeclaracaoObitoStep.IDENTIFICACAO)
        vm.update { it.copy(nome = "João da Silva", cpf = "123.456.789-00", dataNascimento = "03/08/1980", sexo = Sexo.MASCULINO) }
        assertTrue(vm.stepAtualValido())
    }

    // --- OBITO -> OBITO ---

    @Test
    fun `obito sem local e data bloqueia avancar`() {
        irPara(DeclaracaoObitoStep.OBITO)
        vm.update { it.copy(dataObito = "03/08/2026") }
        assertFalse(vm.stepAtualValido())
    }

    @Test
    fun `obito com local e data libera avancar`() {
        irPara(DeclaracaoObitoStep.OBITO)
        vm.update { it.copy(tipoLocalObito = TipoLocalObito.CASA, dataObito = "03/08/2026") }
        assertTrue(vm.stepAtualValido())
    }

    // --- FILIACAO -> FILIACAO (agora obrigatória, alinhada às seções) ---

    @Test
    fun `filiacao vazia bloqueia avancar`() {
        irPara(DeclaracaoObitoStep.FILIACAO)
        assertFalse(vm.stepAtualValido())
    }

    @Test
    fun `filiacao com ao menos um genitor libera avancar`() {
        irPara(DeclaracaoObitoStep.FILIACAO)
        vm.update { it.copy(nomeMae = "Ana da Silva") }
        assertTrue(vm.stepAtualValido())
    }

    // --- DECLARANTE -> DECLARANTE ---

    @Test
    fun `declarante incompleto bloqueia avancar`() {
        irPara(DeclaracaoObitoStep.DECLARANTE)
        vm.update { it.copy(nomeDeclarante = "Maria da Silva") }
        assertFalse(vm.stepAtualValido())
    }

    @Test
    fun `declarante completo libera avancar`() {
        irPara(DeclaracaoObitoStep.DECLARANTE)
        vm.update { it.copy(nomeDeclarante = "Maria da Silva", cpfDeclarante = "987.654.321-00") }
        assertTrue(vm.stepAtualValido())
    }

    // --- Steps de preenchimento livre ---

    @Test
    fun `steps de preenchimento livre nunca bloqueiam avancar`() {
        listOf(
            DeclaracaoObitoStep.ESTADO_CIVIL,
            DeclaracaoObitoStep.DOCUMENTOS,
            DeclaracaoObitoStep.BENS,
            DeclaracaoObitoStep.ENDERECO_CARTORIO_INSS,
            DeclaracaoObitoStep.FILHOS
        ).forEach { step ->
            irPara(step)
            assertTrue("Step $step deveria liberar avancar", vm.stepAtualValido())
        }
    }

    // --- Causas da morte: Causa 1 sempre visível ---

    @Test
    fun `causa 1 inicia visivel`() {
        assertEquals(listOf(""), vm.state.value.causasMorte)
    }

    @Test
    fun `removerCausa nunca deixa a lista de causas vazia`() {
        vm.adicionarCausa()
        vm.adicionarCausa()
        vm.removerCausa(vm.state.value.causasMorte.size - 1)
        vm.removerCausa(vm.state.value.causasMorte.size - 1)
        assertEquals(listOf(""), vm.state.value.causasMorte)
    }
}
