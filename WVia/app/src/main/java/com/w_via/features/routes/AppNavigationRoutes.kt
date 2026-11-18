package com.w_via.features.routes

object Routes {
    const val HOME = "home"
    const val POLITICA_PRIVACIDADE = "politica_privacidade"
    const val MEUS_RECIBOS = "meus_recibos"
    const val RECIBO_SUCESSO = "recibo_sucesso"
    const val RECIBO_SUCESSO_ARG = "recibo_sucesso?reciboId={reciboId}"
    const val RECIBO_DETALHE = "recibo_detalhe"
    const val RECIBO_DETALHE_ARG = "recibo_detalhe?reciboId={reciboId}"
    const val DECLARATION = "declaration"
    const val CREATE_DECLARATION = "create_declaration"
    const val DECLARATION_GRID = "Declaration_grid"
    const val DECLARACAO_SUCESSO = "declaracao_sucesso"
    const val DECLARACAO_DETALHE = "declaracao_detalhe"
    const val DECLARACAO_DETALHE_ARG = "declaracao_detalhe?declaracaoId={declaracaoId}"

    // Rotas de seções da Declaração de Óbito
    const val DECLARATION_FALECIDO = "declaracao/dados_falecido"
    const val DECLARATION_OBITO = "declaracao/dados_obito"
    const val DECLARATION_FILIACAO = "declaracao/filiacao"
    const val DECLARATION_CONJUGE_FILHOS = "declaracao/conjuge_filhos"
    const val DECLARATION_DECLARANTE = "declaracao/declarante"
    const val DECLARATION_OBS = "declaracao/obs"
    const val MYCNPJ = "mycnpj"
    const val UPLOAD_FILE = "upload_file"
    const val SEARCH_DESTINATION = "search_destination"

    const val LOGIN = "login"
    const val FINALIZAR_CONTA = "finalizar_conta"
    const val TELA_START = "tela_start"
    const val SPLASH = "splash"

    // Rotas de Recibos
    const val PDF = "pdf/{reciboId}"
    const val CRIAR_RECIBO = "criar_recibo"

    // Rotas de Perfil
    const val PERFIL = "perfil"
    const val DADOS_PESSOAIS = "dados_pessoais"
    const val INFORMACOES_PROFISSIONAIS = "informacoes_profissionais"
    const val TERMOS_USO = "termos_uso"
    const val LICENCAS = "licencas"

    // Rota da tela de testes (apenas debug)
    const val TESTE = "teste"

}
