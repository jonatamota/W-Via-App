/**
 * Valida se um CPF é válido
 * @param cpf String contendo o CPF (pode ter ou não formatação)
 * @return Boolean indicando se o CPF é válido
 */
fun isValidCPF(cpf: String): Boolean {
    // Remove formatação (pontos, hífens e espaços)
    val cleanCPF = cpf.replace(Regex("[^0-9]"), "")

    // Verifica se tem exatamente 11 dígitos
    if (cleanCPF.length != 11) {
        return false
    }

    // Verifica se não são todos os dígitos iguais (000.000.000-00, 111.111.111-11, etc.)
    if (cleanCPF.all { it == cleanCPF[0] }) {
        return false
    }

    // Converte para lista de inteiros
    val digits = cleanCPF.map { it.toString().toInt() }

    // Calcula o primeiro dígito verificador
    val firstDigit = calculateDigit(digits.take(9), intArrayOf(10, 9, 8, 7, 6, 5, 4, 3, 2))

    // Calcula o segundo dígito verificador (inclui o primeiro dígito calculado)
    val secondDigit = calculateDigit(digits.take(9) + firstDigit, intArrayOf(11, 10, 9, 8, 7, 6, 5, 4, 3, 2))

    // Verifica se os dígitos calculados conferem com os informados
    return firstDigit == digits[9] && secondDigit == digits[10]
}

/**
 * Calcula um dígito verificador do CPF
 * @param digits Lista com os dígitos a serem multiplicados
 * @param multipliers Array com os multiplicadores
 * @return Dígito verificador calculado
 */
private fun calculateDigit(digits: List<Int>, multipliers: IntArray): Int {
    val sum = digits.mapIndexed { index, digit ->
        digit * multipliers[index]
    }.sum()

    val remainder = sum % 11

    return if (remainder < 2) 0 else 11 - remainder
}

/**
 * Função auxiliar para obter a região fiscal do CPF
 * @param cpf String contendo o CPF
 * @return String com a descrição da região fiscal ou null se inválido
 */
fun getCPFRegion(cpf: String): String? {
    val cleanCPF = cpf.replace(Regex("[^0-9]"), "")

    if (cleanCPF.length != 11) {
        return null
    }

    return when (cleanCPF[8].toString().toInt()) {
        1 -> "DF, GO, MS, MT, TO"
        2 -> "AC, AM, AP, PA, RO, RR"
        3 -> "CE, MA, PI"
        4 -> "AL, PB, PE, RN"
        5 -> "BA, SE"
        6 -> "MG"
        7 -> "ES, RJ"
        8 -> "SP"
        9 -> "PR, SC"
        0 -> "RS"
        else -> "Região inválida"
    }
}

// Função de exemplo/teste
fun main() {
    val testCPFs = arrayOf(
        "123.456.789-09",
        "000.000.000-00",
        "111.111.111-11",
        "12345678909",
        "123.456.789-10",
        "041.761.170-60"
    )

    println("=== TESTE DE VALIDAÇÃO DE CPF ===")
    testCPFs.forEach { cpf ->
        val isValid = isValidCPF(cpf)
        val region = if (isValid) getCPFRegion(cpf) else "N/A"
        println("CPF: $cpf")
        println("Válido: ${if (isValid) "✓ SIM" else "✗ NÃO"}")
        println("Região: $region")
        println("---")
    }
}