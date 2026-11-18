package com.w_via.features.authentication.data.model

import kotlinx.serialization.Serializable

/**
 * Linha da tabela `public.users`. O `id` (bigint) é a chave usada como FK
 * nas tabelas de cadastro (`personal_data`, `professional_data`, `status_registration`).
 */
@Serializable
data class UserRowDto(
    val id: Long = 0,
    val user_id: String = "",
    val display_name: String? = null,
    val avatar_url: String? = null
)

/**
 * Linha da tabela `professional_data`. Espelha o que é gravado em
 * [com.w_via.features.authentication.data.RegistrationRepository.saveProfessionalData]
 * e é usada no pull dos dados da empresa após o login.
 */
@Serializable
data class ProfessionalDataDto(
    val user_id: Long = 0,
    val cnpj: String = "",
    val company_name: String = "",
    val image_logo: String? = null,
    val phone_prof: Long? = null,
    val email_prof: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null
)
