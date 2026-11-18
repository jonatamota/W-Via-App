package com.w_via.features.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray

class LicenseViewModel : ViewModel() {
    private val _licenses = MutableStateFlow<List<LicenseClass>>(emptyList())
    val licenses: StateFlow<List<LicenseClass>> = _licenses

    fun loadLicenses(context: Context) {
        viewModelScope.launch {
            val jsonString = context.assets
                .open("open_source_licenses.json")
                .bufferedReader()
                .use { it.readText() }

            val licensesList = parseLicenses(jsonString)
            _licenses.value = licensesList
        }
    }

    private fun parseLicenses(jsonString: String): List<LicenseClass> {
        return try {
            val jsonArray = JSONArray(jsonString)
            List(jsonArray.length()) { i ->
                val item = jsonArray.getJSONObject(i)
                LicenseClass(
                    project = item.getString("project"),
                    version = item.optString("version"),
                    licenses = parseLicenseInfo(item.getJSONArray("licenses")),
                    dependency = item.optString("dependency").takeIf { it.isNotEmpty() },
                    description = item.optString("description").takeIf { it.isNotEmpty() },
                    developers = parseStringList(item.optJSONArray("developers")),
                    url = item.optString("url").takeIf { it.isNotEmpty() },
                    year = item.optString("year").takeIf { it.isNotEmpty() },
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseLicenseInfo(licensesArray: JSONArray): List<LicenseClass.LicenseInfo> {
        return List(licensesArray.length()) { i ->
            val license = licensesArray.getJSONObject(i)
            LicenseClass.LicenseInfo(
                license = license.getString("license"),
                license_url = license.optString("license_url").takeIf { it.isNotEmpty() },
            )
        }
    }

    private fun parseStringList(array: JSONArray?): List<String>? {
        return array?.let {
            List(it.length()) { i -> it.getString(i) }
        }
    }
}
