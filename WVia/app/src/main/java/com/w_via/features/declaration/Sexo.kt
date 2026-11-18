package com.w_via.features.declaration

import kotlinx.serialization.Serializable

@Serializable
enum class Sexo { MASCULINO, FEMININO }

@Serializable
enum class EstadoCivil {
    SOLTEIRO, CASADO, DIVORCIADO, VIUVO, UNIAO_ESTAVEL
}