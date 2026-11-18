package com.w_via.features.receipt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.w_via.R
import com.w_via.designsystem.appbuttons.ActionButton
import com.w_via.designsystem.bottomsheets.DeleteConfirmationBottomSheet
import com.w_via.features.appnavigation.TopBar
import com.w_via.features.receipt.components.ReciboItem
import com.w_via.features.receipt.components.SelectionActionsBar
import com.w_via.features.receipt.data.ReciboRepository
import com.w_via.features.routes.Routes
import com.w_via.features.sync.SyncManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReceiptScreen(
    bottomBarPadding: Dp,
    navController: NavController,
) {
    val repository: ReciboRepository = koinInject()
    val syncManager: SyncManager = koinInject()
    val savedRecibos by repository.observeSaved().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var selectionMode by rememberSaveable { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }

    fun exitSelection() {
        selectionMode = false
        selectedIds = emptySet()
    }

    fun refresh() {
        if (isRefreshing) return
        scope.launch {
            isRefreshing = true
            val result = syncManager.syncRecibos()
            android.util.Log.d("MyReceiptScreen", "refresh -> $result")
            isRefreshing = false
        }
    }

    fun confirmDelete() {
        scope.launch {
            repository.deleteByIds(selectedIds.toList())
            exitSelection()
        }
        showDeleteConfirm = false
    }

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        topBar = {
            Column {
                TopBar(title = "Meus Recibos")
                if (selectionMode) {
                    SelectionActionsBar(
                        selectedCount = selectedIds.size,
                        onExitSelection = ::exitSelection,
                        onDelete = { showDeleteConfirm = true },
                        deleteEnabled = selectedIds.isNotEmpty()
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = ::refresh,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(10.dp)
            ) {
            if (savedRecibos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Você ainda não criou nenhum recibo",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(savedRecibos, key = { it.id }) { recibo ->
                        ReciboItem(
                            nomeContratante = recibo.nomeContratante,
                            dataEmissao = recibo.dataPagamento,
                            valorFormatado = recibo.totalPago,
                            obsolete = recibo.isObsolete,
                            selectionMode = selectionMode,
                            isSelected = recibo.id in selectedIds,
                            onToggleSelect = {
                                selectedIds = if (recibo.id in selectedIds) {
                                    selectedIds - recibo.id
                                } else {
                                    selectedIds + recibo.id
                                }
                            },
                            onItemLongClick = {
                                if (selectionMode && selectedIds == setOf(recibo.id)) {
                                    exitSelection()
                                } else {
                                    selectionMode = true
                                    selectedIds = selectedIds + recibo.id
                                }
                            },
                            onItemClick = {
                                if (selectionMode) {
                                    selectedIds = if (recibo.id in selectedIds) {
                                        selectedIds - recibo.id
                                    } else {
                                        selectedIds + recibo.id
                                    }
                                } else {
                                    navController.navigate("${Routes.RECIBO_DETALHE}?reciboId=${recibo.id}")
                                }
                            }
                        )
                    }
                }
            }

            if (!selectionMode) {
                ActionButton(
                    onClose = { navController.navigate(Routes.CRIAR_RECIBO) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = bottomBarPadding + 16.dp, end = 16.dp),
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    iconColor = MaterialTheme.colorScheme.onPrimary,
                    text = "Criar",
                    icon = { modifier, tint ->
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add_2_24dp),
                            contentDescription = "Criar Recibo",
                            tint = tint,
                            modifier = modifier
                        )
                    }
                )
            }
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteConfirmationBottomSheet(
            onDismiss = { showDeleteConfirm = false },
            onConfirm = ::confirmDelete,
            title = "Excluir recibo?",
            message = if (selectedIds.size == 1) {
                "1 recibo será excluído."
            } else {
                "${selectedIds.size} recibos serão excluídos."
            }
        )
    }
}
