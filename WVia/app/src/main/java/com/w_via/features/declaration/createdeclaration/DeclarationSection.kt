package com.w_via.features.declaration.createdeclaration

import com.w_via.features.declaration.state.DeclaracaoObitoFormState
import com.w_via.features.utils.AgeCalculator

/**
 * Seções do formulário da Declaração de Óbito exibidas no grid.
 *
 * Cada seção possui regras próprias de completude. As seções marcadas como
 * opcionais ([isRequired] == false) podem ser confirmadas sem preenchimento,
 * mas não bloqueiam a geração do PDF.
 */
enum class DeclarationSection(
    val title: String,
    val isRequired: Boolean
) {
    FALECIDO("Dados do Falecido", true),
    OBITO("Dados do Óbito", true),
    FILIACAO("Filiação", true),
    CONJUGE_FILHOS("Filhos", false),
    DECLARANTE("Dados do Declarante", true),
    OBS("Obs", false);

    /** Verifica se os campos obrigatórios da seção foram preenchidos. */
    fun isComplete(state: DeclaracaoObitoFormState): Boolean = when (this) {
        FALECIDO ->
            state.nome.isNotBlank() &&
                state.cpf.isNotBlank() &&
                AgeCalculator.parse(state.dataNascimento) != null &&
                state.sexo != null &&
                state.rg.isNotBlank() &&
                state.cor.isNotBlank() &&
                state.profissao.isNotBlank() &&
                state.natural.isNotBlank() &&
                state.ufNatural.isNotBlank() &&
                state.endereco.isNotBlank() &&
                state.cep.isNotBlank() &&
                state.cidade.isNotBlank() &&
                state.uf.isNotBlank() &&
                (!state.mostrarEstadoCivil() || state.estadoCivil != null) &&
                (!state.mostrarConjuge() || state.nomeConjuge.isNotBlank()) &&
                (!state.exibirCamposCasamentoCartorio() ||
                    (state.casamentoDia.isNotBlank() &&
                        state.casamentoLivro.isNotBlank() &&
                        state.casamentoFolha.isNotBlank() &&
                        state.casamentoNumero.isNotBlank()))
        OBITO ->
            state.tipoLocalObito != null &&
                state.dataObito.isNotBlank() &&
                state.nomeLocalObito.isNotBlank() &&
                state.horaFalecimento.isNotBlank() &&
                state.causasMorte.any { it.isNotBlank() } &&
                state.numeroAtestadoObito.isNotBlank() &&
                state.ufMedico1.isNotBlank() &&
                state.crm1.isNotBlank() &&
                state.medico1.isNotBlank()
        FILIACAO ->
            state.nomePai.isNotBlank() || state.nomeMae.isNotBlank()
        CONJUGE_FILHOS ->
            // Apenas filhos, que permanecem opcionais.
            true
        DECLARANTE ->
            state.emailDeclarante.isNotBlank() &&
                state.profissaoDeclarante.isNotBlank() &&
                state.declaranteCartorio.isNotBlank() &&
                state.ufCartorio.isNotBlank() &&
                state.cidadeCartorio.isNotBlank() &&
                state.enderecoCartorio.isNotBlank()
        OBS ->
            true
    }

    /**
     * Seção obrigatória no estado atual. A maioria é estática ([isRequired]).
     */
    fun isRequiredFor(state: DeclaracaoObitoFormState): Boolean = when (this) {
        CONJUGE_FILHOS -> false
        else -> isRequired
    }

    /** Mensagem exibida quando o usuário tenta confirmar uma seção incompleta. */
    val completionHint: String
        get() = when (this) {
            FALECIDO -> "Preencha todos os campos de identificação, endereço e estado civil para confirmar esta seção."
            OBITO -> "Preencha o local, data e hora do óbito, causa da morte e os dados do médico para confirmar."
            FILIACAO -> "Informe ao menos um dos pais (pai ou mãe) para confirmar."
            CONJUGE_FILHOS -> "Filhos são opcionais."
            DECLARANTE -> "Preencha e-mail e profissão do declarante e todos os campos da aba Cartório para confirmar."
            OBS -> ""
        }

    /** Campos essenciais (bloqueantes) ainda não preenchidos nesta seção. */
    fun essentialsMissing(state: DeclaracaoObitoFormState): List<String> = when (this) {
        FALECIDO -> {
            val missing = mutableListOf<String>()
            if (state.nome.isBlank()) missing.add("Nome")
            if (state.cpf.isBlank()) missing.add("CPF")
            if (AgeCalculator.parse(state.dataNascimento) == null) missing.add("Data de nascimento")
            if (state.sexo == null) missing.add("Sexo")
            if (state.rg.isBlank()) missing.add("RG")
            if (state.cor.isBlank()) missing.add("Cor/Raça")
            if (state.profissao.isBlank()) missing.add("Profissão")
            if (state.natural.isBlank()) missing.add("Natural de")
            if (state.ufNatural.isBlank()) missing.add("UF natural")
            if (state.endereco.isBlank()) missing.add("Endereço")
            if (state.cep.isBlank()) missing.add("CEP")
            if (state.cidade.isBlank()) missing.add("Cidade")
            if (state.uf.isBlank()) missing.add("UF")
            if (state.mostrarEstadoCivil() && state.estadoCivil == null) missing.add("Estado civil")
            if (state.mostrarConjuge() && state.nomeConjuge.isBlank()) missing.add("Nome do cônjuge")
            if (state.exibirCamposCasamentoCartorio() &&
                (state.casamentoDia.isBlank() ||
                    state.casamentoLivro.isBlank() ||
                    state.casamentoFolha.isBlank() ||
                    state.casamentoNumero.isBlank())
            ) {
                missing.add("Dados do casamento")
            }
            missing
        }
        OBITO -> {
            val missing = mutableListOf<String>()
            if (state.tipoLocalObito == null) missing.add("Tipo do local do óbito")
            if (state.dataObito.isBlank()) missing.add("Data do óbito")
            if (state.nomeLocalObito.isBlank()) missing.add("Nome do local")
            if (state.horaFalecimento.isBlank()) missing.add("Hora do óbito")
            if (state.causasMorte.none { it.isNotBlank() }) missing.add("Causa da morte")
            if (state.numeroAtestadoObito.isBlank()) missing.add("N° do atestado")
            if (state.ufMedico1.isBlank()) missing.add("UF do médico")
            if (state.crm1.isBlank()) missing.add("CRM do médico")
            if (state.medico1.isBlank()) missing.add("Nome do médico")
            missing
        }
        FILIACAO -> {
            val missing = mutableListOf<String>()
            if (state.nomePai.isBlank() && state.nomeMae.isBlank()) missing.add("Nome do pai ou da mãe")
            missing
        }
        CONJUGE_FILHOS -> emptyList()
        DECLARANTE -> {
            val missing = mutableListOf<String>()
            if (state.emailDeclarante.isBlank()) missing.add("E-mail do declarante")
            if (state.profissaoDeclarante.isBlank()) missing.add("Profissão do declarante")
            if (state.declaranteCartorio.isBlank()) missing.add("Cartório")
            if (state.ufCartorio.isBlank()) missing.add("UF do cartório")
            if (state.cidadeCartorio.isBlank()) missing.add("Cidade do cartório")
            if (state.enderecoCartorio.isBlank()) missing.add("Endereço do cartório")
            missing
        }
        OBS -> emptyList()
    }

    /** Campos recomendados ainda não preenchidos nesta seção. */
    fun recommendedMissing(state: DeclaracaoObitoFormState): List<String> = when (this) {
        FALECIDO -> emptyList()
        OBITO -> emptyList()
        FILIACAO -> {
            val missing = mutableListOf<String>()
            if (state.naturalPai.isBlank()) missing.add("Naturalidade do pai")
            if (state.ufNaturalPai.isBlank()) missing.add("UF natural do pai")
            if (state.naturalMae.isBlank()) missing.add("Naturalidade da mãe")
            if (state.ufNaturalMae.isBlank()) missing.add("UF natural da mãe")
            missing
        }
        CONJUGE_FILHOS -> emptyList()
        DECLARANTE -> emptyList()
        OBS -> emptyList()
    }

    companion object {
        /** Seções obrigatórias para liberar a geração do PDF. */
        val REQUIRED: List<DeclarationSection> = entries.filter { it.isRequired }

        /** Seções opcionais (não bloqueiam a geração do PDF). */
        val OPTIONAL: List<DeclarationSection> = entries.filterNot { it.isRequired }
    }
}
