package com.w_via.features.declaration.createdeclaration

import com.w_via.features.declaration.EstadoCivil
import com.w_via.features.declaration.Sexo
import com.w_via.features.declaration.TipoLocalObito
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeclarationDraftViewModelTest {

    private fun completeFalecido() = DeclarationSection.FALECIDO

    private fun fillFalecido(vm: DeclarationDraftViewModel) {
        vm.update {
            it.copy(
                nome = "João da Silva",
                cpf = "123.456.789-00",
                dataNascimento = "03/08/1980",
                sexo = Sexo.MASCULINO,
                rg = "12.345.678-9",
                cor = "Branca",
                profissao = "Aposentado",
                natural = "São Paulo",
                ufNatural = "SP",
                endereco = "Rua Exemplo, 100",
                cep = "01000-000",
                cidade = "São Paulo",
                uf = "SP",
                estadoCivil = EstadoCivil.SOLTEIRO
            )
        }
    }

    private fun fillObito(vm: DeclarationDraftViewModel) {
        vm.update {
            it.copy(
                tipoLocalObito = TipoLocalObito.CASA,
                dataObito = "03/08/2026",
                nomeLocalObito = "Residência",
                horaFalecimento = "14:30",
                causasMorte = listOf("Parada cardiorrespiratória"),
                numeroAtestadoObito = "00123",
                ufMedico1 = "SP",
                crm1 = "12345",
                medico1 = "Dr. Roberto"
            )
        }
    }

    private fun fillFiliacao(vm: DeclarationDraftViewModel) {
        vm.update { it.copy(nomeMae = "Ana da Silva") }
    }

    private fun fillDeclarante(vm: DeclarationDraftViewModel) {
        vm.update {
            it.copy(
                emailDeclarante = "maria@exemplo.com",
                profissaoDeclarante = "Do lar",
                declaranteCartorio = "1º Cartório",
                ufCartorio = "SP",
                cidadeCartorio = "São Paulo",
                declaranteTelefone = "(11) 99999-9999",
                enderecoCartorio = "Rua do Cartório, 1",
                emailCartorio = "cartorio@exemplo.com",
                oficialCartorio = "Fulano de Tal"
            )
        }
    }

    private fun confirmAllRequired(vm: DeclarationDraftViewModel) {
        fillFalecido(vm); fillObito(vm); fillFiliacao(vm); fillDeclarante(vm)
        listOf(
            DeclarationSection.FALECIDO,
            DeclarationSection.OBITO,
            DeclarationSection.FILIACAO,
            DeclarationSection.DECLARANTE
        ).forEach { vm.confirm(it) }
    }

    // --- confirm() ---

    @Test
    fun `confirm retorna false e nao marca quando a secao esta incompleta`() {
        val vm = DeclarationDraftViewModel()
        val result = vm.confirm(DeclarationSection.FALECIDO)
        assertFalse(result)
        assertTrue(DeclarationSection.FALECIDO !in vm.confirmedSections.value)
    }

    @Test
    fun `confirm retorna true e marca quando a secao esta completa`() {
        val vm = DeclarationDraftViewModel()
        fillFalecido(vm)
        val result = vm.confirm(DeclarationSection.FALECIDO)
        assertTrue(result)
        assertTrue(DeclarationSection.FALECIDO in vm.confirmedSections.value)
    }

    @Test
    fun `confirm de secao opcional funciona mesmo vazia`() {
        val vm = DeclarationDraftViewModel()
        assertTrue(vm.confirm(DeclarationSection.OBS))
        assertTrue(vm.confirm(DeclarationSection.CONJUGE_FILHOS))
        assertTrue(DeclarationSection.OBS in vm.confirmedSections.value)
        assertTrue(DeclarationSection.CONJUGE_FILHOS in vm.confirmedSections.value)
    }

    // --- canSave() ---

    @Test
    fun `canSave false quando nenhuma secao confirmada`() {
        val vm = DeclarationDraftViewModel()
        fillFalecido(vm); fillObito(vm); fillFiliacao(vm); fillDeclarante(vm)
        assertFalse(vm.canSave())
    }

    @Test
    fun `canSave false quando apenas algumas secoes confirmadas`() {
        val vm = DeclarationDraftViewModel()
        fillFalecido(vm); fillObito(vm); fillFiliacao(vm); fillDeclarante(vm)
        vm.confirm(DeclarationSection.FALECIDO)
        vm.confirm(DeclarationSection.OBITO)
        assertFalse(vm.canSave())
    }

    @Test
    fun `canSave true quando todas as secoes obrigatorias confirmadas`() {
        val vm = DeclarationDraftViewModel()
        confirmAllRequired(vm)
        assertTrue(vm.canSave())
    }

    @Test
    fun `canSave true sem confirmar secoes opcionais`() {
        val vm = DeclarationDraftViewModel()
        confirmAllRequired(vm)
        assertFalse(DeclarationSection.OBS in vm.confirmedSections.value)
        assertFalse(DeclarationSection.CONJUGE_FILHOS in vm.confirmedSections.value)
        assertTrue(vm.canSave())
    }

    @Test
    fun `canSave volta a false se uma secao obrigatoria e revertida`() {
        val vm = DeclarationDraftViewModel()
        confirmAllRequired(vm)
        assertTrue(vm.canSave())
        // Reverte dados do falecido após confirmação
        vm.update { it.copy(nome = "") }
        assertFalse(vm.canSave())
    }

    // --- Progresso ---

    @Test
    fun `requiredCompletedCount reflete confirmacoes obrigatorias`() {
        val vm = DeclarationDraftViewModel()
        fillFalecido(vm); fillObito(vm); fillFiliacao(vm); fillDeclarante(vm)
        assertEquals(0, vm.requiredCompletedCount())
        vm.confirm(DeclarationSection.FALECIDO)
        vm.confirm(DeclarationSection.OBITO)
        assertEquals(2, vm.requiredCompletedCount())
        vm.confirm(DeclarationSection.FILIACAO)
        vm.confirm(DeclarationSection.DECLARANTE)
        assertEquals(4, vm.requiredCompletedCount())
        assertEquals(4, vm.requiredTotal())
    }

    // --- Filhos ---

    @Test
    fun `adicionar e atualizar filhos mantem a lista`() {
        val vm = DeclarationDraftViewModel()
        vm.adicionarFilho()
        vm.adicionarFilho()
        vm.atualizarFilho(0, "Pedro")
        vm.atualizarFilho(1, "Paula")
        assertEquals(listOf("Pedro", "Paula"), vm.state.value.filhos)
    }

    @Test
    fun `remover filho reduz a lista`() {
        val vm = DeclarationDraftViewModel()
        vm.adicionarFilho()
        vm.atualizarFilho(0, "Pedro")
        vm.removerFilho(0)
        assertTrue(vm.state.value.filhos.isEmpty())
    }

    @Test
    fun `atualizar indice invalido nao altera a lista`() {
        val vm = DeclarationDraftViewModel()
        vm.adicionarFilho()
        vm.atualizarFilho(0, "Pedro")
        vm.atualizarFilho(5, "Ignorado")
        assertEquals(listOf("Pedro"), vm.state.value.filhos)
    }

    // --- reset ---

    @Test
    fun `reset limpa estado e confirmacoes`() {
        val vm = DeclarationDraftViewModel()
        confirmAllRequired(vm)
        vm.confirm(DeclarationSection.OBS)
        assertTrue(vm.canSave())
        vm.reset()
        assertTrue(vm.state.value.nome.isBlank())
        assertTrue(vm.confirmedSections.value.isEmpty())
        assertFalse(vm.canSave())
    }

    // --- toDeclaracaoObito ---

    @Test
    fun `toDeclaracaoObito reflete os dados preenchidos`() {
        val vm = DeclarationDraftViewModel()
        fillFalecido(vm)
        vm.update { it.copy(observacoes = "Nota extra") }
        val declaracao = vm.toDeclaracaoObito()
        assertEquals("João da Silva", declaracao.falecido.nome)
        assertEquals("Nota extra", declaracao.observacoes)
    }

    // --- update() preserva campos nao tocados ---

    @Test
    fun `update generico preserva campos anteriores`() {
        val vm = DeclarationDraftViewModel()
        vm.update { it.copy(nome = "João") }
        vm.update { it.copy(idade = "45") }
        val s = vm.state.value
        assertEquals("João", s.nome)
        assertEquals("45", s.idade)
    }

    // --- Estado de geracao do PDF (sobrevive à rotação) ---

    @Test
    fun `setGenerating atualiza o estado de geracao`() {
        val vm = DeclarationDraftViewModel()
        assertFalse(vm.isGenerating.value)
        vm.setGenerating(true)
        assertTrue(vm.isGenerating.value)
        vm.setGenerating(false)
        assertFalse(vm.isGenerating.value)
    }

    @Test
    fun `setPdfResult guarda o resultado e interrompe a geracao`() {
        val vm = DeclarationDraftViewModel()
        vm.setGenerating(true)
        vm.setPdfResult(DeclarationDraftViewModel.PdfResult(isSuccess = true, message = "PDF gerado com sucesso"))
        assertFalse(vm.isGenerating.value)
        assertEquals(true, vm.pdfResult.value?.isSuccess)
    }

    @Test
    fun `setPdfResult null limpa o resultado`() {
        val vm = DeclarationDraftViewModel()
        vm.setPdfResult(DeclarationDraftViewModel.PdfResult(isSuccess = false, message = "Erro"))
        vm.setPdfResult(null)
        assertNull(vm.pdfResult.value)
    }

    @Test
    fun `reset limpa o resultado do PDF`() {
        val vm = DeclarationDraftViewModel()
        vm.setGenerating(true)
        vm.setPdfResult(DeclarationDraftViewModel.PdfResult(isSuccess = true, message = "PDF gerado com sucesso"))
        vm.reset()
        assertNull(vm.pdfResult.value)
        assertFalse(vm.isGenerating.value)
    }

    // --- Normalização e modal de conformidade ---

    @Test
    fun `update gera conflito e limpa campo nao aplicavel`() {
        val vm = DeclarationDraftViewModel()
        vm.update { it.copy(estadoCivil = EstadoCivil.CASADO, nomeConjuge = "Maria") }
        assertTrue(vm.conflictMessage.value.isNullOrEmpty())

        vm.update { it.copy(estadoCivil = EstadoCivil.SOLTEIRO) }
        assertFalse(vm.conflictMessage.value.isNullOrEmpty())
        assertTrue(vm.state.value.nomeConjuge.isBlank())
    }

    @Test
    fun `clearConflict descarta a mensagem de conformidade`() {
        val vm = DeclarationDraftViewModel()
        vm.update { it.copy(idade = "8", filhos = listOf("Pedro")) }
        assertFalse(vm.conflictMessage.value.isNullOrEmpty())
        vm.clearConflict()
        assertNull(vm.conflictMessage.value)
    }

    @Test
    fun `menor de 16 tem estado civil solteiro automatico`() {
        val vm = DeclarationDraftViewModel()
        vm.update { it.copy(idade = "45", estadoCivil = EstadoCivil.CASADO) }
        vm.update { it.copy(idade = "10") }
        assertEquals(EstadoCivil.SOLTEIRO, vm.state.value.estadoCivil)
    }

    @Test
    fun `adicionarFilho passa pela normalizacao`() {
        val vm = DeclarationDraftViewModel()
        vm.update { it.copy(idade = "8") }
        vm.adicionarFilho()
        // menor de 10: filhos não aplicável, então a adição é removida pela normalização
        assertTrue(vm.state.value.filhos.isEmpty())
    }

    // --- Causas da morte: Causa 1 sempre visível ---

    @Test
    fun `causa 1 inicia visivel`() {
        val vm = DeclarationDraftViewModel()
        assertEquals(listOf(""), vm.state.value.causasMorte)
    }

    @Test
    fun `removerCausa nunca deixa a lista de causas vazia`() {
        val vm = DeclarationDraftViewModel()
        vm.adicionarCausa()
        vm.adicionarCausa()
        vm.removerCausa(vm.state.value.causasMorte.size - 1)
        vm.removerCausa(vm.state.value.causasMorte.size - 1)
        assertEquals(listOf(""), vm.state.value.causasMorte)
    }

    @Test
    fun `hasContent falso no estado inicial e verdadeiro apos preencher`() {
        val vm = DeclarationDraftViewModel()
        assertFalse(vm.hasContent())
        vm.update { it.copy(nome = "João") }
        assertTrue(vm.hasContent())
    }

    // --- Seção Cônjuge/Filhos obrigatória quando casado ---

    @Test
    fun `canSave bloqueia salvar quando casado sem nome do conjuge`() {
        val vm = DeclarationDraftViewModel()
        confirmAllRequired(vm)
        vm.update { it.copy(estadoCivil = EstadoCivil.CASADO) }
        assertFalse(vm.canSave())

        vm.update { it.copy(nomeConjuge = "Maria") }
        vm.confirm(DeclarationSection.CONJUGE_FILHOS)
        assertTrue(vm.canSave())
    }

    @Test
    fun `requiredTotal nao muda quando casado`() {
        val vm = DeclarationDraftViewModel()
        assertEquals(4, vm.requiredTotal())
        vm.update { it.copy(estadoCivil = EstadoCivil.CASADO) }
        // O cônjuge agora faz parte de FALECIDO; CONJUGE_FILHOS é sempre opcional.
        assertEquals(4, vm.requiredTotal())
    }

    @Test
    fun `adicionarFilho respeita o limite de 20`() {
        val vm = DeclarationDraftViewModel()
        vm.update { it.copy(idade = "45") }
        repeat(20) { vm.adicionarFilho() }
        assertEquals(20, vm.state.value.filhos.size)
        vm.adicionarFilho()
        assertEquals(20, vm.state.value.filhos.size)
    }
}
