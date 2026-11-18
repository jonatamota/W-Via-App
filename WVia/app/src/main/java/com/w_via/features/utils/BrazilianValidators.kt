package com.w_via.features.utils

/**
 * Validadores de documentos e contato brasileiros.
 *
 * As funções aceitam entradas com ou sem formatação (pontos, hífens, espaços,
 * parênteses etc.), pois os dados são normalizados antes da validação.
 */
object BrazilianValidators {

    private const val MAX_EMAIL_LENGTH = 254

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    private fun digits(value: String): String = value.filter { it.isDigit() }

    private fun List<Int>.checkDigit(): Int {
        var weight = size + 1
        var sum = 0
        for (digit in this) {
            sum += digit * weight
            weight--
        }
        val remainder = sum % 11
        return if (remainder < 2) 0 else 11 - remainder
    }

    /** CPF válido: 11 dígitos, dígitos verificadores corretos e dígitos não repetidos. */
    fun isCpfValid(cpf: String): Boolean {
        val clean = digits(cpf)
        if (clean.length != 11) return false
        if (clean.all { it == clean.first() }) return false

        val digitsList = clean.map { it - '0' }
        if (digitsList.take(9).checkDigit() != digitsList[9]) return false
        if (digitsList.take(10).checkDigit() != digitsList[10]) return false
        return true
    }

    /**
     * RG válido: 7 a 11 caracteres alfanuméricos, todos dígitos com exceção do
     * último, que pode ser um dígito ou a letra X (formato usado por vários estados).
     */
    fun isRgValid(rg: String): Boolean {
        val normalized = rg.filter { it.isLetterOrDigit() }.uppercase()
        if (normalized.length !in 7..11) return false

        val body = normalized.dropLast(1)
        if (body.isEmpty() || !body.all { it.isDigit() }) return false

        val check = normalized.last()
        return check.isDigit() || check == 'X'
    }

    /**
     * Telefone/celular válido: 10 (fixo) ou 11 (celular) dígitos, DDD entre 11 e 99.
     * Aceita também o prefixo internacional `+55`.
     */
    fun isPhoneValid(phone: String): Boolean {
        var clean = digits(phone)
        if (clean.startsWith("55") && clean.length == 13) clean = clean.drop(2)
        if (clean.length !in 10..11) return false
        if (clean.all { it == clean.first() }) return false

        val ddd = clean.take(2).toIntOrNull() ?: return false
        if (ddd !in 11..99) return false

        if (clean.length == 11 && clean[2] != '9') return false
        return true
    }

    /** E-mail válido: formato básico e comprimento máximo de 254 caracteres. */
    fun isEmailValid(email: String): Boolean {
        if (email.isBlank() || email.length > MAX_EMAIL_LENGTH) return false
        return EMAIL_REGEX.matches(email)
    }
}
