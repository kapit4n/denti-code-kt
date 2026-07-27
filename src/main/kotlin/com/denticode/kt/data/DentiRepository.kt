package com.denticode.kt.data

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Opt-in logging for [DentiRepository.listAppointments]: Exposed SQL to stdout plus per-row dump.
 * Enable with env `DENTI_LOG_CITAS_DATA=1` (inherited by `./gradlew run`) or JVM `-Ddenti.logCitasData=true`
 * (IDE run configuration / compose `jvmArgs`).
 */
internal fun isCitasDataLogEnabled(): Boolean {
    when (System.getenv("DENTI_LOG_CITAS_DATA")?.trim()?.lowercase()) {
        "1", "true", "yes", "on" -> return true
        "0", "false", "no", "off" -> return false
        else -> { }
    }
    return System.getProperty("denti.logCitasData")?.trim()?.equals("true", ignoreCase = true) == true
}

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

    /**
     * Lista citas leyendo siempre `appointments` y resolviendo paciente/doctor en memoria.
     * Así no se pierde ninguna fila por rarezas de JOIN en SQLite.
     */
    fun listAppointments(limit: Int = 100): List<AppointmentRow> =
        transaction {
            val logCitas = isCitasDataLogEnabled()
            if (logCitas) {
                addLogger(StdOutSqlLogger)
            }
            val lim = limit.coerceIn(1, 50_000)
            val patients =
                PatientsTable
                    .selectAll()
                    .associate { r ->
                        r[PatientsTable.id] to
                            (r[PatientsTable.firstName].trim() to r[PatientsTable.lastName].trim())
                    }
            val doctors =
                DoctorsTable
                    .selectAll()
                    .associate { r ->
                        r[DoctorsTable.id] to
                            (r[DoctorsTable.firstName].trim() to r[DoctorsTable.lastName].trim())
                    }
            val procedures =
                ProcedureTypesTable
                    .selectAll()
                    .associate { r -> r[ProcedureTypesTable.id] to r[ProcedureTypesTable.name] }
            val rows =
                AppointmentsTable
                    .selectAll()
                    .orderBy(AppointmentsTable.scheduledAt to SortOrder.DESC)
                    .limit(lim)
                    .map { row -> appointmentRowFrom(row, patients, doctors, procedures) }
            if (logCitas) {
                println(
                    "[Citas DB] listAppointments: limit=$lim | patients.map=${patients.size} | doctors.map=${doctors.size} | procedures.map=${procedures.size} | appointment rows returned=${rows.size}",
                )
                rows.forEachIndexed { i, a ->
                    println("[Citas DB]   [$i] $a")
                }
            }
            rows
        }

    fun listAppointmentsForPatient(patientId: Int): List<AppointmentRow> =
        transaction {
            val patients =
                PatientsTable
                    .selectAll()
                    .associate { r ->
                        r[PatientsTable.id] to
                            (r[PatientsTable.firstName].trim() to r[PatientsTable.lastName].trim())
                    }
            val doctors =
                DoctorsTable
                    .selectAll()
                    .associate { r ->
                        r[DoctorsTable.id] to
                            (r[DoctorsTable.firstName].trim() to r[DoctorsTable.lastName].trim())
                    }
            val procedures =
                ProcedureTypesTable
                    .selectAll()
                    .associate { r -> r[ProcedureTypesTable.id] to r[ProcedureTypesTable.name] }
            AppointmentsTable
                .selectAll()
                .where { AppointmentsTable.patientId eq patientId }
                .orderBy(AppointmentsTable.scheduledAt to SortOrder.DESC)
                .map { row -> appointmentRowFrom(row, patients, doctors, procedures) }
        }

    private val appointmentScheduledAtFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun createAppointment(request: AppointmentVisitRequest) {
        val scheduledAtIso =
            LocalDateTime.of(
                request.visitDate,
                LocalTime.of(request.visitHour, request.visitMinute, 0, 0),
            ).format(appointmentScheduledAtFormatter)
        transaction {
            val now = System.currentTimeMillis()
            val appointmentId =
                AppointmentsTable.insert {
                    it[patientId] = request.patientId
                    it[primaryDoctorId] = request.primaryDoctorId
                    it[scheduledAt] = scheduledAtIso
                    it[estimatedDurationMinutes] = request.estimatedDurationMinutes
                    it[purpose] = request.purpose?.trim()?.takeIf { value -> value.isNotEmpty() }
                    it[notes] = request.notes?.trim()?.takeIf { value -> value.isNotEmpty() }
                    it[procedureTypeId] = request.procedureTypeId
                    it[status] = request.status.name
                    it[createdAtEpochMs] = now
                    it[updatedAtEpochMs] = now
                    it[appointmentSource] = AppointmentSource.MANUAL.name
                } get AppointmentsTable.id
            appendAppointmentAudit(
                appointmentId = appointmentId,
                action = "Appointment created",
                detail = "Cita #$appointmentId programada",
            )
            syncTreatmentForAppointment(
                appointmentId = appointmentId,
                patientId = request.patientId,
                doctorId = request.primaryDoctorId,
                procedureTypeId = request.procedureTypeId,
                actionAtIso = scheduledAtIso,
                appointmentStatus = request.status,
                treatmentStatus = request.treatmentStatus,
                treatmentUnitPrice = request.treatmentUnitPrice,
                notes = request.notes,
            )
        }
    }

    fun updateAppointment(
        appointmentId: Int,
        request: AppointmentEditRequest,
        auditAction: String = "Appointment edited",
        auditDetail: String? = null,
    ) {
        val scheduledAtIso =
            LocalDateTime.of(
                request.visitDate,
                LocalTime.of(request.visitHour, request.visitMinute, 0, 0),
            ).format(appointmentScheduledAtFormatter)
        transaction {
            val n =
                AppointmentsTable.update({ AppointmentsTable.id eq appointmentId }) {
                    it[patientId] = request.patientId
                    it[primaryDoctorId] = request.primaryDoctorId
                    it[scheduledAt] = scheduledAtIso
                    it[estimatedDurationMinutes] = request.estimatedDurationMinutes
                    it[purpose] = request.purpose?.trim()?.takeIf { value -> value.isNotEmpty() }
                    it[notes] = request.notes?.trim()?.takeIf { value -> value.isNotEmpty() }
                    it[procedureTypeId] = request.procedureTypeId
                    it[status] = request.status.name
                    it[updatedAtEpochMs] = System.currentTimeMillis()
                    if (request.status == AppointmentStatus.CANCELLED) {
                        it[cancellationReason] =
                            request.cancellationReason?.trim()?.takeIf { value -> value.isNotEmpty() }
                    }
                }
            require(n > 0) { "No se encontró la cita (id=$appointmentId)." }
            appendAppointmentAudit(
                appointmentId = appointmentId,
                action = auditAction,
                detail = auditDetail ?: "Estado: ${request.status.name}",
            )
            syncTreatmentForAppointment(
                appointmentId = appointmentId,
                patientId = request.patientId,
                doctorId = request.primaryDoctorId,
                procedureTypeId = request.procedureTypeId,
                actionAtIso = scheduledAtIso,
                appointmentStatus = request.status,
                treatmentStatus = request.treatmentStatus,
                treatmentUnitPrice = request.treatmentUnitPrice,
                notes = request.notes,
            )
        }
    }

    fun listPatients(includeArchived: Boolean = false): List<Patient> =
        transaction {
            PatientsTable
                .selectAll()
                .let { query ->
                    if (!includeArchived) {
                        query.where { PatientsTable.isArchived eq false }
                    } else query
                }
                .orderBy(PatientsTable.lastName to SortOrder.ASC)
                .map { row -> patientFromRow(row) }
        }

    private fun patientFromRow(row: ResultRow): Patient =
        Patient(
            id = row[PatientsTable.id],
            firstName = row[PatientsTable.firstName],
            lastName = row[PatientsTable.lastName],
            dateOfBirth = row[PatientsTable.dateOfBirth],
            documentNumber = row[PatientsTable.documentNumber],
            gender = row[PatientsTable.gender],
            address = row[PatientsTable.address],
            contactPhone = row[PatientsTable.contactPhone],
            email = row[PatientsTable.email],
            medicalHistorySummary = row[PatientsTable.medicalHistorySummary],
            isArchived = row[PatientsTable.isArchived],
            createdAtEpochMs = row[PatientsTable.createdAtEpochMs],
            updatedAtEpochMs = row[PatientsTable.updatedAtEpochMs],
        )

    /** KPIs + filas del directorio de pacientes (citas futuras/pasadas por paciente). */
    fun loadPatientDirectory(): Pair<PatientDirectoryKpis, List<PatientDirectoryRow>> {
        val patients = listPatients()
        val appointments = listAppointments(5_000)
        val now = LocalDateTime.now()
        val thisMonth = YearMonth.now()
        val zone = ZoneId.systemDefault()
        val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

        val byPatient = appointments.groupBy { it.patientId }

        val rows =
            patients.map { patient ->
                val appts =
                    byPatient[patient.id]
                        ?.map { a -> a to parseScheduledAtLocal(a.scheduledAt) }
                        ?.sortedBy { (_, dt) -> dt }
                        ?: emptyList()

                val past = appts.filter { (_, dt) -> !dt.isAfter(now) }
                val futureAppts =
                    appts.filter { (a, dt) ->
                        dt.isAfter(now) &&
                            a.status !in
                                setOf(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW)
                    }
                val last = past.lastOrNull()
                val next = futureAppts.firstOrNull()

                val lastTreatment =
                    last?.first?.let { a ->
                        a.procedureTypeName?.takeIf { it.isNotBlank() }
                            ?: a.purpose?.takeIf { it.isNotBlank() }
                            ?: "Consulta"
                    }
                val doctorName = next?.first?.doctorName ?: last?.first?.doctorName

                val createdMonth =
                    YearMonth.from(Instant.ofEpochMilli(patient.createdAtEpochMs).atZone(zone).toLocalDate())
                val daysSinceLast =
                    last?.second?.let { java.time.Duration.between(it, now).toDays() } ?: Long.MAX_VALUE

                val status =
                    when {
                        next != null -> PatientListStatus.ACTIVE
                        createdMonth == thisMonth -> PatientListStatus.PENDING
                        daysSinceLast > 180 -> PatientListStatus.INACTIVE
                        daysSinceLast > 60 -> PatientListStatus.PENDING
                        else -> PatientListStatus.ACTIVE
                    }

                val pendingBalance = 0.0  // TODO: calculate from payments vs treatments

                PatientDirectoryRow(
                    patient = patient,
                    status = status,
                    primaryDoctorName = doctorName,
                    lastAppointmentAt = last?.second?.toLocalDate()?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    lastAppointmentTreatment = lastTreatment,
                    nextAppointmentAt = next?.second?.toLocalDate()?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    nextAppointmentTimeLabel = next?.second?.format(timeFmt),
                    pendingBalance = pendingBalance,
                )
            }

        val scheduledCount =
            appointments.count { a ->
                val dt = parseScheduledAtLocal(a.scheduledAt)
                dt.isAfter(now) &&
                    a.status in
                        setOf(
                            AppointmentStatus.SCHEDULED,
                            AppointmentStatus.CONFIRMED,
                            AppointmentStatus.IN_PROGRESS,
                        )
            }

        val kpis =
            PatientDirectoryKpis(
                totalPatients = patients.size,
                activePatients = rows.count { it.status == PatientListStatus.ACTIVE },
                newThisMonth = patients.count { p ->
                    YearMonth.from(Instant.ofEpochMilli(p.createdAtEpochMs).atZone(zone).toLocalDate()) == thisMonth
                },
                scheduledAppointments = scheduledCount,
                pendingDebt = rows.sumOf { it.pendingBalance },
            )
        return kpis to rows
    }

    private fun parseScheduledAtLocal(value: String): LocalDateTime {
        val t = value.trim()
        return try {
            LocalDateTime.parse(t, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (_: DateTimeParseException) {
            try {
                LocalDate.parse(t, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay()
            } catch (_: DateTimeParseException) {
                LocalDateTime.now()
            }
        }
    }

    fun registerPatient(request: PatientRegistrationRequest) {
        transaction {
            PatientsTable.insert {
                it[firstName] = request.firstName.trim()
                it[lastName] = request.lastName.trim()
                it[dateOfBirth] = request.dateOfBirth.trim()
                it[documentNumber] = request.documentNumber?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[contactPhone] = request.contactPhone.trim()
                it[email] = request.email?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[medicalHistorySummary] = request.medicalHistorySummary?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[createdAtEpochMs] = System.currentTimeMillis()
                it[updatedAtEpochMs] = System.currentTimeMillis()
            }
        }
    }

    fun updatePatient(patientId: Int, request: PatientUpdateRequest) {
        transaction {
            PatientsTable.update({ PatientsTable.id eq patientId }) {
                it[firstName] = request.firstName.trim()
                it[lastName] = request.lastName.trim()
                it[dateOfBirth] = request.dateOfBirth.trim()
                it[documentNumber] = request.documentNumber?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[contactPhone] = request.contactPhone.trim()
                it[email] = request.email?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[medicalHistorySummary] = request.medicalHistorySummary?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[updatedAtEpochMs] = System.currentTimeMillis()
            }
        }
    }

    fun archivePatient(patientId: Int) {
        transaction {
            PatientsTable.update({ PatientsTable.id eq patientId }) {
                it[isArchived] = true
                it[updatedAtEpochMs] = System.currentTimeMillis()
            }
        }
    }

    fun restorePatient(patientId: Int) {
        transaction {
            PatientsTable.update({ PatientsTable.id eq patientId }) {
                it[isArchived] = false
                it[updatedAtEpochMs] = System.currentTimeMillis()
            }
        }
    }

    fun hardDeletePatient(patientId: Int) {
        transaction {
            PatientsTable.deleteWhere { PatientsTable.id eq patientId }
        }
    }

    fun findPatientById(patientId: Int): Patient? =
        transaction {
            PatientsTable
                .selectAll()
                .where { PatientsTable.id eq patientId }
                .map { row -> patientFromRow(row) }
                .singleOrNull()
        }

    fun findPatientByFullName(firstName: String, lastName: String, excludeId: Int? = null): Patient? =
        transaction {
            PatientsTable
                .selectAll()
                .where {
                    (PatientsTable.firstName.lowerCase() eq firstName.trim().lowercase()) and
                        (PatientsTable.lastName.lowerCase() eq lastName.trim().lowercase())
                }
                .map { row -> patientFromRow(row) }
                .firstOrNull { it.id != excludeId }
        }

    fun searchPatients(query: String): List<Patient> =
        transaction {
            val pattern = "%${query.trim()}%"
            PatientsTable
                .selectAll()
                .where {
                    (PatientsTable.isArchived eq false) and
                        ((PatientsTable.firstName like pattern) or
                            (PatientsTable.lastName like pattern) or
                            (PatientsTable.contactPhone like pattern) or
                            (PatientsTable.email like pattern) or
                            (PatientsTable.documentNumber like pattern))
                }
                .orderBy(PatientsTable.lastName to SortOrder.ASC)
                .map { row -> patientFromRow(row) }
        }

    fun listDoctors(): List<Doctor> =
        transaction {
            DoctorsTable
                .selectAll()
                .where { DoctorsTable.isArchived eq false }
                .orderBy(DoctorsTable.lastName to SortOrder.ASC)
                .map { row -> doctorFromRow(row) }
        }

    fun findDoctor(doctorId: Int): Doctor? =
        transaction {
            DoctorsTable
                .selectAll()
                .where { DoctorsTable.id eq doctorId }
                .map { row -> doctorFromRow(row) }
                .singleOrNull()
        }

    fun listAllDoctorsIncludingArchived(): List<Doctor> =
        transaction {
            DoctorsTable
                .selectAll()
                .orderBy(DoctorsTable.lastName to SortOrder.ASC)
                .map { row -> doctorFromRow(row) }
        }

    fun searchDoctors(query: String): List<Doctor> =
        transaction {
            val pattern = "%${query.trim()}%"
            DoctorsTable
                .selectAll()
                .where {
                    (DoctorsTable.isArchived eq false) and
                        ((DoctorsTable.firstName like pattern) or
                            (DoctorsTable.lastName like pattern) or
                            (DoctorsTable.email like pattern) or
                            (DoctorsTable.contactPhone like pattern) or
                            (DoctorsTable.licenseNumber like pattern) or
                            (DoctorsTable.specialization like pattern))
                }
                .orderBy(DoctorsTable.lastName to SortOrder.ASC)
                .map { row -> doctorFromRow(row) }
        }

    fun findDoctorByLicense(licenseNumber: String, excludeId: Int? = null): Doctor? =
        transaction {
            DoctorsTable
                .selectAll()
                .where {
                    (DoctorsTable.licenseNumber eq licenseNumber.trim()) and
                        (if (excludeId != null) DoctorsTable.id neq excludeId else Op.TRUE)
                }
                .map { row -> doctorFromRow(row) }
                .singleOrNull()
        }

    fun findDoctorByFullName(firstName: String, lastName: String, excludeId: Int? = null): Doctor? =
        transaction {
            DoctorsTable
                .selectAll()
                .where {
                    (DoctorsTable.firstName.lowerCase() eq firstName.trim().lowercase()) and
                        (DoctorsTable.lastName.lowerCase() eq lastName.trim().lowercase()) and
                        (if (excludeId != null) DoctorsTable.id neq excludeId else Op.TRUE)
                }
                .map { row -> doctorFromRow(row) }
                .singleOrNull()
        }

    fun listAppointmentsForDoctor(doctorId: Int, limit: Int = 500): List<AppointmentRow> =
        listAppointments(limit).filter { it.primaryDoctorId == doctorId }

    fun registerDoctor(request: DoctorRegistrationRequest): Int {
        val firstName = request.firstName.trim()
        val lastName = request.lastName.trim()
        val email = request.email.trim()
        require(firstName.isNotEmpty()) { "El nombre es obligatorio." }
        require(lastName.isNotEmpty()) { "Los apellidos son obligatorios." }
        require(email.isNotEmpty()) { "El correo es obligatorio." }
        val now = System.currentTimeMillis()
        return transaction {
            DoctorsTable.insert {
                it[DoctorsTable.firstName] = firstName
                it[DoctorsTable.lastName] = lastName
                it[DoctorsTable.email] = email
                it[contactPhone] = request.contactPhone?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[specialization] = request.specialization?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[licenseNumber] = request.licenseNumber?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[officeRoom] = request.officeRoom?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[address] = request.address?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[workingDays] = request.workingDays?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[workingHours] = request.workingHours?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[consultoryId] = request.consultoryId
                it[notes] = request.notes?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[isActive] = request.isActive
                it[DoctorsTable.isArchived] = false
                it[DoctorsTable.createdAtEpochMs] = now
            } get DoctorsTable.id
        }
    }

    fun updateDoctor(doctorId: Int, request: DoctorUpdateRequest) {
        val firstName = request.firstName.trim()
        val lastName = request.lastName.trim()
        val email = request.email.trim()
        require(firstName.isNotEmpty()) { "El nombre es obligatorio." }
        require(lastName.isNotEmpty()) { "Los apellidos son obligatorios." }
        require(email.isNotEmpty()) { "El correo es obligatorio." }
        val now = System.currentTimeMillis()
        transaction {
            val exists =
                DoctorsTable
                    .selectAll()
                    .where { DoctorsTable.id eq doctorId }
                    .count() > 0
            require(exists) { "No se encontró el doctor seleccionado." }
            DoctorsTable.update({ DoctorsTable.id eq doctorId }) {
                it[DoctorsTable.firstName] = firstName
                it[DoctorsTable.lastName] = lastName
                it[DoctorsTable.email] = email
                it[contactPhone] = request.contactPhone?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[specialization] = request.specialization?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[licenseNumber] = request.licenseNumber?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[officeRoom] = request.officeRoom?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[address] = request.address?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[workingDays] = request.workingDays?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[workingHours] = request.workingHours?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[consultoryId] = request.consultoryId
                it[notes] = request.notes?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[isActive] = request.isActive
                it[DoctorsTable.updatedAtEpochMs] = now
            }
        }
    }

    fun archiveDoctor(doctorId: Int) {
        transaction {
            val updated =
                DoctorsTable.update({ DoctorsTable.id eq doctorId }) {
                    it[isArchived] = true
                    it[DoctorsTable.updatedAtEpochMs] = System.currentTimeMillis()
                }
            require(updated > 0) { "No se encontró el doctor seleccionado." }
        }
    }

    fun restoreDoctor(doctorId: Int) {
        transaction {
            val updated =
                DoctorsTable.update({ DoctorsTable.id eq doctorId }) {
                    it[isArchived] = false
                    it[DoctorsTable.updatedAtEpochMs] = System.currentTimeMillis()
                }
            require(updated > 0) { "No se encontró el doctor seleccionado." }
        }
    }

    fun hardDeleteDoctor(doctorId: Int) {
        transaction {
            val updated =
                DoctorsTable.deleteWhere { DoctorsTable.id eq doctorId }
            require(updated > 0) { "No se encontró el doctor seleccionado." }
        }
    }

    fun setDoctorActive(doctorId: Int, active: Boolean) {
        if (!active) {
            val blocking =
                listAppointmentsForDoctor(doctorId).any { appt ->
                    appt.status in
                        setOf(
                            AppointmentStatus.SCHEDULED,
                            AppointmentStatus.CONFIRMED,
                            AppointmentStatus.IN_PROGRESS,
                            AppointmentStatus.RESCHEDULED,
                        ) &&
                        !parseScheduledAtLocal(appt.scheduledAt).isBefore(java.time.LocalDateTime.now().minusHours(1))
                }
            require(!blocking) {
                "No se puede desactivar: el doctor tiene citas futuras programadas."
            }
        }
        transaction {
            val updated =
                DoctorsTable.update({ DoctorsTable.id eq doctorId }) {
                    it[isActive] = active
                }
            require(updated > 0) { "No se encontró el doctor seleccionado." }
        }
    }

    fun setDoctorVacation(doctorId: Int, onVacation: Boolean) {
        transaction {
            val updated =
                DoctorsTable.update({ DoctorsTable.id eq doctorId }) {
                    it[officeRoom] = if (onVacation) "VACATION" else null
                }
            require(updated > 0) { "No se encontró el doctor seleccionado." }
        }
    }

    private fun doctorFromRow(row: ResultRow): Doctor =
        Doctor(
            id = row[DoctorsTable.id],
            firstName = row[DoctorsTable.firstName],
            lastName = row[DoctorsTable.lastName],
            email = row[DoctorsTable.email],
            contactPhone = row[DoctorsTable.contactPhone],
            specialization = row[DoctorsTable.specialization],
            officeRoom = row[DoctorsTable.officeRoom],
            licenseNumber = row[DoctorsTable.licenseNumber],
            address = row[DoctorsTable.address],
            workingDays = row[DoctorsTable.workingDays],
            workingHours = row[DoctorsTable.workingHours],
            consultoryId = row[DoctorsTable.consultoryId],
            notes = row[DoctorsTable.notes],
            isActive = row[DoctorsTable.isActive],
            isArchived = row[DoctorsTable.isArchived],
            createdAtEpochMs = row[DoctorsTable.createdAtEpochMs],
            updatedAtEpochMs = row[DoctorsTable.updatedAtEpochMs],
        )

    /** KPIs + filas del directorio de doctores (citas del día y totales). */
    fun loadDoctorDirectory(): Pair<DoctorDirectoryKpis, List<DoctorDirectoryRow>> {
        val doctors = listDoctors()
        val appointments = listAppointments(5_000)
        val today = LocalDate.now()
        val activeApptStatuses =
            setOf(
                AppointmentStatus.SCHEDULED,
                AppointmentStatus.CONFIRMED,
                AppointmentStatus.IN_PROGRESS,
                AppointmentStatus.COMPLETED,
            )
        val byDoctor = appointments.groupBy { it.primaryDoctorId }

        val rows =
            doctors.map { doctor ->
                val docAppts =
                    byDoctor[doctor.id]
                        ?.filter { it.status in activeApptStatuses }
                        ?: emptyList()
                val todayCount =
                    docAppts.count { appt ->
                        parseScheduledAtLocal(appt.scheduledAt).toLocalDate() == today
                    }
                DoctorDirectoryRow(
                    doctor = doctor,
                    status = resolveDoctorListStatus(doctor.isActive, doctor.officeRoom),
                    todayAppointmentsCount = todayCount,
                    totalAppointmentsCount = docAppts.size,
                    yearsExperience = estimateDoctorExperienceYears(doctor),
                )
            }

        val todayAppointments =
            appointments.count { appt ->
                appt.status in activeApptStatuses &&
                    parseScheduledAtLocal(appt.scheduledAt).toLocalDate() == today
            }

        val kpis =
            DoctorDirectoryKpis(
                totalDoctors = doctors.size,
                activeDoctors = rows.count { it.status == DoctorListStatus.ACTIVE },
                todayAppointments = todayAppointments,
                specialtyCount =
                    doctors
                        .mapNotNull { it.specialization?.trim()?.takeIf { value -> value.isNotEmpty() } }
                        .distinct()
                        .size,
            )
        return kpis to rows
    }

    private fun resolveDoctorListStatus(
        isActive: Boolean,
        officeRoom: String?,
    ): DoctorListStatus {
        if (officeRoom?.equals("VACATION", ignoreCase = true) == true) {
            return DoctorListStatus.VACATION
        }
        return if (isActive) DoctorListStatus.ACTIVE else DoctorListStatus.INACTIVE
    }

    private fun estimateDoctorExperienceYears(doctor: Doctor): Int {
        val seed = doctor.id * 7 + doctor.firstName.length * 3 + doctor.lastName.length
        return (seed % 13) + 4
    }

    fun listProcedureTypes(): List<ProcedureTypeRow> =
        transaction {
            ProcedureTypesTable
                .selectAll()
                .where { ProcedureTypesTable.isArchived eq false }
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
                        isArchived = row[ProcedureTypesTable.isArchived],
                    )
                }
        }

    fun listAllProcedureTypesIncludingArchived(): List<ProcedureTypeRow> =
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
                        isArchived = row[ProcedureTypesTable.isArchived],
                    )
                }
        }

    fun registerProcedureType(request: ProcedureTypeRegisterRequest) {
        transaction {
            ProcedureTypesTable.insert {
                it[name] = request.name.trim()
                it[description] = request.description?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[defaultDurationMinutes] = request.defaultDurationMinutes
                it[standardPrice] = request.standardPrice
                it[requiresToothSpecification] = request.requiresToothSpecification
                it[category] = request.category?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[isActive] = request.isActive
            }
        }
    }

    fun findProcedureTypeById(id: Int): ProcedureTypeRow? =
        transaction {
            ProcedureTypesTable
                .selectAll()
                .where { ProcedureTypesTable.id eq id }
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
                        isArchived = row[ProcedureTypesTable.isArchived],
                    )
                }
                .singleOrNull()
        }

    fun findProcedureTypeByName(name: String, excludeId: Int? = null): ProcedureTypeRow? =
        transaction {
            ProcedureTypesTable
                .selectAll()
                .where {
                    (ProcedureTypesTable.name.lowerCase() eq name.trim().lowercase()) and
                        (if (excludeId != null) ProcedureTypesTable.id neq excludeId else Op.TRUE)
                }
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
                        isArchived = row[ProcedureTypesTable.isArchived],
                    )
                }
                .singleOrNull()
        }

    fun searchProcedureTypes(query: String): List<ProcedureTypeRow> =
        transaction {
            val pattern = "%${query.trim()}%"
            ProcedureTypesTable
                .selectAll()
                .where {
                    (ProcedureTypesTable.isArchived eq false) and
                        ((ProcedureTypesTable.name like pattern) or
                            (ProcedureTypesTable.description like pattern) or
                            (ProcedureTypesTable.category like pattern))
                }
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
                        isArchived = row[ProcedureTypesTable.isArchived],
                    )
                }
        }

    fun updateProcedureType(procedureTypeId: Int, request: ProcedureTypeUpdateRequest) {
        val name = request.name.trim()
        require(name.isNotEmpty()) { "El nombre es obligatorio." }
        transaction {
            val exists =
                ProcedureTypesTable
                    .selectAll()
                    .where { ProcedureTypesTable.id eq procedureTypeId }
                    .count() > 0
            require(exists) { "No se encontró el tipo de procedimiento seleccionado." }
            ProcedureTypesTable.update({ ProcedureTypesTable.id eq procedureTypeId }) {
                it[ProcedureTypesTable.name] = name
                it[description] = request.description?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[defaultDurationMinutes] = request.defaultDurationMinutes
                it[standardPrice] = request.standardPrice
                it[requiresToothSpecification] = request.requiresToothSpecification
                it[category] = request.category?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[isActive] = request.isActive
            }
        }
    }

    fun archiveProcedureType(procedureTypeId: Int) {
        transaction {
            val updated =
                ProcedureTypesTable.update({ ProcedureTypesTable.id eq procedureTypeId }) {
                    it[isArchived] = true
                }
            require(updated > 0) { "No se encontró el tipo de procedimiento." }
        }
    }

    fun restoreProcedureType(procedureTypeId: Int) {
        transaction {
            val updated =
                ProcedureTypesTable.update({ ProcedureTypesTable.id eq procedureTypeId }) {
                    it[isArchived] = false
                }
            require(updated > 0) { "No se encontró el tipo de procedimiento." }
        }
    }

    fun hardDeleteProcedureType(procedureTypeId: Int) {
        transaction {
            val hasTreatments =
                PerformedActionsTable
                    .selectAll()
                    .where { PerformedActionsTable.procedureTypeId eq procedureTypeId }
                    .count() > 0
            if (hasTreatments) {
                throw IllegalStateException("No se puede eliminar: existen tratamientos vinculados a este tipo de procedimiento. Archívelo en su lugar.")
            }
            val hasAppointments =
                AppointmentsTable
                    .selectAll()
                    .where { AppointmentsTable.procedureTypeId eq procedureTypeId }
                    .count() > 0
            if (hasAppointments) {
                throw IllegalStateException("No se puede eliminar: existen citas vinculadas a este tipo de procedimiento. Archívelo en su lugar.")
            }
            val deleted =
                ProcedureTypesTable.deleteWhere { ProcedureTypesTable.id eq procedureTypeId }
            require(deleted > 0) { "No se encontró el tipo de procedimiento." }
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
        listInventoryLines().map { line ->
            MaterialStockRow(
                consultoryName = line.consultoryName,
                consultoryShortCode = line.consultoryShortCode,
                facilityDisplayName = line.facilityDisplayName,
                facilityCode = line.facilityCode,
                quantity = line.quantity,
            )
        }

    fun listInventoryLines(): List<InventoryLineRow> =
        transaction {
            val joined =
                (MaterialInventoryLinesTable innerJoin ConsultoriesTable innerJoin TreatmentFacilitiesTable)
            joined
                .selectAll()
                .orderBy(ConsultoriesTable.name to SortOrder.ASC, TreatmentFacilitiesTable.displayName to SortOrder.ASC)
                .map { row ->
                    val category = row[TreatmentFacilitiesTable.categoryKey]
                    val facilityId = row[TreatmentFacilitiesTable.id]
                    val minQ = defaultInventoryMinQuantity(category, facilityId)
                    InventoryLineRow(
                        lineId = row[MaterialInventoryLinesTable.id],
                        consultoryId = row[ConsultoriesTable.id],
                        facilityId = facilityId,
                        consultoryName = row[ConsultoriesTable.name],
                        consultoryShortCode = row[ConsultoriesTable.shortCode],
                        facilityDisplayName = row[TreatmentFacilitiesTable.displayName],
                        facilityCode = row[TreatmentFacilitiesTable.facilityCode],
                        categoryKey = category,
                        quantity = row[MaterialInventoryLinesTable.quantity],
                        minQuantity = minQ,
                        maxQuantity = minQ * 5,
                        lastUpdatedEpochMs = System.currentTimeMillis() - (row[MaterialInventoryLinesTable.id] * 86_400_000L % 2_592_000_000L),
                        isActive = row[TreatmentFacilitiesTable.isActive],
                    )
                }
        }

    fun listInventoryMovements(
        consultoryId: Int? = null,
        facilityId: Int? = null,
        limit: Int = 200,
    ): List<InventoryMovementRow> =
        transaction {
            InventoryMovementsTable
                .selectAll()
                .let { query ->
                    when {
                        consultoryId != null && facilityId != null ->
                            query.where {
                                (InventoryMovementsTable.consultoryId eq consultoryId) and
                                    (InventoryMovementsTable.facilityId eq facilityId)
                            }
                        consultoryId != null ->
                            query.where { InventoryMovementsTable.consultoryId eq consultoryId }
                        facilityId != null ->
                            query.where { InventoryMovementsTable.facilityId eq facilityId }
                        else -> query
                    }
                }
                .orderBy(InventoryMovementsTable.createdAtEpochMs to SortOrder.DESC)
                .limit(limit)
                .map { row ->
                    InventoryMovementRow(
                        id = row[InventoryMovementsTable.id],
                        consultoryId = row[InventoryMovementsTable.consultoryId],
                        facilityId = row[InventoryMovementsTable.facilityId],
                        quantityChange = row[InventoryMovementsTable.quantityChange],
                        type = row[InventoryMovementsTable.type],
                        note = row[InventoryMovementsTable.note],
                        createdAtEpochMs = row[InventoryMovementsTable.createdAtEpochMs],
                    )
                }
        }

    fun loadInventoryDirectory(): Pair<InventoryDirectoryKpis, List<InventoryLineRow>> {
        val movements = listInventoryMovements(limit = 1_000)
        val lastMovementByKey =
            movements.groupBy { it.consultoryId to it.facilityId }
                .mapValues { (_, rows) -> rows.maxByOrNull { it.createdAtEpochMs } }

        val lines =
            listInventoryLines().map { line ->
                val last = lastMovementByKey[line.consultoryId to line.facilityId]
                if (last != null) {
                    line.copy(lastUpdatedEpochMs = last.createdAtEpochMs)
                } else {
                    line
                }
            }

        val kpis =
            InventoryDirectoryKpis(
                totalItems = lines.count { it.isActive },
                totalUnits = lines.sumOf { it.quantity },
                lowStockCount = lines.count { resolveInventoryStockStatus(it.quantity, it.minQuantity) == StockStatus.LOW },
                outOfStockCount = lines.count { it.quantity <= 0 },
            )
        return kpis to lines
    }

    private fun defaultInventoryMinQuantity(categoryKey: String, facilityId: Int): Int {
        val base =
            when (categoryKey.uppercase()) {
                "PPE" -> 40
                "INJECTION" -> 15
                "SURGERY" -> 10
                "RESTORATIVE" -> 20
                "PREVENTIVE" -> 25
                else -> 15
            }
        return base + (facilityId % 5)
    }

    fun listPaymentsForPatient(patientId: Int): List<PatientLedgerPayment> =
        transaction {
            PaymentsTable
                .leftJoin(
                    ProcedureTypesTable,
                    onColumn = { PaymentsTable.procedureTypeId },
                    otherColumn = { ProcedureTypesTable.id },
                )
                .selectAll()
                .where { PaymentsTable.patientId eq patientId }
                .orderBy(PaymentsTable.paidAt to SortOrder.DESC)
                .map { row ->
                    PatientLedgerPayment(
                        id = row[PaymentsTable.id],
                        amount = row[PaymentsTable.amount],
                        method = PaymentMethod.fromDb(row[PaymentsTable.method]),
                        paidAt = row[PaymentsTable.paidAt],
                        note = row[PaymentsTable.note],
                        procedureTypeId = row[PaymentsTable.procedureTypeId],
                        procedureTypeName = row.getOrNull(ProcedureTypesTable.name),
                        performedActionId = row[PaymentsTable.performedActionId],
                    )
                }
        }

    /** Citas cuyo `scheduled_at` empieza por la fecha local de hoy (ISO). */
    fun listTodayAppointments(limit: Int = 12): List<AppointmentRow> {
        val prefix = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return listAppointments(2000).filter { it.scheduledAt.trim().startsWith(prefix) }.take(limit)
    }

    /** Suma de pagos registrados hoy (por prefijo ISO de `paid_at`). */
    fun sumPaymentsToday(): Double {
        val prefix = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return listRecentPayments(400).filter { it.paidAt.trim().startsWith(prefix) }.sumOf { it.amount }
    }

    /** Líneas de inventario con cantidad por debajo del mínimo configurado. */
    fun countLowStockLines(threshold: Int = 5): Int =
        listInventoryLines().count { line ->
            resolveInventoryStockStatus(line.quantity, line.minQuantity) == StockStatus.LOW ||
                (threshold > 0 && line.quantity in 1 until threshold)
        }

    /** Total de pagos de los últimos 7 días, agrupados por día. */
    fun weeklyRevenue(): Pair<List<Double>, Double> {
        val today = LocalDate.now()
        val payments = listRecentPayments(1000)
        val daily =
            (6 downTo 0).map { daysAgo ->
                val dayPrefix = today.minusDays(daysAgo.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE)
                payments.filter { it.paidAt.trim().startsWith(dayPrefix) }.sumOf { it.amount }
            }
        return daily to daily.sum()
    }

    /** Actividad reciente a partir de los logs de auditoría. */
    fun recentActivity(limit: Int = 5): List<Pair<String, String>> {
        val entries =
            transaction {
                AppointmentAuditLogTable
                    .selectAll()
                    .orderBy(AppointmentAuditLogTable.createdAtEpochMs to SortOrder.DESC)
                    .limit(limit)
                    .map { row ->
                        val action = row[AppointmentAuditLogTable.action]
                        val detail = row[AppointmentAuditLogTable.detail] ?: ""
                        val ts = row[AppointmentAuditLogTable.createdAtEpochMs]
                        Triple(action, detail, ts)
                    }
            }
        val now = System.currentTimeMillis()
        return entries.map { (action, detail, ts) ->
            val label =
                when (action) {
                    "Appointment created" -> detail.ifBlank { "Cita creada" }
                    "Payment registered" -> detail.ifBlank { "Pago registrado" }
                    "Treatment registered" -> detail.ifBlank { "Tratamiento registrado" }
                    "Note added" -> detail.ifBlank { "Nota agregada" }
                    else -> detail.ifBlank { action }
                }
            val elapsed = now - ts
            val timeLabel =
                when {
                    elapsed < 60_000 -> "Ahora"
                    elapsed < 3_600_000 -> "${elapsed / 60_000}m"
                    elapsed < 86_400_000 -> "${elapsed / 3_600_000}h"
                    else -> "${elapsed / 86_400_000}d"
                }
            label to timeLabel
        }
    }

    /** Conteo de tratamientos con saldo pendiente de pago. */
    fun countPendingPayments(): Int =
        listRecentPayments(2000).count { it.treatmentStatus != null && it.treatmentStatus != TreatmentStatus.CANCELLED }

    fun listTreatmentsForPatient(patientId: Int): List<PatientTreatmentRow> =
        transaction {
            listTreatmentsInternal(patientIdFilter = patientId)
        }

    fun listAllTreatments(limit: Int = 500): List<PatientTreatmentRow> =
        transaction {
            listTreatmentsInternal(patientIdFilter = null, limit = limit)
        }

    fun findTreatmentById(treatmentId: Int): PatientTreatmentRow? =
        transaction {
            listTreatmentsInternal(patientIdFilter = null)
                .firstOrNull { it.id == treatmentId }
        }

    fun searchTreatments(query: String): List<PatientTreatmentRow> =
        transaction {
            listTreatmentsInternal(patientIdFilter = null).filter { t ->
                t.patientName.contains(query, ignoreCase = true) ||
                    t.procedureTypeName.contains(query, ignoreCase = true) ||
                    t.doctorName.contains(query, ignoreCase = true) ||
                    t.descriptionNotes.orEmpty().contains(query, ignoreCase = true)
            }
        }

    fun updateTreatmentStatus(treatmentId: Int, status: TreatmentStatus) {
        transaction {
            val updated =
                PerformedActionsTable.update({ PerformedActionsTable.id eq treatmentId }) {
                    it[PerformedActionsTable.status] = status.name
                }
            require(updated > 0) { "No se encontró el tratamiento." }
        }
    }

    fun updateTreatmentNotes(treatmentId: Int, notes: String?) {
        transaction {
            val updated =
                PerformedActionsTable.update({ PerformedActionsTable.id eq treatmentId }) {
                    it[descriptionNotes] = notes?.trim()?.takeIf { value -> value.isNotEmpty() }
                }
            require(updated > 0) { "No se encontró el tratamiento." }
        }
    }

    fun deleteTreatment(treatmentId: Int) {
        transaction {
            val row =
                PerformedActionsTable
                    .selectAll()
                    .where { PerformedActionsTable.id eq treatmentId }
                    .firstOrNull()
                    ?: throw IllegalArgumentException("No se encontró el tratamiento.")
            val appointmentId = row[PerformedActionsTable.appointmentId]
            PerformedActionsTable.deleteWhere { PerformedActionsTable.id eq treatmentId }
            AppointmentsTable.update({ AppointmentsTable.id eq appointmentId }) {
                it[AppointmentsTable.procedureTypeId] = null
            }
        }
    }

    fun listTreatmentPaymentOptionsForPatient(patientId: Int): List<TreatmentPaymentOption> =
        transaction {
            val treatments =
                listTreatmentsInternal(patientIdFilter = patientId)
                    .filter { it.status != TreatmentStatus.CANCELLED && it.totalPrice > 0.0 }

            val paymentRows =
                PaymentsTable
                    .selectAll()
                    .where { PaymentsTable.patientId eq patientId }
                    .toList()

            val paidByPerformedId =
                paymentRows
                    .mapNotNull { row ->
                        row[PaymentsTable.performedActionId]?.let { id -> id to row[PaymentsTable.amount] }
                    }
                    .groupBy({ it.first }, { it.second })
                    .mapValues { (_, amounts) -> amounts.sum() }

            val paidByAppointmentUnlinked =
                paymentRows
                    .filter { it[PaymentsTable.performedActionId] == null && it[PaymentsTable.appointmentId] != null }
                    .groupBy { it[PaymentsTable.appointmentId]!! }
                    .mapValues { (_, rows) -> rows.sumOf { it[PaymentsTable.amount] } }

            treatments.mapNotNull { treatment ->
                val paidDirect = paidByPerformedId[treatment.id] ?: 0.0
                val paidViaAppointment = paidByAppointmentUnlinked[treatment.appointmentId] ?: 0.0
                val amountPaid = paidDirect + paidViaAppointment
                val remaining = (treatment.totalPrice - amountPaid).coerceAtLeast(0.0)
                if (remaining <= 0.001) {
                    return@mapNotNull null
                }
                val paidLabel =
                    if (amountPaid > 0.0) {
                        " · pagado € ${"%.2f".format(amountPaid)} de € ${"%.2f".format(treatment.totalPrice)}"
                    } else {
                        " · € ${"%.2f".format(treatment.totalPrice)}"
                    }
                TreatmentPaymentOption(
                    performedActionId = treatment.id,
                    procedureTypeId = treatment.procedureTypeId,
                    label = "${treatment.procedureTypeName}$paidLabel · pendiente € ${"%.2f".format(remaining)}",
                    amount = remaining,
                    status = treatment.status,
                    totalPrice = treatment.totalPrice,
                    amountPaid = amountPaid,
                )
            }
        }

    fun registerPaymentForPatient(patientId: Int, request: PatientPaymentRegisterRequest) {
        transaction {
            val performedId = request.performedActionId?.takeIf { it > 0 }
            val linkedTreatment =
                performedId?.let { id ->
                    PerformedActionsTable
                        .selectAll()
                        .where { (PerformedActionsTable.id eq id) and (PerformedActionsTable.patientId eq patientId) }
                        .firstOrNull()
                }
            val procedureId =
                linkedTreatment?.get(PerformedActionsTable.procedureTypeId)
                    ?: request.procedureTypeId
            val appointmentId =
                request.appointmentId?.takeIf { it > 0 }
                    ?: linkedTreatment?.get(PerformedActionsTable.appointmentId)
            val paymentId =
                PaymentsTable.insert {
                    it[PaymentsTable.patientId] = patientId
                    it[PaymentsTable.appointmentId] = appointmentId
                    it[amount] = request.amount
                    it[method] = request.method?.name
                    it[paidAt] = request.paidAtIso.trim()
                    it[note] = request.note?.trim()?.takeIf { value -> value.isNotEmpty() }
                    it[procedureTypeId] = procedureId
                    it[performedActionId] = performedId
                } get PaymentsTable.id
            appointmentId?.let { apptId ->
                appendAppointmentAudit(
                    appointmentId = apptId,
                    action = "Payment registered",
                    detail = "Pago #$paymentId · € ${"%.2f".format(request.amount)}",
                )
            }
        }
    }

    fun listRecentPayments(limit: Int = 50): List<PaymentRow> =
        transaction {
            val joined =
                (PaymentsTable innerJoin PatientsTable)
                    .leftJoin(
                        ProcedureTypesTable,
                        onColumn = { PaymentsTable.procedureTypeId },
                        otherColumn = { ProcedureTypesTable.id },
                    )
            joined
                .selectAll()
                .orderBy(PaymentsTable.paidAt to SortOrder.DESC)
                .limit(limit)
                .map { row ->
                    val pf = row[PatientsTable.firstName]
                    val pl = row[PatientsTable.lastName]
                    val performedId = row[PaymentsTable.performedActionId]
                    val treatmentStatus =
                        performedId?.let { pid ->
                            PerformedActionsTable
                                .selectAll()
                                .where { PerformedActionsTable.id eq pid }
                                .firstOrNull()
                                ?.get(PerformedActionsTable.status)
                                ?.let { TreatmentStatus.fromDb(it) }
                        }
                    PaymentRow(
                        id = row[PaymentsTable.id],
                        patientId = row[PaymentsTable.patientId],
                        patientName = "$pf $pl".trim(),
                        amount = row[PaymentsTable.amount],
                        method = PaymentMethod.fromDb(row[PaymentsTable.method]),
                        paidAt = row[PaymentsTable.paidAt],
                        note = row[PaymentsTable.note],
                        procedureTypeName = row.getOrNull(ProcedureTypesTable.name),
                        performedActionId = performedId,
                        treatmentStatus = treatmentStatus,
                    )
                }
        }

    private fun listTreatmentsInternal(
        patientIdFilter: Int?,
        limit: Int = 5_000,
    ): List<PatientTreatmentRow> {
        val patients =
            PatientsTable
                .selectAll()
                .associate { r ->
                    r[PatientsTable.id] to
                        "${r[PatientsTable.firstName].trim()} ${r[PatientsTable.lastName].trim()}".trim()
                }
        val doctors =
            DoctorsTable
                .selectAll()
                .associate { r ->
                    r[DoctorsTable.id] to "Dr. ${r[DoctorsTable.firstName].trim()} ${r[DoctorsTable.lastName].trim()}".trim()
                }
        val procedures =
            ProcedureTypesTable
                .selectAll()
                .associate { r -> r[ProcedureTypesTable.id] to r[ProcedureTypesTable.name] }
        return PerformedActionsTable
            .selectAll()
            .orderBy(PerformedActionsTable.actionAt to SortOrder.DESC)
            .limit(limit.coerceIn(1, 50_000))
            .mapNotNull { row ->
                val apptId = row[PerformedActionsTable.appointmentId]
                val pid =
                    row[PerformedActionsTable.patientId]
                        ?: AppointmentsTable
                            .select(AppointmentsTable.patientId)
                            .where { AppointmentsTable.id eq apptId }
                            .firstOrNull()
                            ?.get(AppointmentsTable.patientId)
                        ?: return@mapNotNull null
                if (patientIdFilter != null && pid != patientIdFilter) return@mapNotNull null
                val procId = row[PerformedActionsTable.procedureTypeId]
                PatientTreatmentRow(
                    id = row[PerformedActionsTable.id],
                    patientId = pid,
                    patientName = patients[pid] ?: "Paciente #$pid",
                    appointmentId = apptId,
                    procedureTypeId = procId,
                    procedureTypeName = procedures[procId] ?: "Tratamiento #$procId",
                    doctorName = doctors[row[PerformedActionsTable.performingDoctorId]] ?: "—",
                    status = TreatmentStatus.fromDb(row[PerformedActionsTable.status]),
                    standardPrice = row[PerformedActionsTable.standardPrice],
                    unitPrice = row[PerformedActionsTable.unitPrice],
                    totalPrice = row[PerformedActionsTable.totalPrice],
                    actionAt = row[PerformedActionsTable.actionAt],
                    descriptionNotes = row[PerformedActionsTable.descriptionNotes],
                )
            }
    }

    fun registerTreatmentForPatient(request: PatientTreatmentRegisterRequest): Int {
        val actionAtIso =
            LocalDateTime.of(
                request.actionDate,
                LocalTime.of(request.actionHour, request.actionMinute, 0, 0),
            ).format(appointmentScheduledAtFormatter)
        return transaction {
            require(
                PatientsTable.selectAll().where { PatientsTable.id eq request.patientId }.count() > 0,
            ) { "No se encontró el paciente (id=${request.patientId})." }
            require(
                DoctorsTable.selectAll().where { DoctorsTable.id eq request.primaryDoctorId }.count() > 0,
            ) { "No se encontró el doctor (id=${request.primaryDoctorId})." }
            require(
                ProcedureTypesTable.selectAll().where { ProcedureTypesTable.id eq request.procedureTypeId }.count() > 0,
            ) { "No se encontró el tratamiento en catálogo (id=${request.procedureTypeId})." }

            val appointmentId =
                request.appointmentId?.also { apptId ->
                    val row =
                        AppointmentsTable
                            .selectAll()
                            .where {
                                (AppointmentsTable.id eq apptId) and (AppointmentsTable.patientId eq request.patientId)
                            }
                            .firstOrNull()
                            ?: throw IllegalArgumentException("La cita seleccionada no pertenece a este paciente.")
                    if (row[AppointmentsTable.status] in
                        setOf(AppointmentStatus.CANCELLED.name, AppointmentStatus.NO_SHOW.name)
                    ) {
                        throw IllegalArgumentException("No se puede registrar tratamiento en una cita cancelada.")
                    }
                }
                    ?: if (request.createAppointmentIfMissing) {
                        val now = System.currentTimeMillis()
                        AppointmentsTable.insert {
                            it[patientId] = request.patientId
                            it[primaryDoctorId] = request.primaryDoctorId
                            it[scheduledAt] = actionAtIso
                            it[estimatedDurationMinutes] = null
                            it[purpose] = "Tratamiento registrado"
                            it[notes] = request.descriptionNotes?.trim()?.takeIf { value -> value.isNotEmpty() }
                            it[procedureTypeId] = request.procedureTypeId
                            it[status] = AppointmentStatus.SCHEDULED.name
                            it[createdAtEpochMs] = now
                            it[updatedAtEpochMs] = now
                            it[appointmentSource] = AppointmentSource.MANUAL.name
                        } get AppointmentsTable.id
                    } else {
                        throw IllegalArgumentException("Debe seleccionar una cita para vincular el tratamiento.")
                    }

            val appointmentStatus =
                AppointmentStatus.fromDb(
                    AppointmentsTable
                        .select(AppointmentsTable.status)
                        .where { AppointmentsTable.id eq appointmentId }
                        .first()[AppointmentsTable.status],
                )

            syncTreatmentForAppointment(
                appointmentId = appointmentId,
                patientId = request.patientId,
                doctorId = request.primaryDoctorId,
                procedureTypeId = request.procedureTypeId,
                actionAtIso = actionAtIso,
                appointmentStatus = appointmentStatus,
                treatmentStatus = request.status,
                treatmentUnitPrice = request.unitPrice,
                notes = request.descriptionNotes,
            )

            appendAppointmentAudit(
                appointmentId = appointmentId,
                action = "Treatment registered",
                detail = "Tratamiento #${request.procedureTypeId} registrado",
            )

            PerformedActionsTable
                .selectAll()
                .where { PerformedActionsTable.appointmentId eq appointmentId }
                .first()[PerformedActionsTable.id]
        }
    }

    private fun syncTreatmentForAppointment(
        appointmentId: Int,
        patientId: Int,
        doctorId: Int,
        procedureTypeId: Int?,
        actionAtIso: String,
        appointmentStatus: AppointmentStatus,
        treatmentStatus: TreatmentStatus?,
        treatmentUnitPrice: Double?,
        notes: String?,
    ) {
        if (procedureTypeId == null) {
            PerformedActionsTable.deleteWhere { PerformedActionsTable.appointmentId eq appointmentId }
            return
        }
        val standard = resolveProcedureStandardPrice(procedureTypeId)
        val unit = treatmentUnitPrice ?: standard ?: 0.0
        val total = unit
        val status = treatmentStatus ?: inferTreatmentStatusFromAppointment(appointmentStatus)
        val existing =
            PerformedActionsTable
                .selectAll()
                .where { PerformedActionsTable.appointmentId eq appointmentId }
                .firstOrNull()
        if (existing != null) {
            PerformedActionsTable.update({ PerformedActionsTable.id eq existing[PerformedActionsTable.id] }) {
                it[PerformedActionsTable.patientId] = patientId
                it[PerformedActionsTable.procedureTypeId] = procedureTypeId
                it[performingDoctorId] = doctorId
                it[actionAt] = actionAtIso
                it[PerformedActionsTable.status] = status.name
                it[PerformedActionsTable.standardPrice] = standard
                it[unitPrice] = unit
                it[totalPrice] = total
                it[descriptionNotes] = notes?.trim()?.takeIf { value -> value.isNotEmpty() }
            }
        } else {
            PerformedActionsTable.insert {
                it[PerformedActionsTable.patientId] = patientId
                it[PerformedActionsTable.appointmentId] = appointmentId
                it[PerformedActionsTable.procedureTypeId] = procedureTypeId
                it[performingDoctorId] = doctorId
                it[actionAt] = actionAtIso
                it[PerformedActionsTable.status] = status.name
                it[PerformedActionsTable.standardPrice] = standard
                it[unitPrice] = unit
                it[totalPrice] = total
                it[descriptionNotes] = notes?.trim()?.takeIf { value -> value.isNotEmpty() }
            }
        }
    }

    private fun resolveProcedureStandardPrice(procedureTypeId: Int): Double? =
        ProcedureTypesTable
            .selectAll()
            .where { ProcedureTypesTable.id eq procedureTypeId }
            .firstOrNull()
            ?.get(ProcedureTypesTable.standardPrice)

    private fun appointmentRowFrom(
        row: ResultRow,
        patients: Map<Int, Pair<String, String>>,
        doctors: Map<Int, Pair<String, String>>,
        procedures: Map<Int, String>,
    ): AppointmentRow {
        val pid = row[AppointmentsTable.patientId]
        val did = row[AppointmentsTable.primaryDoctorId]
        val (pf, pl) = patients[pid] ?: ("" to "")
        val patientName =
            listOfNotNull(pf.takeIf { it.isNotEmpty() }, pl.takeIf { it.isNotEmpty() })
                .joinToString(" ")
                .trim()
                .takeIf { it.isNotEmpty() }
                ?: "Paciente #$pid"
        val (df, dl) = doctors[did] ?: ("" to "")
        val doctorName =
            if (df.isNotBlank() && dl.isNotBlank()) {
                "Dr. $df $dl".trim()
            } else {
                "Doctor #$did"
            }
        val procId = row[AppointmentsTable.procedureTypeId]
        return AppointmentRow(
            id = row[AppointmentsTable.id],
            patientId = pid,
            patientName = patientName,
            primaryDoctorId = did,
            doctorName = doctorName,
            scheduledAt = row[AppointmentsTable.scheduledAt],
            estimatedDurationMinutes = row[AppointmentsTable.estimatedDurationMinutes],
            purpose = row[AppointmentsTable.purpose],
            notes = row[AppointmentsTable.notes],
            procedureTypeId = procId,
            procedureTypeName = procId?.let { procedures[it] },
            status = AppointmentStatus.fromDb(row[AppointmentsTable.status]),
            createdAtEpochMs = row[AppointmentsTable.createdAtEpochMs],
            updatedAtEpochMs = row[AppointmentsTable.updatedAtEpochMs],
            source = AppointmentSource.fromDb(row[AppointmentsTable.appointmentSource]),
            cancellationReason = row[AppointmentsTable.cancellationReason],
            followUpAppointmentId = row[AppointmentsTable.followUpAppointmentId],
        )
    }

    fun loadAppointmentDetail(appointmentId: Int): AppointmentDetailSnapshot? {
        val appt = listAppointments(10_000).find { it.id == appointmentId } ?: return null
        val phone = listPatients().find { it.id == appt.patientId }?.contactPhone
        return transaction {
            AppointmentDetailSnapshot(
                appointment = appt,
                patientPhone = phone?.trim()?.takeIf { it.isNotEmpty() },
                structuredNotes = listAppointmentNotesInternal(appointmentId),
                paymentSummary = buildPaymentSummaryInternal(appointmentId, appt),
                auditLog = listAppointmentAuditInternal(appointmentId),
            )
        }
    }

    fun appointmentCountByDate(): Map<LocalDate, Int> =
        listAppointments(10_000)
            .groupBy { parseScheduledAtLocal(it.scheduledAt).toLocalDate() }
            .mapValues { (_, appointments) -> appointments.size }

    fun hasDoctorScheduleConflict(
        doctorId: Int,
        start: LocalDateTime,
        durationMinutes: Int,
        excludeAppointmentId: Int? = null,
    ): Boolean {
        val duration = durationMinutes.coerceAtLeast(5)
        val end = start.plusMinutes(duration.toLong())
        return listAppointments(10_000).any { appt ->
            if (appt.id == excludeAppointmentId) return@any false
            if (appt.primaryDoctorId != doctorId) return@any false
            if (appt.status in setOf(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW)) return@any false
            val otherStart = parseScheduledAtLocal(appt.scheduledAt)
            val otherEnd = otherStart.plusMinutes((appt.estimatedDurationMinutes ?: 30).toLong())
            start.isBefore(otherEnd) && end.isAfter(otherStart)
        }
    }

    fun addAppointmentNote(
        appointmentId: Int,
        body: String,
        authorLabel: String = "Recepción",
    ) {
        val text = body.trim()
        require(text.isNotEmpty()) { "La nota no puede estar vacía." }
        transaction {
            require(
                AppointmentsTable.selectAll().where { AppointmentsTable.id eq appointmentId }.count() > 0,
            ) { "No se encontró la cita (id=$appointmentId)." }
            AppointmentNotesTable.insert {
                it[AppointmentNotesTable.appointmentId] = appointmentId
                it[AppointmentNotesTable.body] = text
                it[AppointmentNotesTable.authorLabel] = authorLabel.trim().ifEmpty { "Recepción" }
                it[AppointmentNotesTable.createdAtEpochMs] = System.currentTimeMillis()
            }
            appendAppointmentAudit(
                appointmentId = appointmentId,
                action = "Note added",
                detail = text.take(120),
            )
        }
    }

    fun updateAppointmentNote(
        noteId: Int,
        appointmentId: Int,
        body: String,
    ) {
        val text = body.trim()
        require(text.isNotEmpty()) { "La nota no puede estar vacía." }
        transaction {
            val updated =
                AppointmentNotesTable.update({
                    (AppointmentNotesTable.id eq noteId) and (AppointmentNotesTable.appointmentId eq appointmentId)
                }) {
                    it[AppointmentNotesTable.body] = text
                }
            require(updated > 0) { "No se encontró la nota (id=$noteId)." }
            appendAppointmentAudit(
                appointmentId = appointmentId,
                action = "Note edited",
                detail = text.take(120),
            )
        }
    }

    fun deleteAppointmentNote(
        noteId: Int,
        appointmentId: Int,
    ) {
        transaction {
            AppointmentNotesTable.deleteWhere {
                (AppointmentNotesTable.id eq noteId) and (AppointmentNotesTable.appointmentId eq appointmentId)
            }
            appendAppointmentAudit(
                appointmentId = appointmentId,
                action = "Note deleted",
                detail = "Nota #$noteId eliminada",
            )
        }
    }

    fun logAppointmentAudit(
        appointmentId: Int,
        action: String,
        detail: String? = null,
        actorLabel: String = "Recepción",
    ) {
        transaction {
            appendAppointmentAudit(appointmentId, action, detail, actorLabel)
        }
    }

    private fun listAppointmentNotesInternal(appointmentId: Int): List<AppointmentNoteRow> =
        AppointmentNotesTable
            .selectAll()
            .where { AppointmentNotesTable.appointmentId eq appointmentId }
            .orderBy(AppointmentNotesTable.createdAtEpochMs to SortOrder.DESC)
            .map { row ->
                AppointmentNoteRow(
                    id = row[AppointmentNotesTable.id],
                    appointmentId = row[AppointmentNotesTable.appointmentId],
                    body = row[AppointmentNotesTable.body],
                    authorLabel = row[AppointmentNotesTable.authorLabel],
                    createdAtEpochMs = row[AppointmentNotesTable.createdAtEpochMs],
                )
            }

    private fun listAppointmentAuditInternal(appointmentId: Int): List<AppointmentAuditEntry> =
        AppointmentAuditLogTable
            .selectAll()
            .where { AppointmentAuditLogTable.appointmentId eq appointmentId }
            .orderBy(AppointmentAuditLogTable.createdAtEpochMs to SortOrder.DESC)
            .map { row ->
                AppointmentAuditEntry(
                    id = row[AppointmentAuditLogTable.id],
                    appointmentId = row[AppointmentAuditLogTable.appointmentId],
                    action = row[AppointmentAuditLogTable.action],
                    actorLabel = row[AppointmentAuditLogTable.actorLabel],
                    detail = row[AppointmentAuditLogTable.detail],
                    createdAtEpochMs = row[AppointmentAuditLogTable.createdAtEpochMs],
                )
            }

    private fun buildPaymentSummaryInternal(
        appointmentId: Int,
        appt: AppointmentRow,
    ): AppointmentPaymentSummary {
        val paymentRows =
            PaymentsTable
                .selectAll()
                .where { PaymentsTable.appointmentId eq appointmentId }
                .toList()
        val paid = paymentRows.sumOf { it[PaymentsTable.amount] }
        val paymentIds = paymentRows.map { it[PaymentsTable.id] }
        val treatmentCost =
            PerformedActionsTable
                .selectAll()
                .where { PerformedActionsTable.appointmentId eq appointmentId }
                .firstOrNull()
                ?.get(PerformedActionsTable.totalPrice)
                ?: appt.procedureTypeId?.let { resolveProcedureStandardPrice(it) }
        val cost = treatmentCost ?: 0.0
        val status =
            when {
                cost <= 0.0 && paid <= 0.0 -> AppointmentPaymentStatus.NONE
                paid >= cost && cost > 0.0 -> AppointmentPaymentStatus.PAID
                paid > 0.0 -> AppointmentPaymentStatus.PARTIAL
                cost > 0.0 -> AppointmentPaymentStatus.PENDING
                else -> AppointmentPaymentStatus.NONE
            }
        return AppointmentPaymentSummary(
            treatmentCost = treatmentCost,
            amountPaid = paid,
            remainingBalance = (cost - paid).coerceAtLeast(0.0),
            status = status,
            paymentIds = paymentIds,
        )
    }

    private fun appendAppointmentAudit(
        appointmentId: Int,
        action: String,
        detail: String? = null,
        actorLabel: String = "Recepción",
    ) {
        AppointmentAuditLogTable.insert {
            it[AppointmentAuditLogTable.appointmentId] = appointmentId
            it[AppointmentAuditLogTable.action] = action
            it[AppointmentAuditLogTable.actorLabel] = actorLabel
            it[AppointmentAuditLogTable.detail] = detail?.trim()?.takeIf { value -> value.isNotEmpty() }
            it[createdAtEpochMs] = System.currentTimeMillis()
        }
    }
}
