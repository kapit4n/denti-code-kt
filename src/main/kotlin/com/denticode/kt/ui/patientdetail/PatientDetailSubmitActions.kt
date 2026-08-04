package com.denticode.kt.ui.patientdetail

import com.denticode.kt.data.AppointmentVisitRequest
import com.denticode.kt.data.DentalRecordRegisterRequest
import com.denticode.kt.data.DentalRecordUpdateRequest
import com.denticode.kt.data.FollowUpRegisterRequest
import com.denticode.kt.data.MedicalRecordRegisterRequest
import com.denticode.kt.data.MedicalRecordUpdateRequest
import com.denticode.kt.data.PatientDocumentRegisterRequest
import com.denticode.kt.data.PatientNoteRegisterRequest
import com.denticode.kt.data.PatientPaymentRegisterRequest
import com.denticode.kt.data.PatientTreatmentRegisterRequest
import com.denticode.kt.data.PatientUpdateRequest
import com.denticode.kt.data.PrescriptionRegisterRequest
import com.denticode.kt.data.TreatmentPlanPhaseRegisterRequest
import com.denticode.kt.data.TreatmentPlanPhaseUpdateRequest
import com.denticode.kt.data.TreatmentPlanRegisterRequest
import com.denticode.kt.data.TreatmentPlanUpdateRequest
import com.denticode.kt.export.DocumentStore
import com.denticode.kt.export.ExportService
import com.denticode.kt.export.ReceiptData
import com.denticode.kt.export.renderPatientSummaryHtml
import com.denticode.kt.export.renderPatientSummaryText
import com.denticode.kt.export.renderReceiptText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun PatientDetailWindowActions.exportFicha() {
    exportBusy = true
    scope.launch {
        val file =
            ExportService.pickSaveFile("ficha_${safeFileName()}.html") ?: run {
                messenger.showSuccess("Exportación cancelada.")
                exportBusy = false
                return@launch
            }
        runCatching {
            withContext(Dispatchers.IO) {
                ExportService.writeTextFile(file, renderPatientSummaryHtml(summaryBundle()))
            }
        }.onSuccess {
            messenger.showSuccess("Ficha exportada: ${file.name}")
            ExportService.openFile(file)
        }.onFailure { e ->
            messenger.showError(e.message ?: "No se pudo exportar la ficha.")
        }
        exportBusy = false
    }
}

fun PatientDetailWindowActions.printSummary() {
    exportBusy = true
    scope.launch {
        val file =
            ExportService.pickSaveFile("resumen_${safeFileName()}.txt") ?: run {
                messenger.showSuccess("Exportación cancelada.")
                exportBusy = false
                return@launch
            }
        runCatching {
            withContext(Dispatchers.IO) {
                ExportService.writeTextFile(file, renderPatientSummaryText(summaryBundle()))
            }
        }.onSuccess {
            messenger.showSuccess("Resumen guardado: ${file.name}")
            ExportService.openFile(file)
        }.onFailure { e ->
            messenger.showError(e.message ?: "No se pudo guardar el resumen.")
        }
        exportBusy = false
    }
}

fun PatientDetailWindowActions.submitVisit(request: AppointmentVisitRequest) {
    saveVisitBusy = true
    saveVisitError = null
    scope.launch {
        runCatching {
            withContext(Dispatchers.IO) { repo.createAppointment(request) }
        }.onSuccess {
            refreshNonce++
            showNewVisit = false
        }.onFailure { e ->
            saveVisitError = e.message ?: "No se pudo registrar la cita."
        }
        saveVisitBusy = false
    }
}

fun PatientDetailWindowActions.submitPayment(req: PatientPaymentRegisterRequest) {
    savePaymentBusy = true
    savePaymentError = null
    scope.launch {
        runCatching {
            withContext(Dispatchers.IO) { repo.registerPaymentForPatient(patient.id, req) }
        }.onSuccess {
            refreshNonce++
            showNewPayment = false
        }.onFailure { e ->
            savePaymentError = e.message ?: "No se pudo registrar el pago."
        }
        savePaymentBusy = false
    }
}

fun PatientDetailWindowActions.submitTreatment(request: PatientTreatmentRegisterRequest) {
    saveTreatmentBusy = true
    saveTreatmentError = null
    scope.launch {
        runCatching {
            withContext(Dispatchers.IO) {
                repo.registerTreatmentForPatient(request.copy(patientId = patient.id))
            }
        }.onSuccess {
            refreshNonce++
            showRegisterTreatment = false
            messenger.showSuccess("Tratamiento registrado correctamente.")
        }.onFailure { e ->
            saveTreatmentError = e.message ?: "No se pudo registrar el tratamiento."
        }
        saveTreatmentBusy = false
    }
}

fun PatientDetailWindowActions.submitMedical(request: MedicalRecordRegisterRequest) {
    val editing = editingMedical
    saveMedicalBusy = true
    saveMedicalError = null
    scope.launch {
        runCatching {
            withContext(Dispatchers.IO) {
                if (editing == null) {
                    repo.registerMedicalRecord(patient.id, request)
                } else {
                    repo.updateMedicalRecord(
                        editing.id,
                        MedicalRecordUpdateRequest(
                            recordType = request.recordType,
                            description = request.description,
                            recordedAt = request.recordedAt,
                            doctorId = request.doctorId,
                            isActive = request.isActive,
                            notes = request.notes,
                        ),
                    )
                }
            }
        }.onSuccess {
            refreshNonce++
            showMedicalRecord = false
            editingMedical = null
            messenger.showSuccess(if (editing == null) "Registro médico agregado." else "Registro médico actualizado.")
        }.onFailure { e ->
            saveMedicalError = e.message ?: "No se pudo guardar el registro médico."
        }
        saveMedicalBusy = false
    }
}

fun PatientDetailWindowActions.submitDental(request: DentalRecordRegisterRequest) {
    val editing = editingDental
    saveDentalBusy = true
    saveDentalError = null
    scope.launch {
        runCatching {
            withContext(Dispatchers.IO) {
                if (editing == null) {
                    repo.registerDentalRecord(patient.id, request)
                } else {
                    repo.updateDentalRecord(
                        editing.id,
                        DentalRecordUpdateRequest(
                            toothNumber = request.toothNumber,
                            toothQuadrant = request.toothQuadrant,
                            diagnosis = request.diagnosis,
                            treatmentPerformed = request.treatmentPerformed,
                            procedureTypeId = request.procedureTypeId,
                            recordedAt = request.recordedAt,
                            doctorId = request.doctorId,
                            notes = request.notes,
                        ),
                    )
                }
            }
        }.onSuccess {
            refreshNonce++
            showDentalRecord = false
            editingDental = null
            messenger.showSuccess(if (editing == null) "Registro dental agregado." else "Registro dental actualizado.")
        }.onFailure { e ->
            saveDentalError = e.message ?: "No se pudo guardar el registro dental."
        }
        saveDentalBusy = false
    }
}

fun PatientDetailWindowActions.submitNote(request: PatientNoteRegisterRequest) {
    saveNoteBusy = true
    saveNoteError = null
    scope.launch {
        runCatching {
            withContext(Dispatchers.IO) { repo.addPatientNote(patient.id, request) }
        }.onSuccess {
            refreshNonce++
            showAddNote = false
            messenger.showSuccess("Nota guardada.")
        }.onFailure { e ->
            saveNoteError = e.message ?: "No se pudo guardar la nota."
        }
        saveNoteBusy = false
    }
}

fun PatientDetailWindowActions.submitPrescription(request: PrescriptionRegisterRequest) {
    savePrescriptionBusy = true
    savePrescriptionError = null
    scope.launch {
        runCatching {
            withContext(Dispatchers.IO) { repo.registerPrescription(patient.id, request) }
        }.onSuccess {
            refreshNonce++
            showAddPrescription = false
            messenger.showSuccess("Receta registrada.")
        }.onFailure { e ->
            savePrescriptionError = e.message ?: "No se pudo registrar la receta."
        }
        savePrescriptionBusy = false
    }
}

fun PatientDetailWindowActions.submitFollowUp(request: FollowUpRegisterRequest) {
    saveFollowUpBusy = true
    saveFollowUpError = null
    scope.launch {
        runCatching {
            withContext(Dispatchers.IO) { repo.registerFollowUp(patient.id, request) }
        }.onSuccess {
            refreshNonce++
            showAddFollowUp = false
            messenger.showSuccess("Seguimiento registrado.")
        }.onFailure { e ->
            saveFollowUpError = e.message ?: "No se pudo registrar el seguimiento."
        }
        saveFollowUpBusy = false
    }
}

fun PatientDetailWindowActions.submitDocument(request: PatientDocumentRegisterRequest) {
    saveDocumentBusy = true
    saveDocumentError = null
    scope.launch {
        runCatching {
            withContext(Dispatchers.IO) { repo.registerPatientDocument(patient.id, request) }
        }.onSuccess {
            refreshNonce++
            showAddDocument = false
            messenger.showSuccess("Documento registrado.")
        }.onFailure { e ->
            saveDocumentError = e.message ?: "No se pudo registrar el documento."
        }
        saveDocumentBusy = false
    }
}

fun PatientDetailWindowActions.submitPlan(request: TreatmentPlanRegisterRequest) {
    val editing = editingPlan
    savePlanBusy = true
    savePlanError = null
    scope.launch {
        runCatching {
            withContext(Dispatchers.IO) {
                if (editing == null) {
                    repo.registerTreatmentPlan(patient.id, request)
                } else {
                    repo.updateTreatmentPlan(
                        editing.id,
                        TreatmentPlanUpdateRequest(
                            title = request.title,
                            description = request.description,
                            status = request.status,
                        ),
                    )
                }
            }
        }.onSuccess {
            refreshNonce++
            showPlanDialog = false
            editingPlan = null
            messenger.showSuccess(if (editing == null) "Plan de tratamiento creado." else "Plan de tratamiento actualizado.")
        }.onFailure { e ->
            savePlanError = e.message ?: "No se pudo guardar el plan."
        }
        savePlanBusy = false
    }
}

fun PatientDetailWindowActions.submitPhase(request: TreatmentPlanPhaseRegisterRequest) {
    val editing = editingPhase
    val contextPlan = phasePlanContext
    savePhaseBusy = true
    savePhaseError = null
    scope.launch {
        runCatching {
            withContext(Dispatchers.IO) {
                if (editing == null) {
                    contextPlan ?: throw IllegalStateException("Plan de contexto no disponible.")
                    repo.addTreatmentPlanPhase(contextPlan.id, request)
                } else {
                    repo.updateTreatmentPlanPhase(
                        editing.id,
                        TreatmentPlanPhaseUpdateRequest(
                            name = request.name,
                            description = request.description,
                            estimatedCost = request.estimatedCost,
                            status = request.status,
                        ),
                    )
                }
            }
        }.onSuccess {
            refreshNonce++
            showPhaseDialog = false
            editingPhase = null
            phasePlanContext = null
            messenger.showSuccess(if (editing == null) "Fase agregada." else "Fase actualizada.")
        }.onFailure { e ->
            savePhaseError = e.message ?: "No se pudo guardar la fase."
        }
        savePhaseBusy = false
    }
}

fun PatientDetailWindowActions.confirmDeleteClinical(target: DeleteClinicalTarget) {
    deleteClinicalBusy = true
    scope.launch {
        runCatching {
            withContext(Dispatchers.IO) {
                when (target.kind) {
                    ClinicalDeleteKind.MEDICAL -> repo.deleteMedicalRecord(target.id)
                    ClinicalDeleteKind.DENTAL -> repo.deleteDentalRecord(target.id)
                    ClinicalDeleteKind.NOTE -> repo.deletePatientNote(target.id)
                    ClinicalDeleteKind.PRESCRIPTION -> repo.deletePrescription(target.id)
                    ClinicalDeleteKind.FOLLOW_UP -> repo.deleteFollowUp(target.id)
                    ClinicalDeleteKind.DOCUMENT -> {
                        DocumentStore.delete(target.filePath)
                        repo.deletePatientDocument(target.id)
                    }
                    ClinicalDeleteKind.TREATMENT_PLAN -> repo.deleteTreatmentPlan(target.id)
                    ClinicalDeleteKind.TREATMENT_PLAN_PHASE -> repo.deleteTreatmentPlanPhase(target.id)
                }
            }
        }.onSuccess {
            confirmDelete = null
            refreshNonce++
            messenger.showSuccess("Elemento eliminado.")
        }.onFailure { e ->
            messenger.showError(e.message ?: "Error al eliminar.")
        }
        deleteClinicalBusy = false
    }
}

fun PatientDetailWindowActions.saveReceipt(receipt: ReceiptData, payment: PatientDetailPaymentUi) {
    receiptSaving = true
    scope.launch {
        val file =
            ExportService.pickSaveFile("recibo_${payment.id}.txt") ?: run {
                messenger.showSuccess("Guardado cancelado.")
                receiptSaving = false
                return@launch
            }
        runCatching {
            withContext(Dispatchers.IO) {
                ExportService.writeTextFile(file, renderReceiptText(receipt))
            }
        }.onSuccess {
            messenger.showSuccess("Recibo guardado: ${file.name}")
            ExportService.openFile(file)
        }.onFailure { e ->
            messenger.showError(e.message ?: "No se pudo guardar el recibo.")
        }
        receiptSaving = false
    }
}

fun PatientDetailWindowActions.editPatient(request: PatientUpdateRequest) {
    editPatientBusy = true
    editPatientError = null
    scope.launch {
        runCatching {
            withContext(Dispatchers.IO) {
                val existing = repo.findPatientByFullName(request.firstName, request.lastName, patient.id)
                if (existing != null) {
                    throw IllegalArgumentException("Ya existe un paciente con ese nombre.")
                }
                repo.updatePatient(patient.id, request)
            }
        }.onSuccess {
            refreshNonce++
            showEditPatient = false
            messenger.showSuccess("Paciente actualizado correctamente.")
        }.onFailure { e ->
            editPatientError = e.message ?: "No se pudo actualizar el paciente."
        }
        editPatientBusy = false
    }
}

fun PatientDetailWindowActions.hardDeletePatient() {
    scope.launch {
        archiveDeleteBusy = true
        runCatching {
            withContext(Dispatchers.IO) { repo.hardDeletePatient(patient.id) }
        }.onSuccess {
            showDeleteDialog = false
            messenger.showSuccess("Paciente eliminado permanentemente.")
            onClose()
        }.onFailure { e ->
            messenger.showError(e.message ?: "Error al eliminar paciente.")
        }
        archiveDeleteBusy = false
    }
}
