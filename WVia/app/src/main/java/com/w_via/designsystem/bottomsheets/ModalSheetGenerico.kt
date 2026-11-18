package com.w_via.designsystem.bottomsheets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w_via.designsystem.appbuttons.CloseButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheetWrapper(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onClose: () -> Unit,
    sheetContent: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheetWrapper(
        isVisible = isVisible,
        onDismiss = onDismiss,
        title = null,
        onClose = onClose,
        sheetContent = sheetContent,
        showCloseButton = true,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheetWrapper(
    isVisible: Boolean,
    title: String? = null,
    onDismiss: () -> Unit,
    onClose: () -> Unit,
    sheetContent: @Composable ColumnScope.() -> Unit,
    showCloseButton: Boolean = true
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(isVisible) {
        if (!isVisible && sheetState.isVisible) {
            sheetState.hide()
            onClose()
        }
    }

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                    onDismiss()
                    onClose()
                }
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Título — sempre exibido se não for nulo
                    if (!title.isNullOrEmpty()) {
                        Text(
                            text = title,
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(2f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Botão close — opcional
                    if (showCloseButton) {
                        CloseButton(
                            size = 40.dp,
                            onClose = {
                                scope.launch {
                                    sheetState.hide()
                                    onDismiss()
                                    onClose()
                                }
                            }
                        )
                    }
                }
                sheetContent()
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
