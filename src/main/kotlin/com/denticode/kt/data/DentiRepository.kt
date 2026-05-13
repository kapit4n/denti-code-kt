package com.denticode.kt.data

import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class DentiRepository {
    fun clinicOverview(): ClinicOverview =
        transaction {
            val patients = PatientsTable.selectAll().count().toInt()
            val doctors =
                DoctorsTable
                    .selectAll()
                    .map { it[DoctorsTable.isActive] }
                    .count { it }
            val appts = AppointmentsTable.selectAll().count().toInt()
            val upcoming =
                AppointmentsTable
                    .selectAll()
                    .map { it[AppointmentsTable.status] }
                    .count {
                        it == AppointmentStatus.SCHEDULED.name ||
                            it == AppointmentStatus.CONFIRMED.name ||
                            it == AppointmentStatus.IN_PROGRESS.name
                    }
            val paySum =
                PaymentsTable
                    .selectAll()
                    .map { it[PaymentsTable.amount] }
                    .sumOf { amt: Double -> amt }
            ClinicOverview(
                patientCount = patients,
                doctorCount = doctors,
                appointmentCount = appts,
                upcomingAppointmentCount = upcoming,
                paymentTotalRecent = paySum,
            )
        }

    fun listAppointments(limit: Int = 100): List<AppointmentRow> =
        transaction {
            val joined =
                (AppointmentsTable innerJoin PatientsTable innerJoin DoctorsTable)
            joined
                .selectAll()
                .orderBy(AppointmentsTable.scheduledAt to SortOrder.DESC)
                .limit(limit)
                .map { row ->
                    val pf = row[PatientsTable.firstName]
                    val pl = row[PatientsTable.lastName]
                    val df = row[DoctorsTable.firstName]
                    val dl = row[DoctorsTable.lastName]
                    AppointmentRow(
                        id = row[AppointmentsTable.id],
                        patientId = row[AppointmentsTable.patientId],
                        patientName = "$pf $pl".trim(),
                        primaryDoctorId = row[AppointmentsTable.primaryDoctorId],
                        doctorName = "Dr. $df $dl".trim(),
                        scheduledAt = row[AppointmentsTable.scheduledAt],
                        estimatedDurationMinutes = row[AppointmentsTable.estimatedDurationMinutes],
                        purpose = row[AppointmentsTable.purpose],
                        status = AppointmentStatus.fromDb(row[AppointmentsTable.status]),
                    )
                }
        }

    fun listPatients(): List<Patient> =
        transaction {
            PatientsTable
                .selectAll()
                .orderBy(PatientsTable.lastName to SortOrder.ASC)
                .map { row ->
                    Patient(
                        id = row[PatientsTable.id],
                        firstName = row[PatientsTable.firstName],
                        lastName = row[PatientsTable.lastName],
                        dateOfBirth = row[PatientsTable.dateOfBirth],
                        contactPhone = row[PatientsTable.contactPhone],
                        email = row[PatientsTable.email],
                        medicalHistorySummary = row[PatientsTable.medicalHistorySummary],
                        createdAtEpochMs = row[PatientsTable.createdAtEpochMs],
                    )
                }
        }

    fun listDoctors(): List<Doctor> =
        transaction {
            DoctorsTable
                .selectAll()
                .orderBy(DoctorsTable.lastName to SortOrder.ASC)
                .map { row ->
                    Doctor(
                        id = row[DoctorsTable.id],
                        firstName = row[DoctorsTable.firstName],
                        lastName = row[DoctorsTable.lastName],
                        email = row[DoctorsTable.email],
                        contactPhone = row[DoctorsTable.contactPhone],
                        specialization = row[DoctorsTable.specialization],
                        isActive = row[DoctorsTable.isActive],
                    )
                }
        }

    fun listProcedureTypes(): List<ProcedureTypeRow> =
        transaction {
            ProcedureTypesTable
                .selectAll()
                .orderBy(ProcedureTypesTable.name to SortOrder.ASC)
                .map { row ->
                    ProcedureTypeRow(
                        id = row[ProcedureTypesTable.id],
                        name = row[ProcedureTypesTable.name],
                        description = row[ProcedureTypesTable.description],
                        defaultDurationMinutes = row[ProcedureTypesTable.defaultDurationMinutes],
                        standardPrice = row[ProcedureTypesTable.standardPrice],
                        requiresToothSpecification = row[ProcedureTypesTable.requiresToothSpecification],
                        category = row[ProcedureTypesTable.category],
                        isActive = row[ProcedureTypesTable.isActive],
                    )
                }
        }

    fun listConsultories(): List<Consultory> =
        transaction {
            ConsultoriesTable
                .selectAll()
                .orderBy(ConsultoriesTable.sortOrder to SortOrder.ASC)
                .map { row ->
                    Consultory(
                        id = row[ConsultoriesTable.id],
                        name = row[ConsultoriesTable.name],
                        shortCode = row[ConsultoriesTable.shortCode],
                        sortOrder = row[ConsultoriesTable.sortOrder],
                    )
                }
        }

    fun listMaterialStock(): List<MaterialStockRow> =
        transaction {
            val joined =
                MaterialInventoryLinesTable
                    .innerJoin(ConsultoriesTable, { MaterialInventoryLinesTable.consultoryId eq ConsultoriesTable.id })
                    .innerJoin(TreatmentFacilitiesTable, { MaterialInventoryLinesTable.facilityId eq TreatmentFacilitiesTable.id })
            joined
                .selectAll()
                .orderBy(ConsultoriesTable.name to SortOrder.ASC, TreatmentFacilitiesTable.displayName to SortOrder.ASC)
                .map { row ->
                    MaterialStockRow(
                        consultoryName = row[ConsultoriesTable.name],
                        consultoryShortCode = row[ConsultoriesTable.shortCode],
                        facilityDisplayName = row[TreatmentFacilitiesTable.displayName],
                        facilityCode = row[TreatmentFacilitiesTable.facilityCode],
                        quantity = row[MaterialInventoryLinesTable.quantity],
                    )
                }
        }

    fun listRecentPayments(limit: Int = 50): List<PaymentRow> =
        transaction {
            val joined = (PaymentsTable innerJoin PatientsTable)
            joined
                .selectAll()
                .orderBy(PaymentsTable.paidAt to SortOrder.DESC)
                .limit(limit)
                .map { row ->
                    val pf = row[PatientsTable.firstName]
                    val pl = row[PatientsTable.lastName]
                    PaymentRow(
                        id = row[PaymentsTable.id],
                        patientName = "$pf $pl".trim(),
                        amount = row[PaymentsTable.amount],
                        method = PaymentMethod.fromDb(row[PaymentsTable.method]),
                        paidAt = row[PaymentsTable.paidAt],
                        note = row[PaymentsTable.note],
                    )
                }
        }
}
