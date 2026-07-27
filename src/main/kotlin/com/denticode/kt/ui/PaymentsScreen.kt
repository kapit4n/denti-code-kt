package com.denticode.kt.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.Patient
import com.denticode.kt.data.PaymentRow
import com.denticode.kt.data.ProcedureTypeRow
import com.denticode.kt.ui.components.feedback.LoadingIndicator
import com.denticode.kt.ui.payments.ModernPaymentsContent
import com.denticode.kt.ui.payments.PaymentsNewPaymentDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PaymentsScreen(
    repo: DentiRepository,
    onOpenPatient: (Patient) -> Unit = {},
) {
    var paymentRows by remember { mutableStateOf<List<PaymentRow>>(emptyList()) }
    var patients by remember { mutableStateOf<List<Patient>>(emptyList()) }
    var procedureTypes by remember { mutableStateOf<List<ProcedureTypeRow>>(emptyList()) }
    var showNewPayment by remember { mutableStateOf(false) }
    var saveBusy by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var loaded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    suspend fun reload() {
        withContext(Dispatchers.IO) {
            paymentRows = repo.listRecentPayments(500)
            patients = repo.listPatients()
            procedureTypes = repo.listProcedureTypes()
        }
        loaded = true
    }

    LaunchedEffect(Unit) {
        reload()
    }

    if (!loaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingIndicator()
        }
    } else {
        ModernPaymentsContent(
            paymentRows = paymentRows,
            onNewPaymentClick = {
                saveError = null
                showNewPayment = true
            },
            onOpenPatient = { patientId ->
                patients.find { it.id == patientId }?.let(onOpenPatient)
            },
        )
    }

    if (showNewPayment) {
        PaymentsNewPaymentDialog(
            patients = patients,
            procedureTypes = procedureTypes,
            treatmentOptionsForPatient = { patientId ->
                repo.listTreatmentPaymentOptionsForPatient(patientId)
            },
            isSaving = saveBusy,
            errorMessage = saveError,
            onDismiss = {
                if (!saveBusy) {
                    showNewPayment = false
                    saveError = null
                }
            },
            onSubmit = { patientId, request ->
                saveBusy = true
                saveError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.registerPaymentForPatient(patientId, request)
                        }
                        reload()
                    }.onSuccess {
                        showNewPayment = false
                    }.onFailure { e ->
                        saveError = e.message ?: "No se pudo registrar el pago."
                    }
                    saveBusy = false
                }
            },
        )
    }
}
