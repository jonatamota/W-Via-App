package com.w_via.features.utils

/**
 * Utilitários para conversão de valores monetários
 */
object CurrencyUtils {
    
    /**
     * Converte uma string de valor para centavos (Long)
     * @param value String do valor (ex: "10.50")
     * @return Valor em centavos (ex: 1050)
     */
    fun stringToCents(value: String): Long {
        return ((value.toDoubleOrNull() ?: 0.0) * 100.0).toLong()
    }
    
    /**
     * Converte centavos (Long) para string de valor
     * @param cents Valor em centavos (ex: 1050)
     * @return String do valor (ex: "10.5")
     */
    fun centsToString(cents: Long): String {
        return (cents / 100.0).toString()
    }
}
