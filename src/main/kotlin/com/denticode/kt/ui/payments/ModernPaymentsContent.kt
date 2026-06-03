package com.denticode.kt.ui.payments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.PaymentDisplayStatus
import com.denticode.kt.data.PaymentMethod
import com.denticode.kt.data.PaymentRow
import com.denticode.kt.ui.app.LocalAppMessenger
import java.time.LocalDate

@Composable
fun ModernPaymentsContent(
    paymentRows: List<PaymentRow>,
    onNewPaymentClick: () -> Unit,
    onOpenPatient: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val messenger = LocalAppMessenger.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var selectedStatus by remember { mutableStateOf<PaymentDisplayStatus?>(null) }
    var filterDate by remember { mutableStateOf<LocalDate?>(null) }
    var currentPage by remember { mutableIntStateOf(1) }
    var pageSize by remember { mutableIntStateOf(10) }
    var selectedPaymentId by remember { mutableStateOf<Int?>(null) }

    val methodOptions =
        remember {
            listOf("Todos los métodos") + PaymentMethod.entries.map { it.displayLabel }
        }
    val statusOptions =
        remember {
            listOf("Todos los estados") + PaymentDisplayStatus.entries.map { it.labelEs }
        }

    val uiState =
        remember(
            paymentRows,
            searchQuery,
            selectedMethod,
            selectedStatus,
            filterDate,
            currentPage,
            pageSize,
        ) {
            buildPaymentsUiState(
                rows = paymentRows,
                searchQuery = searchQuery,
                selectedMethod = selectedMethod,
                selectedStatus = selectedStatus,
                filterDate = filterDate,
                currentPage = currentPage,
                pageSize = pageSize,
            )
        }

    val pageItems = remember(uiState) { paginatedPayments(uiState) }
    val totalPages = remember(uiState) { totalPaymentPages(uiState) }
    val rangeStart =
        if (uiState.totalItems == 0) {
            0
        } else {
            (uiState.currentPage - 1) * uiState.pageSize + 1
        }
    val rangeEnd = (rangeStart + pageItems.size - 1).coerceAtLeast(0)
    val hasFilters =
        searchQuery.isNotBlank() ||
            selectedMethod != null ||
            selectedStatus != null ||
            filterDate != null

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        PaymentsPageHeader(onNewPaymentClick = onNewPaymentClick)
        PaymentsKpiRow(
            totalCollected = uiState.totalCollected,
            completedCount = uiState.completedCount,
            pendingCount = uiState.pendingCount,
            averageAmount = uiState.averageAmount,
        )
        PaymentsFilterToolbar(
            searchQuery = searchQuery,
            onSearchChange = {
                searchQuery = it
                currentPage = 1
            },
            methodLabel = selectedMethod?.displayLabel ?: "Todos los métodos",
            methodOptions = methodOptions,
            onMethodSelect = { label ->
                selectedMethod =
                    if (label == "Todos los métodos") {
                        null
                    } else {
                        PaymentMethod.entries.firstOrNull { it.displayLabel == label }
                    }
                currentPage = 1
            },
            statusLabel = selectedStatus?.labelEs ?: "Todos los estados",
            statusOptions = statusOptions,
            onStatusSelect = { label ->
                selectedStatus =
                    if (label == "Todos los estados") {
                        null
                    } else {
                        PaymentDisplayStatus.entries.firstOrNull { it.labelEs == label }
                    }
                currentPage = 1
            },
            filterDate = filterDate,
            onFilterDateChange = {
                filterDate = it
                currentPage = 1
            },
            onClearDateFilter = {
                filterDate = null
                currentPage = 1
            },
            onExport = { messenger.showSuccess("Exportación de pagos próximamente.") },
        )
        if (pageItems.isEmpty()) {
            EmptyPaymentsState(hasFilters = hasFilters, modifier = Modifier.weight(1f))
        } else {
            PaymentTable(
                payments = pageItems,
                selectedId = selectedPaymentId,
                onSelect = { selectedPaymentId = it.id },
                onViewDetail = { onOpenPatient(it.patientId) },
                modifier = Modifier.weight(1f),
            )
        }
        PaymentsFooterBar(
            rangeStart = rangeStart,
            rangeEnd = rangeEnd,
            totalItems = uiState.totalItems,
            totalCollected = uiState.totalCollected,
            pendingTotal = uiState.pendingTotal,
            currentPage = uiState.currentPage,
            totalPages = totalPages,
            pageSize = uiState.pageSize,
            pageSizeOptions = listOf(5, 10, 20, 50),
            onPageChange = { currentPage = it },
            onPageSizeChange = {
                pageSize = it
                currentPage = 1
            },
        )
    }
}
