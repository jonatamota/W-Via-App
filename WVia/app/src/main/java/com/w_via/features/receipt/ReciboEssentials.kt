import kotlinx.datetime.LocalDate

object ReciboEssentials {

    /** Passo 1 — Contratante. */
    fun step1Blocking(
        nomeContratante: String,
        cpfCnpj: String,
        enderecoContratante: String
    ): List<String> = buildList {
        if (nomeContratante.isBlank()) add("Nome do contratante")
        if (cpfCnpj.isBlank()) add("CPF/CNPJ do contratante")
        if (enderecoContratante.isBlank()) add("Endereço do contratante")
    }

    fun step1Recommended(tel: String, email: String): List<String> = buildList {
        if (tel.isBlank()) add("Telefone do contratante")
        if (email.isBlank()) add("E-mail do contratante")
    }

    /** Passo 2 — Falecido. CPF ou RG (pelo menos um) e filiação (pai ou mãe). */
    fun step2Blocking(
        nomeFalecido: String,
        cpfFalecido: String,
        rgFalecido: String,
        naturalidadeFalecido: String,
        ufFalecido: String,
        paiFalecido: String,
        maeFalecido: String
    ): List<String> = buildList {
        if (nomeFalecido.isBlank()) add("Nome do falecido")
        if (cpfFalecido.isBlank() && rgFalecido.isBlank()) add("CPF ou RG do falecido")
        if (naturalidadeFalecido.isBlank()) add("Naturalidade do falecido")
        if (ufFalecido.isBlank()) add("UF do falecido")
        if (paiFalecido.isBlank() && maeFalecido.isBlank()) add("Filiação do falecido (pai ou mãe)")
    }

    fun step2Recommended(idadeFalecido: String, sexoFalecido: String): List<String> = buildList {
        if (idadeFalecido.isBlank()) add("Idade do falecido")
        if (sexoFalecido.isBlank()) add("Sexo do falecido")
    }

    /** Passo 3 — Funeral. */
    fun step3Blocking(
        localFuneral: String,
        diaFuneral: LocalDate?,
        cemiterioSepultamento: String
    ): List<String> = buildList {
        if (localFuneral.isBlank()) add("Local de saída do funeral")
        if (diaFuneral == null) add("Dia do funeral")
        if (cemiterioSepultamento.isBlank()) add("Cemitério")
    }

    fun step3Recommended(horaSaidaFuneral: String): List<String> = buildList {
        if (horaSaidaFuneral.isBlank()) add("Hora de saída do funeral")
    }

    /** Passo 4 — Serviço/Pagamento. */
    fun step4Blocking(
        descricao: String,
        totalPago: Double,
        dataPagamento: String
    ): List<String> = buildList {
        if (descricao.isBlank()) add("Descrição do serviço")
        if (totalPago <= 0.0) add("Valor total (maior que zero)")
        if (dataPagamento.isBlank()) add("Data de pagamento")
    }

    fun step4Recommended(): List<String> = emptyList()
}
