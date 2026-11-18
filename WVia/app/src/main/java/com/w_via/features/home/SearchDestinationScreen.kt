package com.w_via.features.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.foundation.shape.CircleShape
import com.w_via.features.home.components.AutoCompleteTextBar

@Composable
fun SearchDestinationScreen(
    navController: NavController,
    field: String = "destino",
    mapRouteViewModel: MapRouteViewModel? = null,
    viewModel: SearchDestinationViewModel = viewModel(
        factory = SearchDestinationViewModel.Factory
    )
){
    val query by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val history by viewModel.searchHistory.collectAsState()

    val origemText = mapRouteViewModel?.origemText?.collectAsState()?.value ?: ""
    val destinoText = mapRouteViewModel?.destinoText?.collectAsState()?.value ?: ""

    val showMyLocalization = origemText != "Sua localização atual" && destinoText != "Sua localização atual"

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        val placeholders = setOf("De onde?", "Para onde?", "Ponto de partida", "Sua localização atual")
        val initial = when (field) {
            "origem" -> origemText
            else -> destinoText
        }
        if (initial.isNotBlank() && initial !in placeholders) {
            viewModel.prefillQuery(initial)
        }
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    var visible by remember { mutableStateOf(false) }
    val bgAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 250)
    )

    LaunchedEffect(Unit) { visible = true }

    val backgroundColor = MaterialTheme.colorScheme.background

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(top = 20.dp)
                .background(backgroundColor)
        ) {
            // ── SearchBar com seta de voltar ──
            SearchBarExpanded(
                query = query,
                onQueryChange = { viewModel.onQueryChange(it) },
                onBack = {
                    keyboardController?.hide()
                    navController.popBackStack()
                },
                onClearQuery = { viewModel.clearQuery() },
                focusRequester = focusRequester,
                keyboardController = keyboardController,
            )

            // ── Conteúdo abaixo da barra ──
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                if (showMyLocalization) {
                    item {
                        com.w_via.features.home.components.MyLocalizationSuggestion(
                            onClick = {
                                keyboardController?.hide()
                                val suges = PlaceSuggestion(
                                    placeId = "mylocation",
                                    primaryText = "Sua localização atual",
                                    secondaryText = "Usar o GPS do dispositivo",
                                    latitude = viewModel.userLat,
                                    longitude = viewModel.userLng
                                )
                                navController.previousBackStackEntry?.savedStateHandle?.set("selected_place", suges)
                                navController.previousBackStackEntry?.savedStateHandle?.set("selected_field", field)
                                navController.popBackStack()
                            }
                        )
                    }
                }

                when (val state = uiState) {
                    is SearchUiState.Idle -> {
                        // Faz nada, o histórico já renderiza abaixo
                    }
                    is SearchUiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                    is SearchUiState.Results -> {
                        items(state.suggestions) { suggestion ->
                            AutoCompleteTextBar(
                                distance = "",
                                AdressTitle = suggestion.primaryText,
                                AdressSubtitle = suggestion.secondaryText,
                                onClick = {
                                    keyboardController?.hide()
                                    viewModel.saveToHistory(suggestion)
                                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_place", suggestion)
                                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_field", field)
                                    navController.popBackStack()
                                }
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                    is SearchUiState.Error -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                if (history.isNotEmpty()) {
                    item {
                        Text(
                            text = "Pesquisas recentes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                        )
                    }
                    items(history) { suggestion ->
                        AutoCompleteTextBar(
                            distance = "",
                            AdressTitle = suggestion.primaryText,
                            AdressSubtitle = suggestion.secondaryText,
                            onClick = {
                                keyboardController?.hide()
                                viewModel.saveToHistory(suggestion)
                                navController.previousBackStackEntry?.savedStateHandle?.set("selected_place", suggestion)
                                navController.previousBackStackEntry?.savedStateHandle?.set("selected_field", field)
                                navController.popBackStack()
                            }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun SearchBarExpanded(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onBack: () -> Unit,
    onClearQuery: () -> Unit,
    focusRequester: FocusRequester,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?
) {
    val cornerShape = RoundedCornerShape(2.dp)

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .height(50.dp)
            .clip(cornerShape)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), cornerShape)
            .background(MaterialTheme.colorScheme.background, cornerShape)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(22.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack
                    )
            )

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { keyboardController?.hide() }
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box {
                        if (query.text.isEmpty()) {
                            Text(
                                text = "Busque por um destino",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (query.text.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClearQuery
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Limpar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Results(val suggestions: List<PlaceSuggestion>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}
