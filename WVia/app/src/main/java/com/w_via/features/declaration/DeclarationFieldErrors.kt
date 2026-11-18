package com.w_via.features.declaration

/**
 * Mensagens de erro de formato por campo, puramente Kotlin (multiplataforma, testável).
 *
 * A validação é "progressiva": campos numéricos (CPF, CEP, telefone) só exibem erro
 * depois que o usuário digitou a quantidade mínima esperada, para não incomodar
 * durante a digitação. Campos vazios nunca exibem erro — a obrigatoriedade é tratada
 * pela completude da seção ([DeclarationSection.isComplete]).
 */
object DeclarationFieldErrors {

    /** Erro de CPF, ou null se vazio / ainda em digitação / válido. */
    fun cpf(value: String): String? =
        if (value.isNotBlank() && FieldValidators.digitCount(value) >= 11 && !FieldValidators.isCpfValid(value)) {
            "CPF inválido — informe os 11 dígitos"
        } else {
            null
        }

    /** Erro de CEP, ou null se vazio / ainda em digitação / válido. */
    fun cep(value: String): String? =
        if (value.isNotBlank() && FieldValidators.digitCount(value) >= 8 && !FieldValidators.isCepValid(value)) {
            "CEP inválido — informe os 8 dígitos"
        } else {
            null
        }

    /** Erro de telefone/celular, ou null se vazio / ainda em digitação / válido. */
    fun phone(value: String): String? =
        if (value.isNotBlank() && FieldValidators.digitCount(value) >= 10 && !FieldValidators.isPhoneValid(value)) {
            "Telefone inválido — informe DDD + número"
        } else {
            null
        }

    /** Erro de e-mail, ou null se vazio / válido. */
    fun email(value: String): String? =
        if (value.isNotBlank() && !FieldValidators.isEmailValid(value)) {
            "E-mail inválido"
        } else {
            null
        }
}
