package com.w_via.features.datamycnpj

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import java.io.File

object LogoStorage {
    private const val PREFS_NAME = "wvia_logo_prefs"
    private const val KEY_LOGO_URI = "logo_uri"

    fun save(context: Context, uri: Uri) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(KEY_LOGO_URI, uri.toString())
            }
    }

    fun load(context: Context): Uri? {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LOGO_URI, null) ?: return null
        return Uri.parse(raw)
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                remove(KEY_LOGO_URI)
            }
    }
}

/** Indica se a URI da logo corresponde a um arquivo realmente disponível. */
fun logoDisponivel(context: Context, uri: Uri?): Boolean {
    if (uri == null) return false
    return runCatching {
        if (uri.scheme == "file") {
            File(uri.path.orEmpty()).exists()
        } else {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { true } ?: false
        }
    }.getOrDefault(false)
}