package com.w_via.features.profile

data class LicenseClass(
    val project: String, // Nome do projeto (ex: "Accompanist Drawable Painter library")
    val version: String, // Versão (ex: "0.32.0")
    val licenses: List<LicenseInfo>, // Lista de licenças (pode ter múltiplas)
    val dependency: String?, // Identificador da dependência (ex: "com.google.accompanist:accompanist-drawablepainter:0.32.0")
    val description: String? = null, // Opcional
    val developers: List<String>? = null, // Opcional
    val url: String? = null, // Opcional
    val year: String? = null, // Opcional
) {
    // Classe interna para representar cada licença
    data class LicenseInfo(
        val license: String, // Nome da licença (ex: "Apache 2.0")
        val license_url: String? = null, // URL da licença (opcional)
    )

    // Helper para pegar a primeira licença (útil para exibição simples)
    fun firstLicense(): String = licenses.firstOrNull()?.license ?: "Unknown License"
}
