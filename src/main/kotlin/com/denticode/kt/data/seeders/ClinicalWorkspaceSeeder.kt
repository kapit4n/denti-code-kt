package com.denticode.kt.data.seeders

import com.denticode.kt.data.FollowUpsTable
import com.denticode.kt.data.PatientDentalHistoryTable
import com.denticode.kt.data.PatientDocumentsTable
import com.denticode.kt.data.PatientMedicalHistoryTable
import com.denticode.kt.data.PatientNotesTable
import com.denticode.kt.data.PrescriptionsTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

/** Seed para la ficha clínica del paciente: historial médico/dental, notas, recetas, seguimientos y documentos. */
object ClinicalWorkspaceSeeder {

    private val ISO_DATE: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private val medicalConditions =
        listOf(
            "Hipertensión arterial controlada",
            "Diabetes tipo 2",
            "Asma bronquial",
            "Alergia a la penicilina",
            "Alergia al látex",
            "Alergia al ibuprofeno",
            "Hipotiroidismo",
            "Anemia leve",
            "Cardiopatía: soplo cardíaco",
            "Epilepsia",
            "Artritis reumatoide",
            "Rinitis alérgica",
            "Embarazo (2do trimestre)",
            "Ulceración gástrica",
            "Osteoporosis",
        )

    private val surgeries =
        listOf(
            "Apendicectomía",
            "Amigdalectomía",
            "Cesárea",
            "Extracción de muelas del juicio",
            "Cirugía de vesícula",
        )

    private val dentalDiagnoses =
        listOf(
            "Caries oclusal profunda",
            "Caries proximal",
            "Pulpitis irreversible",
            "Enfermedad periodontal leve",
            "Periodontitis moderada",
            "Fractura de corona",
            "Diente impactado",
            "Absceso periapical",
            "Desgaste por bruxismo",
            "Hipersensibilidad dental",
            "Gingivitis",
            "Maloclusión",
            "Falta de pieza dental",
        )

    private val dentalTreatments =
        listOf(
            "Obturación con resina",
            "Endodoncia",
            "Profilaxis y pulido",
            "Corona de porcelana",
            "Raspaje y alisado radicular",
            "Extracción quirúrgica",
            "Férula de descarga",
            "Tratamiento de conducto",
            "Aplicación de flúor",
            "Sellante dental",
        )

    private val medicines =
        listOf(
            "Amoxicilina 500 mg" to "1 cápsula cada 8 horas",
            "Ibuprofeno 400 mg" to "1 tableta cada 8 horas",
            "Paracetamol 500 mg" to "1 tableta cada 6 horas",
            "Metronidazol 250 mg" to "1 tableta cada 8 horas",
            "Clorhexidina 0.12% (enjuague)" to "Enjuague 30 s cada 12 horas",
            "Nimesulida 100 mg" to "1 sobre cada 12 horas",
            "Amoxicilina + Ácido clavulánico 875/125 mg" to "1 tableta cada 12 horas",
            "Ketorolaco 10 mg" to "1 tableta cada 8 horas por 3 días",
            "Lidocaína gel 2%" to "Aplicar según dolor",
            "Eugenol (tópico)" to "Aplicar en la zona afectada",
        )

    private val noteBodies =
        listOf(
            "Paciente refiere dolor desde hace 3 días en zona inferior izquierda.",
            "Se realiza radiografía periapical. Se observa caries profunda.",
            "Paciente alérgico a la penicilina. Se administra con precaución.",
            "Control post-operatorio: cicatrización correcta, sin signos de infección.",
            "Paciente solicita información sobre opciones de blanqueamiento.",
            "Se programa segunda fase de tratamiento periodontal.",
            "Endodoncia completada. Se recomienda corona de porcelana.",
            "Extracción sin complicaciones. Indicaciones post-operatorias entregadas.",
            "Paciente presenta bruxismo. Se sugiere uso de férula de descarga.",
            "Limpieza y profilaxis completadas. Próximo control en 6 meses.",
            "Se colocan brackets en arcada superior. Instrucciones de higiene entregadas.",
            "Paciente manifiesta satisfacción con el resultado del tratamiento.",
            "Se realiza aplicación de fluoruro en toda la arcada.",
            "Paciente no asistió a control. Se reprograma para la próxima semana.",
        )

    private val followUpNotes =
        listOf(
            "Control post-operatorio a los 7 días",
            "Reevaluación periodontal en 1 mes",
            "Control de endodoncia en 2 semanas",
            "Retiro de puntos en 7 días",
            "Evaluación de corona definitiva en 15 días",
            "Control de ortodoncia mensual",
            "Reprogramar citas restantes",
        )

    fun seed(
        config: DemoDataConfig,
        patients: List<PatientsSeeder.SeedPatient>,
        doctors: List<DoctorsSeeder.SeedDoctor>,
        procedures: List<ProceduresSeeder.SeedProcedure>,
    ) {
        if (PatientMedicalHistoryTable.selectAll().count() > 0) return

        val rng = Random(777)
        val now = LocalDate.now()
        val todayMs = System.currentTimeMillis()

        val patientIds = patients.map { it.id }
        val doctorIds = doctors.map { it.id }
        val procedureIds = procedures.map { it.id }

        // ── Medical history ─────────────────────────────────────────────
        patientIds.forEach { pid ->
            if (rng.nextDouble() > 0.75) return@forEach
            val recordCount = rng.nextInt(1, 4)
            repeat(recordCount) {
                val isCondition = rng.nextDouble() > 0.5
                val description =
                    if (isCondition) medicalConditions[rng.nextInt(medicalConditions.size)]
                    else surgeries[rng.nextInt(surgeries.size)]
                val recordType =
                    when {
                        description.contains("Alergia") -> "ALLERGY"
                        !isCondition -> "SURGERY"
                        else -> "CONDITION"
                    }
                val recordedAt = now.minusDays(rng.nextLong(0, 500)).format(ISO_DATE)
                PatientMedicalHistoryTable.insert {
                    it[patientId] = pid
                    it[PatientMedicalHistoryTable.recordType] = recordType
                    it[PatientMedicalHistoryTable.description] = description
                    it[PatientMedicalHistoryTable.recordedAt] = recordedAt
                    it[doctorId] = doctorIds[rng.nextInt(doctorIds.size)]
                    it[isActive] = rng.nextDouble() > 0.12
                    it[notes] = if (rng.nextDouble() > 0.6) noteBodies[rng.nextInt(noteBodies.size)] else null
                    it[createdAtEpochMs] = todayMs - rng.nextLong(0, 400L * 86_400_000)
                }
            }
        }

        // ── Dental history ──────────────────────────────────────────────
        patientIds.forEach { pid ->
            if (rng.nextDouble() > 0.7) return@forEach
            val recordCount = rng.nextInt(1, 4)
            repeat(recordCount) {
                val tooth = rng.nextInt(11, 48).toString()
                val recordedAt = now.minusDays(rng.nextLong(0, 400)).format(ISO_DATE)
                PatientDentalHistoryTable.insert {
                    it[patientId] = pid
                    it[toothNumber] = tooth
                    it[toothQuadrant] = "Q${(tooth.toInt() - 1) / 8 + 1}"
                    it[diagnosis] = dentalDiagnoses[rng.nextInt(dentalDiagnoses.size)]
                    it[treatmentPerformed] = if (rng.nextDouble() > 0.3) dentalTreatments[rng.nextInt(dentalTreatments.size)] else null
                    it[procedureTypeId] = procedureIds[rng.nextInt(procedureIds.size)]
                    it[PatientDentalHistoryTable.recordedAt] = recordedAt
                    it[doctorId] = doctorIds[rng.nextInt(doctorIds.size)]
                    it[notes] = if (rng.nextDouble() > 0.65) noteBodies[rng.nextInt(noteBodies.size)] else null
                    it[createdAtEpochMs] = todayMs - rng.nextLong(0, 350L * 86_400_000)
                }
            }
        }

        // ── Patient notes ───────────────────────────────────────────────
        patientIds.forEach { pid ->
            if (rng.nextDouble() > 0.55) return@forEach
            val noteCount = rng.nextInt(1, 4)
            repeat(noteCount) {
                PatientNotesTable.insert {
                    it[patientId] = pid
                    it[body] = noteBodies[rng.nextInt(noteBodies.size)]
                    it[authorLabel] = if (rng.nextDouble() > 0.4) "Recepción" else "Dr. ${doctorIds[rng.nextInt(doctorIds.size)]}"
                    it[isPinned] = rng.nextDouble() > 0.85
                    it[createdAtEpochMs] = todayMs - rng.nextLong(0, 300L * 86_400_000)
                }
            }
        }

        // ── Prescriptions ───────────────────────────────────────────────
        patientIds.forEach { pid ->
            if (rng.nextDouble() > 0.4) return@forEach
            val rxCount = rng.nextInt(1, 3)
            repeat(rxCount) {
                val (medicine, dosage) = medicines[rng.nextInt(medicines.size)]
                val prescribedAt = now.minusDays(rng.nextLong(0, 200)).format(ISO_DATE)
                PrescriptionsTable.insert {
                    it[patientId] = pid
                    it[PrescriptionsTable.medicine] = medicine
                    it[PrescriptionsTable.dosage] = dosage
                    it[frequency] = "Cada 8 horas"
                    it[instructions] = "Tomar después de las comidas con abundante agua."
                    it[PrescriptionsTable.prescribedAt] = prescribedAt
                    it[doctorId] = doctorIds[rng.nextInt(doctorIds.size)]
                    it[status] =
                        if (rng.nextDouble() > 0.6) {
                            listOf("ACTIVE", "COMPLETED", "COMPLETED", "CANCELLED")[rng.nextInt(4)]
                        } else {
                            "ACTIVE"
                        }
                    it[createdAtEpochMs] = todayMs - rng.nextLong(0, 180L * 86_400_000)
                }
            }
        }

        // ── Follow-ups ──────────────────────────────────────────────────
        patientIds.forEach { pid ->
            if (rng.nextDouble() > 0.35) return@forEach
            val due = now.plusDays(rng.nextLong(-5, 40)).format(ISO_DATE)
            val past = LocalDate.parse(due).isBefore(now)
            FollowUpsTable.insert {
                it[patientId] = pid
                it[dueDate] = due
                it[notes] = followUpNotes[rng.nextInt(followUpNotes.size)]
                it[status] =
                    if (past && rng.nextDouble() > 0.3) {
                        listOf("COMPLETED", "CANCELLED")[rng.nextInt(2)]
                    } else {
                        "PENDING"
                    }
                it[createdAtEpochMs] = todayMs - rng.nextLong(0, 120L * 86_400_000)
            }
        }

        // ── Documents (metadatos) ───────────────────────────────────────
        val documentTitles =
            listOf(
                "Radiografía panorámica",
                "Radiografía periapical pieza 36",
                "Fotografía intraoral",
                "Consentimiento informado",
                "Plan de tratamiento",
                "Evolución clínica",
            )
        patientIds.forEach { pid ->
            if (rng.nextDouble() > 0.25) return@forEach
            val docCount = rng.nextInt(1, 3)
            repeat(docCount) {
                val title = documentTitles[rng.nextInt(documentTitles.size)]
                val category =
                    when {
                        title.startsWith("Radiografía") -> "RADIOGRAPH"
                        title.startsWith("Fotografía") -> "PHOTO"
                        title.startsWith("Consentimiento") -> "CONSENT"
                        else -> "PDF"
                    }
                PatientDocumentsTable.insert {
                    it[patientId] = pid
                    it[PatientDocumentsTable.title] = title
                    it[PatientDocumentsTable.category] = category
                    it[fileName] = "demo_${title.lowercase().replace(" ", "_")}_${rng.nextInt(100, 999)}.${if (category == "PHOTO") "jpg" else "pdf"}"
                    it[filePath] = null
                    it[mimeType] = if (category == "PHOTO") "image/jpeg" else "application/pdf"
                    it[fileSize] = rng.nextLong(80_000, 4_200_000)
                    it[notes] = null
                    it[uploadedAtEpochMs] = todayMs - rng.nextLong(0, 250L * 86_400_000)
                }
            }
        }
    }
}
