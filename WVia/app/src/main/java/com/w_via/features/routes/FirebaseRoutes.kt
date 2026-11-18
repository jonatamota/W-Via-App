package com.w_via.features.routes

/**
 * Constantes para coleções, documentos e campos do Firestore
 */

object FirestoreCollections {
    const val USUARIOS = "users"
    const val RECIBOS = "receipt"
    const val PREFERENCIAS = "preferences"
}

object FirestoreMap {
    // Documentos em subcoleções
    const val PROF_DATA = "prof_data"

    // Mapas no documento do usuário
    const val DADOS_PESSOAIS = "personal_data"
    const val CREDENTIALS = "credentials"
    const val STATUS_ACCOUNT = "status_account"

    // Campos raiz do usuário
    const val EMAIL = "email"
    const val DISPLAY_NAME = "displayName"
    const val NAME = "name"

    // Campos de credentials
    const val ACCOUNT_TIME = "account_time"

    // Campos de status_account
    const val REGISTER_COMPLETE = "register_complete"
    const val FIRST_STEPS = "first_steps"

    // Campos de personal_data
    const val BIRTHDAY = "birthday"
    const val PHONE = "phone"
    const val SEX = "sex"
    const val ACCOUNT_TIME_PERSONAL = "accountTime"
    const val AGE = "age"

    // Campos de prof_data
    const val CNPJ = "cnpj"
    const val TRADE_NAME = "trade_name"
}

object FirestorePaths {
    const val STATUS_REGISTER_COMPLETE =
        "${FirestoreMap.STATUS_ACCOUNT}.${FirestoreMap.REGISTER_COMPLETE}"
    const val STATUS_FIRST_STEPS =
        "${FirestoreMap.STATUS_ACCOUNT}.${FirestoreMap.FIRST_STEPS}"
}
