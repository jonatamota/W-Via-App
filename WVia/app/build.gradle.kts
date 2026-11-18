@file:Suppress("DEPRECATION")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.jaredsburrows.license")
    id("androidx.room")
    id("kotlin-parcelize")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
}

val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        load(FileInputStream(localFile))
    }
}

val RELEASE_STORE_FILE: String by localProperties
val RELEASE_STORE_PASSWORD: String by localProperties
val RELEASE_KEY_ALIAS: String by localProperties
val RELEASE_KEY_PASSWORD: String by localProperties


room {
    schemaDirectory("$projectDir/schemas")
}

android {
    namespace = "com.w_via"
    //noinspection GradleDependency
    compileSdk = 37

    defaultConfig {
        manifestPlaceholders += mapOf()
        applicationId = "com.w_via"
        minSdk = 27

        //noinspection EditedTargetSdkVersion
        targetSdk = 37
        versionCode = 7
        versionName = "1.1.4 (beta)"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
        manifestPlaceholders["HERE_API_KEY"] =
            localProperties.getProperty("HERE_API_KEY") as String

        manifestPlaceholders["GRAPHHOPPER_API_KEY"] =
            localProperties.getProperty("GRAPHHOPPER_API_KEY") as String

        manifestPlaceholders["SUPABASE_ANON_KEY"] =
            localProperties.getProperty("SUPABASE_ANON_KEY") as String

        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"${localProperties.getProperty("SUPABASE_URL")}\""
        )
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"${localProperties.getProperty("SUPABASE_ANON_KEY")}\""
        )
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${localProperties.getProperty("GOOGLE_WEB_CLIENT_ID") ?: ""}\""
        )

    }

    // Configuração de assinatura
    signingConfigs {
        getByName("debug") {
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("release") {
            storeFile = file(RELEASE_STORE_FILE)
            storePassword = RELEASE_STORE_PASSWORD
            keyAlias = RELEASE_KEY_ALIAS
            keyPassword = RELEASE_KEY_PASSWORD
        }
    }

    buildFeatures {
        buildConfig = true
        resValues = true
        compose = true
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

licenseReport {
    generateCsvReport = false
    generateHtmlReport = true
    generateJsonReport = true
    generateTextReport = false
    copyCsvReportToAssets = true
    copyHtmlReportToAssets = false
    copyJsonReportToAssets = true
    copyTextReportToAssets = false
}

afterEvaluate {
    tasks.named("assembleDebug") {
        dependsOn("licenseDebugReport")
    }
    tasks.named("assembleRelease") {
        dependsOn("licenseReleaseReport")
    }
}


dependencies {
    // Android compose
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.navigation.runtime.ktx)

    // Visualização e geração de PDF
    implementation(libs.pdf.kmp)
    implementation(libs.compose.pdf)

    // Google Play Services - Core Credential Manager
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Componentes de layout do Compose
    implementation(libs.androidx.compose.foundation.layout)

    // API de pagamento Google Play Billing
    implementation(libs.billing)
    implementation(libs.billing.ktx)
    implementation(libs.androidx.core.ktx)

    // Implementação DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.compose.foundation)

    // Implementação Armazenamento Interno - Room Database
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.rxjava2)
    implementation(libs.androidx.room.rxjava3)
    implementation(libs.androidx.room.guava)
    testImplementation(libs.androidx.room.testing)
    implementation(libs.androidx.room.paging)

    // Gerenciador de licencas
    implementation(libs.wsdl4j)
    implementation(libs.android.gif.drawable)

    // Tela Splash
    implementation(libs.androidx.core.splashscreen)

    // Context e ciclo de vida do Android
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.integrity)

    // Ingeção de dependências
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Requisições HTTP
    implementation(libs.ktor.client.android)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Dependências do BOM
    implementation(platform(libs.firebase.bom))
    implementation(platform(libs.androidx.compose.bom))

    // CardView e outros componentes AndroidX
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.appcompat)

    // Compose media3
    implementation(libs.androidx.media3.common.ktx)

    // Dependências principais do Compose
    implementation(libs.androidx.compose.ui.ui)
    implementation(libs.material3)
    implementation(libs.androidx.compose.ui.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.ui.tooling)

    // Ícones adicionais do Material Design para Compose
    implementation(libs.androidx.material.icons.core)
    implementation(libs.material.icons.extended)

    // Google Servico de Localizacao
    implementation(libs.play.services.location)

    // Api de mapas do MapLibre
    implementation(libs.android.sdk)

    // Google Sign-In - Usando versões compatíveis com Firebase BOM
    implementation(libs.play.services.auth.v2060)

    // Adicionando dependências explícitas com versões compatíveis
    implementation(libs.play.services.base)
    implementation(libs.play.services.basement)
    implementation(libs.play.services.identity)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Accompanist
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.swiperefresh)
    implementation(libs.accompanist.pager.v0301)
    implementation(libs.accompanist.pager.indicators)

    // Outras dependências
    implementation(libs.coil.compose)
    implementation(libs.navigation.compose)

    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Library for blur
    implementation(libs.haze)

    // Supabase dependencies
    implementation(libs.storage.kt)
    implementation(libs.postgrest.kt)
    implementation(libs.auth.kt)
    implementation(libs.functions.kt)
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/config/detekt.yml"))
    parallel = true
    baseline = file("$projectDir/config/detekt/baseline.xml")
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}

ktlint {
    android.set(true)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    baseline.set(file("$projectDir/config/ktlint/baseline.xml"))
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML)
    }
}
