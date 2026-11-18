package com.w_via.features.declaration

/**
 * Validações leves de formato, puramente Kotlin (multiplataforma, sem Android APIs).
 * Verificam apenas a estrutura básica do dado — não usam regras completas de
 * dígitos verificadores para não travar o fluxo rápido de preenchimento.
 */
object FieldValidators {

    private fun digits(value: String): String = value.filter { it.isDigit() }

    /** Quantidade de dígitos numéricos presentes no texto (para validação progressiva). */
    fun digitCount(value: String): Int = digits(value).length

    /** CPF com 11 dígitos numéricos. */
    fun isCpfValid(cpf: String): Boolean = digitCount(cpf) == 11

    /** CEP com 8 dígitos numéricos. */
    fun isCepValid(cep: String): Boolean = digitCount(cep) == 8

    /** Telefone/celular com 10 ou 11 dígitos (DDD + número). */
    fun isPhoneValid(phone: String): Boolean = digitCount(phone) in 10..11

    /** E-mail com usuário e domínio válidos, e um ponto no domínio. */
    fun isEmailValid(email: String): Boolean {
        if (email.isBlank()) return false
        val atIndex = email.indexOf('@')
        if (atIndex <= 0 || atIndex == email.length - 1) return false
        val domain = email.substring(atIndex + 1)
        return domain.contains('.') && domain.substringAfter('.', "").isNotBlank()
    }
}
