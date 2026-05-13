package com.denticode.kt.data

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val ISO_DT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

fun seedDentiReferenceDataIfEmpty() {
    transaction {
        if (ProcedureTypesTable.selectAll().count() == 0L) {
            val procedures =
                listOf(
                    arrayOf("Routine cleaning", "Prophylaxis and polishing", "30", "60.0", "Preventive"),
                    arrayOf("Periodic exam", "Comprehensive oral evaluation", "20", "50.0", "Diagnostic"),
                    arrayOf("Composite filling", "Tooth-coloured restoration", "45", "120.0", "Restorative"),
                    arrayOf("Root canal — molar", "Endodontic treatment", "90", "650.0", "Endodontics"),
                    arrayOf("Tooth extraction", "Simple extraction", "30", "180.0", "Surgery"),
                    arrayOf("Crown placement", "Single-unit ceramic crown", "60", "850.0", "Prosthetics"),
                    arrayOf("Teeth whitening", "In-office whitening session", "60", "300.0", "Cosmetic"),
                    arrayOf("Orthodontic adjustment", "Bracket / wire adjustment", "30", "95.0", "Orthodontics"),
                    arrayOf("Pediatric sealant", "Sealant on permanent molars", "20", "55.0", "Pediatric"),
                    arrayOf("Periodontal scaling", "Scaling and root planing per quadrant", "60", "220.0", "Periodontics"),
                )
            procedures.forEach { row ->
                val name = row[0]
                val desc = row[1]
                val dur = row[2].toInt()
                val price = row[3].toDouble()
                ProcedureTypesTable.insert {
                    it[ProcedureTypesTable.name] = name
                    it[ProcedureTypesTable.description] = desc
                    it[ProcedureTypesTable.defaultDurationMinutes] = dur
                    it[ProcedureTypesTable.standardPrice] = price
                    it[ProcedureTypesTable.requiresToothSpecification] =
                        name.contains("filling", true) || name.contains("Root", true) || name.contains("sealant", true)
                    it[ProcedureTypesTable.category] = row[4]
                    it[ProcedureTypesTable.isActive] = true
                }
            }
        }
        if (TreatmentFacilitiesTable.selectAll().count() == 0L) {
            val facilities =
                listOf(
                    Triple("GLOVES", "PPE", "Exam gloves"),
                    Triple("MASKS", "PPE", "Surgical masks"),
                    Triple("ANESTHESIA", "INJECTION", "Local anesthesia"),
                    Triple("SUTURES", "SURGERY", "Sutures"),
                    Triple("COMPOSITE", "RESTORATIVE", "Composite resin"),
                    Triple("FLUORIDE", "PREVENTIVE", "Fluoride varnish"),
                )
            facilities.forEachIndexed { i, t ->
                TreatmentFacilitiesTable.insert {
                    it[facilityCode] = t.first
                    it[categoryKey] = t.second
                    it[displayName] = t.third
                    it[sortOrder] = (i + 1) * 10
                    it[isActive] = true
                }
            }
        }
        if (ConsultoriesTable.selectAll().count() == 0L) {
            ConsultoriesTable.insert {
                it[name] = "Consultory 1"
                it[shortCode] = "C1"
                it[sortOrder] = 10
                it[isActive] = true
            }
            ConsultoriesTable.insert {
                it[name] = "Consultory 2"
                it[shortCode] = "C2"
                it[sortOrder] = 20
                it[isActive] = true
            }
        }
        if (MaterialInventoryLinesTable.selectAll().count() == 0L) {
            val c1 = ConsultoriesTable.selectAll().first()[ConsultoriesTable.id]
            val c2 =
                ConsultoriesTable.selectAll()
                    .where { ConsultoriesTable.shortCode eq "C2" }
                    .firstOrNull()?.get(ConsultoriesTable.id)
                    ?: c1
            TreatmentFacilitiesTable.selectAll().forEach { fRow ->
                val fid = fRow[TreatmentFacilitiesTable.id]
                MaterialInventoryLinesTable.insert {
                    it[consultoryId] = c1
                    it[facilityId] = fid
                    it[quantity] = 50 + (fid % 7) * 5
                }
                MaterialInventoryLinesTable.insert {
                    it[consultoryId] = c2
                    it[facilityId] = fid
                    it[quantity] = 30 + (fid % 5) * 3
                }
            }
        }
    }
}

fun seedDemoClinicDataIfNeeded() {
    transaction {
        if (DoctorsTable.selectAll().count() == 0L) {
            DoctorsTable.insert {
                it[firstName] = "María"
                it[lastName] = "García"
                it[email] = "m.garcia@clinic.demo"
                it[contactPhone] = "+34 600 000 001"
                it[specialization] = "Odontología general"
                it[isActive] = true
            }
            DoctorsTable.insert {
                it[firstName] = "James"
                it[lastName] = "Nguyen"
                it[email] = "j.nguyen@clinic.demo"
                it[contactPhone] = "+34 600 000 002"
                it[specialization] = "Endodoncia"
                it[isActive] = true
            }
        }
        if (PatientsTable.selectAll().count() == 0L) {
            val now = System.currentTimeMillis()
            PatientsTable.insert {
                it[firstName] = "Ana"
                it[lastName] = "López"
                it[dateOfBirth] = "1990-05-12"
                it[contactPhone] = "+34 611 111 111"
                it[email] = "ana.lopez@mail.demo"
                it[createdAtEpochMs] = now
            }
            PatientsTable.insert {
                it[firstName] = "Carlos"
                it[lastName] = "Ruiz"
                it[dateOfBirth] = "1985-11-03"
                it[contactPhone] = "+34 622 222 222"
                it[email] = null
                it[createdAtEpochMs] = now
            }
            PatientsTable.insert {
                it[firstName] = "Elena"
                it[lastName] = "Martín"
                it[dateOfBirth] = "2012-01-20"
                it[gender] = "F"
                it[contactPhone] = "+34 633 333 333"
                it[email] = "elena.martin@mail.demo"
                it[createdAtEpochMs] = now
            }
        }
        if (AppointmentsTable.selectAll().count() == 0L) {
            val d1 = DoctorsTable.selectAll().first()[DoctorsTable.id]
            val d2 = DoctorsTable.selectAll().where { DoctorsTable.email eq "j.nguyen@clinic.demo" }.first()[DoctorsTable.id]
            val p1 = PatientsTable.selectAll().first()[PatientsTable.id]
            val p2 = PatientsTable.selectAll().where { PatientsTable.lastName eq "Ruiz" }.first()[PatientsTable.id]
            val tomorrow = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0)
            val nextWeek = LocalDateTime.now().plusDays(7).withHour(15).withMinute(30)
            AppointmentsTable.insert {
                it[patientId] = p1
                it[primaryDoctorId] = d1
                it[scheduledAt] = tomorrow.format(ISO_DT)
                it[estimatedDurationMinutes] = 45
                it[purpose] = "Limpieza y revisión"
                it[status] = AppointmentStatus.CONFIRMED.name
            }
            AppointmentsTable.insert {
                it[patientId] = p2
                it[primaryDoctorId] = d2
                it[scheduledAt] = nextWeek.format(ISO_DT)
                it[estimatedDurationMinutes] = 60
                it[purpose] = "Valoración endodoncia"
                it[status] = AppointmentStatus.SCHEDULED.name
            }
        }
        if (PaymentsTable.selectAll().count() == 0L) {
            val p1 = PatientsTable.selectAll().first()[PatientsTable.id]
            PaymentsTable.insert {
                it[patientId] = p1
                it[appointmentId] = null
                it[amount] = 120.0
                it[method] = PaymentMethod.CARD.name
                it[paidAt] = LocalDateTime.now().minusDays(2).format(ISO_DT)
                it[note] = "Demo"
            }
        }
    }
}
