package com.w_via.features.maps.hereAPI

import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AddressSearchField(
    value: String,
    label: String,
    leadingIcon: @Composable () -> Unit,
    onAddressSelected: (HereSuggestion) -> Unit,
    userLat: Double = -23.5505,
    userLng: Double = -46.6333,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val apiKey = context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        .metaData.getString("HERE_API_KEY") ?: ""

    val service = remember { HereAutocompleteService(apiKey) }
    var query by remember { mutableStateOf(value) }
    var suggestions by remember { mutableStateOf<List<HereSuggestion>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        OutlinedTextField(
            shape = RoundedCornerShape(16.dp),
            value = query,
            onValueChange = { input ->
                query = input
                if (input.length >= 3) {
                    scope.launch {
                        suggestions = service.autocomplete(input, userLat, userLng)
                        expanded = suggestions.isNotEmpty()
                    }
                } else {
                    expanded = false
                }
            },
            label = { Text(label) },
            leadingIcon = leadingIcon,
            modifier = Modifier.fillMaxWidth()
        )

        if (expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column {
                    suggestions.forEach { suggestion ->
                        ListItem(
                            headlineContent = { Text(suggestion.title) },
                            supportingContent = { Text(suggestion.address, maxLines = 1) },
                            modifier = Modifier.clickable {
                                query = suggestion.title
                                expanded = false
                                onAddressSelected(suggestion)
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}