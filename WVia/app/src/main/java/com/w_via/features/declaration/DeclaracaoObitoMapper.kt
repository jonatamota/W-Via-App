package com.w_via.features.declaration

import com.w_via.features.declaration.state.DeclaracaoObitoFormState
import com.w_via.features.utils.AgeCalculator

/**
 * Reconstrói o estado editável do formulário a partir do modelo de domínio,
 * usado no fluxo de edição de uma declaração já salva.
 */
fun DeclaracaoObito.toFormState(): DeclaracaoObitoFormState {
    val d = this
    val f = d.falecido
    val o = d.obito
    val dec = d.declarante
    return DeclaracaoObitoFormState(
        nome = f.nome,
        cpf = f.cpf,
        rg = f.rg,
        dataNascimento = f.dataNascimento,
        idade = if (f.idade > 0) f.idade.toString() else "",
        sexo = f.sexo,
        cor = f.cor,
        profissao = f.profissao,
        natural = f.natural,
        ufNatural = f.ufNatural,
        estadoCivil = f.estadoCivil,
        nomeConjuge = f.nomeConjuge.orEmpty(),
        casamentoEmCartorio = f.casamentoEmCartorio,
        casamentoDia = f.casamentoCartorioInfo?.dia.orEmpty(),
        casamentoLivro = f.casamentoCartorioInfo?.livro.orEmpty(),
        casamentoFolha = f.casamentoCartorioInfo?.folha.orEmpty(),
        casamentoNumero = f.casamentoCartorioInfo?.numero.orEmpty(),
        deixaBens = f.deixaBens,
        testamento = f.testamento,
        cartorio = f.cartorio,
        livro = f.livro,
        folha = f.folha,
        numero = f.numero,
        endereco = f.endereco,
        cep = f.cep,
        cidade = f.cidade,
        uf = f.uf,
        inss = f.inss.orEmpty(),
        numeroBeneficio = f.numeroBeneficio.orEmpty(),
        eleitor = f.eleitor,
        eleitorCidade = f.eleitorInfo?.cidade.orEmpty(),
        eleitorUf = f.eleitorInfo?.uf.orEmpty(),
        eleitorZona = f.eleitorInfo?.zona.orEmpty(),
        eleitorSecao = f.eleitorInfo?.secao.orEmpty(),
        eleitorNumeroTitulo = f.eleitorInfo?.numeroTitulo.orEmpty(),
        reservista = f.reservista,
        reservistaCarteira = f.reservistaInfo?.carteira.orEmpty(),
        reservistaNumeroSerie = f.reservistaInfo?.numeroSerie.orEmpty(),
        reservistaCsm = f.reservistaInfo?.csm.orEmpty(),
        reservistaRm = f.reservistaInfo?.rm.orEmpty(),
        nomePai = f.filiacao.nomePai.orEmpty(),
        naturalPai = f.filiacao.naturalPai.orEmpty(),
        ufNaturalPai = f.filiacao.ufNaturalPai.orEmpty(),
        estadoCivilPai = f.filiacao.estadoCivilPai.orEmpty(),
        profissaoPai = f.filiacao.profissaoPai.orEmpty(),
        nomeMae = f.filiacao.nomeMae.orEmpty(),
        naturalMae = f.filiacao.naturalMae.orEmpty(),
        ufNaturalMae = f.filiacao.ufNaturalMae.orEmpty(),
        estadoCivilMae = f.filiacao.estadoCivilMae.orEmpty(),
        profissaoMae = f.filiacao.profissaoMae.orEmpty(),
        tipoLocalObito = TipoLocalObito.entries.firstOrNull { it.label == o.tipoLocal },
        nomeLocalObito = o.nomeLocal,
        cidadeObito = o.cidade,
        ufObito = o.uf,
        dataObito = o.dataObito,
        horaFalecimento = o.horaFalecimento,
        numeroAtestadoObito = o.numeroAtestadoObito,
        causasMorte = o.causaMorte.split(";").map { it.trim() }.filter { it.isNotBlank() }
            .ifEmpty { listOf("") },
        cemiterio = o.cemiterio,
        horaSepultamento = o.horaSepultamento,
        localVelorio = o.localVelorio,
        horaVelorio = o.horaVelorio,
        enderecoVelorio = o.endereco,
        numeroVelorio = o.numero,
        bairroVelorio = o.bairro,
        cidadeVelorio = o.cidadeVelorio,
        cepVelorio = o.cep,
        ufVelorio = o.uf2,
        medico1 = o.medico1,
        crm1 = o.crm1,
        ufMedico1 = o.ufMedico1,
        medico2 = o.medico2.orEmpty(),
        crm2 = o.crm2.orEmpty(),
        ufMedico2 = o.ufMedico2.orEmpty(),
        filhos = d.filhos.map { it.nome },
        observacoes = d.observacoes.ifBlank { "" },
        declaranteCartorio = dec.cartorio,
        declaranteTelefone = dec.telefone,
        residenciaDeclarante = dec.residencia,
        nomeDeclarante = dec.nomeDeclarante,
        grauParentesco = dec.grauParentesco,
        profissaoDeclarante = dec.profissao,
        rgDeclarante = dec.rg,
        cpfDeclarante = dec.cpf,
        bairroDeclarante = dec.bairro,
        numeroDeclarante = dec.numero,
        municipioDeclarante = dec.municipio,
        cepDeclarante = dec.cep,
        ufDeclarante = dec.uf,
        celularDeclarante = dec.celular,
        emailDeclarante = dec.email,
        enderecoCartorio = dec.enderecoCartorio,
        numeroCartorio = dec.numeroCartorio,
        ufCartorio = dec.ufCartorio,
        cidadeCartorio = dec.cidadeCartorio,
        emailCartorio = dec.emailCartorio,
        oficialCartorio = dec.oficialCartorio
    )
}

/**
 * Converte o estado editável do formulário (Strings) para os data classes finais
 * do domínio. Mantém a conversão fora das ViewModels para ser reutilizada pelo
 * fluxo por seções e pelo fluxo linear, e testável de forma isolada.
 */
fun DeclaracaoObitoFormState.toDeclaracaoObito(): DeclaracaoObito {
    val s = this
    val falecido = DadosFalecido(
        nome = s.nome,
        cpf = s.cpf,
        rg = s.rg,
        dataNascimento = s.dataNascimento,
        idade = idadeNoObito(s) ?: s.idade.toIntOrNull() ?: 0,
        sexo = s.sexo ?: Sexo.MASCULINO,
        cor = s.cor,
        profissao = s.profissao,
        natural = s.natural,
        ufNatural = s.ufNatural,
        cartorio = s.cartorio,
        livro = s.livro,
        folha = s.folha,
        numero = s.numero,
        endereco = s.endereco,
        cep = s.cep,
        cidade = s.cidade,
        uf = s.uf,
        estadoCivil = s.estadoCivil,
        nomeConjuge = s.nomeConjuge.ifBlank { null },
        casamentoEmCartorio = s.casamentoEmCartorio,
        casamentoCartorioInfo = if (s.exibirCamposCasamentoCartorio()) {
            CasamentoCartorioInfo(s.casamentoDia, s.casamentoLivro, s.casamentoFolha, s.casamentoNumero)
        } else null,
        deixaBens = s.deixaBens,
        testamento = s.testamento,
        inss = s.inss.ifBlank { null },
        numeroBeneficio = s.numeroBeneficio.ifBlank { null },
        eleitor = s.eleitor,
        eleitorInfo = if (s.exibirCamposEleitor()) {
            EleitorInfo(s.eleitorCidade, s.eleitorUf, s.eleitorZona, s.eleitorSecao, s.eleitorNumeroTitulo)
        } else null,
        reservista = s.reservista,
        reservistaInfo = if (s.exibirCamposReservista()) {
            ReservistaInfo(s.reservistaCarteira, s.reservistaNumeroSerie, s.reservistaCsm, s.reservistaRm)
        } else null,
        filiacao = Filiacao(
            nomePai = s.nomePai.ifBlank { null },
            naturalPai = s.naturalPai.ifBlank { null },
            ufNaturalPai = s.ufNaturalPai.ifBlank { null },
            estadoCivilPai = s.estadoCivilPai.ifBlank { null },
            profissaoPai = s.profissaoPai.ifBlank { null },
            nomeMae = s.nomeMae.ifBlank { null },
            naturalMae = s.naturalMae.ifBlank { null },
            ufNaturalMae = s.ufNaturalMae.ifBlank { null },
            estadoCivilMae = s.estadoCivilMae.ifBlank { null },
            profissaoMae = s.profissaoMae.ifBlank { null }
        )
    )

    val obito = DadosObito(
        tipoLocal = s.tipoLocalObito?.label ?: "",
        nomeLocal = s.nomeLocalObito,
        cidade = s.cidadeObito,
        uf = s.ufObito,
        dataObito = s.dataObito,
        horaFalecimento = s.horaFalecimento,
        numeroAtestadoObito = s.numeroAtestadoObito,
        causaMorte = s.causasMorte.filter { it.isNotBlank() }.joinToString("; "),
        cemiterio = s.cemiterio,
        horaSepultamento = s.horaSepultamento,
        localVelorio = s.localVelorio,
        horaVelorio = s.horaVelorio,
        endereco = s.enderecoVelorio,
        numero = s.numeroVelorio,
        bairro = s.bairroVelorio,
        cidadeVelorio = s.cidadeVelorio,
        cep = s.cepVelorio,
        uf2 = s.ufVelorio,
        medico1 = s.medico1,
        crm1 = s.crm1,
        ufMedico1 = s.ufMedico1,
        medico2 = s.medico2.ifBlank { null },
        crm2 = s.crm2.ifBlank { null },
        ufMedico2 = s.ufMedico2.ifBlank { null }
    )

    val declarante = Declarante(
        cartorio = s.declaranteCartorio,
        telefone = s.declaranteTelefone,
        endereco = s.residenciaDeclarante,
        nomeDeclarante = s.nomeDeclarante,
        grauParentesco = s.grauParentesco,
        profissao = s.profissaoDeclarante,
        rg = s.rgDeclarante,
        cpf = s.cpfDeclarante,
        residencia = s.residenciaDeclarante,
        bairro = s.bairroDeclarante,
        numero = s.numeroDeclarante,
        municipio = s.municipioDeclarante,
        cep = s.cepDeclarante,
        uf = s.ufDeclarante,
        celular = s.celularDeclarante,
        email = s.emailDeclarante,
        enderecoCartorio = s.enderecoCartorio,
        numeroCartorio = s.numeroCartorio,
        ufCartorio = s.ufCartorio,
        cidadeCartorio = s.cidadeCartorio,
        emailCartorio = s.emailCartorio,
        oficialCartorio = s.oficialCartorio
    )

    return DeclaracaoObito(
        falecido = falecido,
        obito = obito,
        filhos = s.filhos.filter { it.isNotBlank() }.map { Filho(it) },
        observacoes = s.observacoes.ifBlank { "Informações passadas pela declarante" },
        declarante = declarante
    )
}

/**
 * Idade completa em anos do falecido calculada tendo a data do óbito como
 * referência — não a data em que a declaração é lavrada. Se a data de óbito
 * ou a de nascimento forem inválidas/ausentes, retorna null.
 */
private fun idadeNoObito(s: DeclaracaoObitoFormState): Int? =
    AgeCalculator.parse(s.dataObito)?.let { dataObito ->
        AgeCalculator.ageOnText(s.dataNascimento, dataObito)?.years
    }
