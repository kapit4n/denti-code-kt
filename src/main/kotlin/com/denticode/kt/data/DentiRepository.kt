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
    fun loadPatientDirectory(): Pair<PatientDirectoryKpis, List<PatientDirectoryRow>> =
        transaction {
            val patients = listPatients()
            val appointments = listAppointments(5_000)
            val now = LocalDateTime.now()
            val thisMonth = YearMonth.now()
            val zone = ZoneId.systemDefault()
            val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

            val byPatient = appointments.groupBy { it.patientId }
    
            val billableByPatient =
                listTreatmentsInternal(patientIdFilter = null)
                    .filter { it.status != TreatmentStatus.CANCELLED }
                    .groupBy { it.patientId }
                    .mapValues { (_, rows) -> rows.sumOf { it.totalPrice } }
    
            val paidByPatient =
                PaymentsTable
                    .selectAll()
                    .groupBy { it[PaymentsTable.patientId] }
                    .mapValues { (_, rows) -> rows.sumOf { it[PaymentsTable.amount] } }
    
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
    
                    val pendingBalance =
                        ((billableByPatient[patient.id] ?: 0.0) - (paidByPatient[patient.id] ?: 0.0))
                            .coerceAtLeast(0.0)
    
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
            kpis to rows
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

    private fun procedureTypeFromRow(row: ResultRow): ProcedureTypeRow =
        ProcedureTypeRow(
            id = row[ProcedureTypesTable.id],
            name = row[ProcedureTypesTable.name],
            description = row[ProcedureTypesTable.description],
            defaultDurationMinutes = row[ProcedureTypesTable.defaultDurationMinutes],
            standardPrice = row[ProcedureTypesTable.standardPrice],
            requiresToothSpecification = row[ProcedureTypesTable.requiresToothSpecification],
            category = row[ProcedureTypesTable.category],
            categoryId = row[ProcedureTypesTable.categoryId],
            currency = row[ProcedureTypesTable.currency],
            color = row[ProcedureTypesTable.color],
            icon = row[ProcedureTypesTable.icon],
            isFavorite = row[ProcedureTypesTable.isFavorite],
            notes = row[ProcedureTypesTable.notes],
            isActive = row[ProcedureTypesTable.isActive],
            isArchived = row[ProcedureTypesTable.isArchived],
            createdAtEpochMs = row[ProcedureTypesTable.createdAtEpochMs],
            updatedAtEpochMs = row[ProcedureTypesTable.updatedAtEpochMs],
        )

    fun listProcedureTypes(): List<ProcedureTypeRow> =
        transaction {
            ProcedureTypesTable
                .selectAll()
                .where { ProcedureTypesTable.isArchived eq false }
                .orderBy(ProcedureTypesTable.name to SortOrder.ASC)
                .map { row -> procedureTypeFromRow(row) }
        }

    fun listAllProcedureTypesIncludingArchived(): List<ProcedureTypeRow> =
        transaction {
            ProcedureTypesTable
                .selectAll()
                .orderBy(ProcedureTypesTable.name to SortOrder.ASC)
                .map { row -> procedureTypeFromRow(row) }
        }

    fun registerProcedureType(request: ProcedureTypeRegisterRequest) {
        val now = System.currentTimeMillis()
        transaction {
            ProcedureTypesTable.insert {
                it[name] = request.name.trim()
                it[description] = request.description?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[defaultDurationMinutes] = request.defaultDurationMinutes
                it[standardPrice] = request.standardPrice
                it[requiresToothSpecification] = request.requiresToothSpecification
                it[category] = request.category?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[categoryId] = request.categoryId
                it[currency] = request.currency.ifBlank { "BOB" }
                it[color] = request.color?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[icon] = request.icon?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[isFavorite] = request.isFavorite
                it[notes] = request.notes?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[isActive] = request.isActive
                it[createdAtEpochMs] = now
            }
        }
    }

    fun findProcedureTypeById(id: Int): ProcedureTypeRow? =
        transaction {
            ProcedureTypesTable
                .selectAll()
                .where { ProcedureTypesTable.id eq id }
                .map { row -> procedureTypeFromRow(row) }
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
                .map { row -> procedureTypeFromRow(row) }
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
                .map { row -> procedureTypeFromRow(row) }
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
                it[categoryId] = request.categoryId
                it[currency] = request.currency.ifBlank { "BOB" }
                it[color] = request.color?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[icon] = request.icon?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[isFavorite] = request.isFavorite
                it[notes] = request.notes?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[isActive] = request.isActive
                it[updatedAtEpochMs] = System.currentTimeMillis()
            }
        }
    }

    fun toggleFavoriteProcedureType(procedureTypeId: Int) {
        transaction {
            val current =
                ProcedureTypesTable
                    .selectAll()
                    .where { ProcedureTypesTable.id eq procedureTypeId }
                    .firstOrNull() ?: return@transaction
            ProcedureTypesTable.update({ ProcedureTypesTable.id eq procedureTypeId }) {
                it[isFavorite] = !current[ProcedureTypesTable.isFavorite]
                it[updatedAtEpochMs] = System.currentTimeMillis()
            }
        }
    }

    fun archiveProcedureType(procedureTypeId: Int) {
        transaction {
            val updated =
                ProcedureTypesTable.update({ ProcedureTypesTable.id eq procedureTypeId }) {
                    it[isArchived] = true
                    it[updatedAtEpochMs] = System.currentTimeMillis()
                }
            require(updated > 0) { "No se encontró el tipo de procedimiento." }
        }
    }

    fun restoreProcedureType(procedureTypeId: Int) {
        transaction {
            val updated =
                ProcedureTypesTable.update({ ProcedureTypesTable.id eq procedureTypeId }) {
                    it[isArchived] = false
                    it[updatedAtEpochMs] = System.currentTimeMillis()
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

    // ── Treatment Categories CRUD ──────────────────────────────────────────

    private fun categoryFromRow(row: ResultRow): TreatmentCategory =
        TreatmentCategory(
            id = row[TreatmentCategoriesTable.id],
            name = row[TreatmentCategoriesTable.name],
            icon = row[TreatmentCategoriesTable.icon],
            color = row[TreatmentCategoriesTable.color],
            sortOrder = row[TreatmentCategoriesTable.sortOrder],
            isActive = row[TreatmentCategoriesTable.isActive],
            isArchived = row[TreatmentCategoriesTable.isArchived],
            createdAtEpochMs = row[TreatmentCategoriesTable.createdAtEpochMs],
            updatedAtEpochMs = row[TreatmentCategoriesTable.updatedAtEpochMs],
        )

    fun listCategories(): List<TreatmentCategory> =
        transaction {
            TreatmentCategoriesTable
                .selectAll()
                .where { TreatmentCategoriesTable.isArchived eq false }
                .orderBy(TreatmentCategoriesTable.sortOrder to SortOrder.ASC)
                .orderBy(TreatmentCategoriesTable.name to SortOrder.ASC)
                .map { row -> categoryFromRow(row) }
        }

    fun listAllCategoriesIncludingArchived(): List<TreatmentCategory> =
        transaction {
            TreatmentCategoriesTable
                .selectAll()
                .orderBy(TreatmentCategoriesTable.sortOrder to SortOrder.ASC)
                .orderBy(TreatmentCategoriesTable.name to SortOrder.ASC)
                .map { row -> categoryFromRow(row) }
        }

    fun findCategoryById(id: Int): TreatmentCategory? =
        transaction {
            TreatmentCategoriesTable
                .selectAll()
                .where { TreatmentCategoriesTable.id eq id }
                .map { row -> categoryFromRow(row) }
                .singleOrNull()
        }

    fun findCategoryByName(name: String, excludeId: Int? = null): TreatmentCategory? =
        transaction {
            TreatmentCategoriesTable
                .selectAll()
                .where {
                    (TreatmentCategoriesTable.name.lowerCase() eq name.trim().lowercase()) and
                        (if (excludeId != null) TreatmentCategoriesTable.id neq excludeId else Op.TRUE)
                }
                .map { row -> categoryFromRow(row) }
                .singleOrNull()
        }

    fun registerCategory(request: CategoryRegisterRequest): Int {
        val name = request.name.trim()
        require(name.isNotEmpty()) { "El nombre de la categoría es obligatorio." }
        val now = System.currentTimeMillis()
        return transaction {
            TreatmentCategoriesTable.insert {
                it[TreatmentCategoriesTable.name] = name
                it[icon] = request.icon?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[color] = request.color?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[sortOrder] = request.sortOrder
                it[isActive] = request.isActive
                it[createdAtEpochMs] = now
            } get TreatmentCategoriesTable.id
        }
    }

    fun updateCategory(categoryId: Int, request: CategoryUpdateRequest) {
        val name = request.name.trim()
        require(name.isNotEmpty()) { "El nombre de la categoría es obligatorio." }
        transaction {
            val exists =
                TreatmentCategoriesTable
                    .selectAll()
                    .where { TreatmentCategoriesTable.id eq categoryId }
                    .count() > 0
            require(exists) { "No se encontró la categoría." }
            TreatmentCategoriesTable.update({ TreatmentCategoriesTable.id eq categoryId }) {
                it[TreatmentCategoriesTable.name] = name
                it[icon] = request.icon?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[color] = request.color?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[sortOrder] = request.sortOrder
                it[isActive] = request.isActive
                it[updatedAtEpochMs] = System.currentTimeMillis()
            }
        }
    }

    fun archiveCategory(categoryId: Int) {
        transaction {
            val updated =
                TreatmentCategoriesTable.update({ TreatmentCategoriesTable.id eq categoryId }) {
                    it[isArchived] = true
                    it[updatedAtEpochMs] = System.currentTimeMillis()
                }
            require(updated > 0) { "No se encontró la categoría." }
        }
    }

    fun restoreCategory(categoryId: Int) {
        transaction {
            val updated =
                TreatmentCategoriesTable.update({ TreatmentCategoriesTable.id eq categoryId }) {
                    it[isArchived] = false
                    it[updatedAtEpochMs] = System.currentTimeMillis()
                }
            require(updated > 0) { "No se encontró la categoría." }
        }
    }

    fun hardDeleteCategory(categoryId: Int) {
        transaction {
            val hasProcedures =
                ProcedureTypesTable
                    .selectAll()
                    .where { ProcedureTypesTable.categoryId eq categoryId }
                    .count() > 0
            if (hasProcedures) {
                throw IllegalStateException("No se puede eliminar: existen tratamientos vinculados a esta categoría. Archívela en su lugar.")
            }
            val deleted =
                TreatmentCategoriesTable.deleteWhere { TreatmentCategoriesTable.id eq categoryId }
            require(deleted > 0) { "No se encontró la categoría." }
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

    fun listTreatmentFacilities(): List<TreatmentFacilityRow> =
        transaction {
            TreatmentFacilitiesTable
                .selectAll()
                .where { TreatmentFacilitiesTable.isActive eq true }
                .orderBy(
                    TreatmentFacilitiesTable.categoryKey to SortOrder.ASC,
                    TreatmentFacilitiesTable.displayName to SortOrder.ASC,
                )
                .map { row ->
                    TreatmentFacilityRow(
                        id = row[TreatmentFacilitiesTable.id],
                        code = row[TreatmentFacilitiesTable.facilityCode],
                        categoryKey = row[TreatmentFacilitiesTable.categoryKey],
                        displayName = row[TreatmentFacilitiesTable.displayName],
                        isActive = row[TreatmentFacilitiesTable.isActive],
                    )
                }
        }

    fun findInventoryLine(consultoryId: Int, facilityId: Int): InventoryLineRow? =
        listInventoryLines().firstOrNull { it.consultoryId == consultoryId && it.facilityId == facilityId }

    /** Registra una nueva línea de stock en un consultorio, con su movimiento inicial. */
    fun registerInventoryLine(
        consultoryId: Int,
        facilityId: Int,
        quantity: Int,
        note: String? = null,
    ): Int {
        require(quantity >= 0) { "La cantidad inicial no puede ser negativa." }
        require(findInventoryLine(consultoryId, facilityId) == null) {
            "Ese insumo ya existe en el consultorio seleccionado."
        }
        return transaction {
            val lineId =
                MaterialInventoryLinesTable.insert {
                    it[MaterialInventoryLinesTable.consultoryId] = consultoryId
                    it[MaterialInventoryLinesTable.facilityId] = facilityId
                    it[MaterialInventoryLinesTable.quantity] = quantity
                } get MaterialInventoryLinesTable.id
            InventoryMovementsTable.insert {
                it[InventoryMovementsTable.consultoryId] = consultoryId
                it[InventoryMovementsTable.facilityId] = facilityId
                it[InventoryMovementsTable.quantityChange] = quantity
                it[InventoryMovementsTable.type] = "RESTOCK"
                it[InventoryMovementsTable.note] = note?.trim()?.takeIf { n -> n.isNotEmpty() } ?: "Registro inicial de stock"
                it[InventoryMovementsTable.createdAtEpochMs] = System.currentTimeMillis()
            }
            lineId
        }
    }

    /**
     * Aplica un cambio de cantidad a una línea de stock y registra un movimiento de ajuste.
     * El resultado no puede quedar negativo.
     */
    fun adjustInventoryStock(
        consultoryId: Int,
        facilityId: Int,
        quantityDelta: Int,
        note: String? = null,
    ): Int {
        require(quantityDelta != 0) { "El ajuste no puede ser cero." }
        return applyQuantityChange(
            consultoryId = consultoryId,
            facilityId = facilityId,
            quantityDelta = quantityDelta,
            movementType = "ADJUSTMENT",
            note = note?.trim()?.takeIf { n -> n.isNotEmpty() } ?: "Ajuste manual de stock",
        )
    }

    /** Transfiere `quantity` unidades entre consultorios, creando la línea destino si no existe. */
    fun transferInventoryStock(
        fromConsultoryId: Int,
        fromFacilityId: Int,
        toConsultoryId: Int,
        quantity: Int,
        note: String? = null,
    ) {
        require(quantity > 0) { "La cantidad a transferir debe ser mayor a cero." }
        require(fromConsultoryId != toConsultoryId) {
            "El consultorio de origen y destino no pueden ser el mismo."
        }
        transaction {
            val source =
                MaterialInventoryLinesTable
                    .selectAll()
                    .where {
                        (MaterialInventoryLinesTable.consultoryId eq fromConsultoryId) and
                            (MaterialInventoryLinesTable.facilityId eq fromFacilityId)
                    }
                    .firstOrNull()
                    ?: throw IllegalArgumentException("La línea de stock de origen no existe.")
            val sourceQty = source[MaterialInventoryLinesTable.quantity]
            require(sourceQty >= quantity) {
                "Stock insuficiente en origen: solo hay $sourceQty unidades."
            }
            val now = System.currentTimeMillis()

            MaterialInventoryLinesTable.update({ MaterialInventoryLinesTable.id eq source[MaterialInventoryLinesTable.id] }) {
                it[MaterialInventoryLinesTable.quantity] = sourceQty - quantity
            }
            InventoryMovementsTable.insert {
                it[InventoryMovementsTable.consultoryId] = fromConsultoryId
                it[InventoryMovementsTable.facilityId] = fromFacilityId
                it[InventoryMovementsTable.quantityChange] = -quantity
                it[InventoryMovementsTable.type] = "TRANSFER"
                it[InventoryMovementsTable.note] = note?.trim()?.takeIf { n -> n.isNotEmpty() }
                    ?: "Transferencia hacia el consultorio destino"
                it[InventoryMovementsTable.createdAtEpochMs] = now
            }

            val target =
                MaterialInventoryLinesTable
                    .selectAll()
                    .where {
                        (MaterialInventoryLinesTable.consultoryId eq toConsultoryId) and
                            (MaterialInventoryLinesTable.facilityId eq fromFacilityId)
                    }
                    .firstOrNull()
            val targetQty = target?.get(MaterialInventoryLinesTable.quantity) ?: 0
            if (target != null) {
                MaterialInventoryLinesTable.update({ MaterialInventoryLinesTable.id eq target[MaterialInventoryLinesTable.id] }) {
                    it[MaterialInventoryLinesTable.quantity] = targetQty + quantity
                }
            } else {
                MaterialInventoryLinesTable.insert {
                    it[MaterialInventoryLinesTable.consultoryId] = toConsultoryId
                    it[MaterialInventoryLinesTable.facilityId] = fromFacilityId
                    it[MaterialInventoryLinesTable.quantity] = quantity
                }
            }
            InventoryMovementsTable.insert {
                it[InventoryMovementsTable.consultoryId] = toConsultoryId
                it[InventoryMovementsTable.facilityId] = fromFacilityId
                it[InventoryMovementsTable.quantityChange] = quantity
                it[InventoryMovementsTable.type] = "TRANSFER"
                it[InventoryMovementsTable.note] = note?.trim()?.takeIf { n -> n.isNotEmpty() }
                    ?: "Transferencia desde el consultorio origen"
                it[InventoryMovementsTable.createdAtEpochMs] = now
            }
        }
    }

    private fun applyQuantityChange(
        consultoryId: Int,
        facilityId: Int,
        quantityDelta: Int,
        movementType: String,
        note: String,
    ): Int =
        transaction {
            val line =
                MaterialInventoryLinesTable
                    .selectAll()
                    .where {
                        (MaterialInventoryLinesTable.consultoryId eq consultoryId) and
                            (MaterialInventoryLinesTable.facilityId eq facilityId)
                    }
                    .firstOrNull()
                    ?: throw IllegalArgumentException("La línea de stock no existe.")
            val current = line[MaterialInventoryLinesTable.quantity]
            val next = current + quantityDelta
            require(next >= 0) { "El stock no puede quedar negativo (actual: $current)." }
            MaterialInventoryLinesTable.update({ MaterialInventoryLinesTable.id eq line[MaterialInventoryLinesTable.id] }) {
                it[MaterialInventoryLinesTable.quantity] = next
            }
            InventoryMovementsTable.insert {
                it[InventoryMovementsTable.consultoryId] = consultoryId
                it[InventoryMovementsTable.facilityId] = facilityId
                it[InventoryMovementsTable.quantityChange] = quantityDelta
                it[InventoryMovementsTable.type] = movementType
                it[InventoryMovementsTable.note] = note
                it[InventoryMovementsTable.createdAtEpochMs] = System.currentTimeMillis()
            }
            next
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

    /**
     * Reporte de ingreso y agendamiento para un rango de fechas, calculado desde las tablas reales.
     * `paid_at`/`scheduled_at` son cadenas ISO, por lo que el rango se evalúa por día.
     */
    fun reportsOverview(startDate: LocalDate, endDate: LocalDate): ReportsOverview {
        val safeStart = if (startDate.isAfter(endDate)) endDate else startDate
        val safeEnd = if (startDate.isAfter(endDate)) startDate else endDate
        return transaction {
            data class PaymentAccum(
                val day: LocalDate,
                val amount: Double,
                val methodLabel: String,
                val procedureName: String?,
            )

            val procedureNames =
                ProcedureTypesTable
                    .selectAll()
                    .associate { it[ProcedureTypesTable.id] to it[ProcedureTypesTable.name] }

            val payments =
                PaymentsTable
                    .selectAll()
                    .mapNotNull { row ->
                        val day = parseReportDay(row[PaymentsTable.paidAt])
                        if (day == null || day.isBefore(safeStart) || day.isAfter(safeEnd)) return@mapNotNull null
                        PaymentAccum(
                            day = day,
                            amount = row[PaymentsTable.amount],
                            methodLabel = PaymentMethod.fromDb(row[PaymentsTable.method])?.displayLabel ?: "Otro",
                            procedureName =
                                row[PaymentsTable.procedureTypeId]?.let { procedureNames[it] },
                        )
                    }

            val appointments =
                AppointmentsTable
                    .selectAll()
                    .mapNotNull { row ->
                        val day = parseReportDay(row[AppointmentsTable.scheduledAt])
                        if (day == null || day.isBefore(safeStart) || day.isAfter(safeEnd)) return@mapNotNull null
                        AppointmentStatus.fromDb(row[AppointmentsTable.status]) to day
                    }

            val zone = ZoneId.systemDefault()
            val startMs = safeStart.atStartOfDay(zone).toInstant().toEpochMilli()
            val endExclusiveMs = safeEnd.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val newPatients =
                PatientsTable
                    .selectAll()
                    .where { (PatientsTable.createdAtEpochMs greaterEq startMs) and (PatientsTable.createdAtEpochMs less endExclusiveMs) }
                    .count()
                    .toInt()

            val dayRange =
                (0 until (java.time.temporal.ChronoUnit.DAYS.between(safeStart, safeEnd).toInt() + 1))
                    .map { safeStart.plusDays(it.toLong()) }

            val dailyRevenue =
                dayRange.map { day -> RevenueDayPoint(day, payments.filter { it.day == day }.sumOf { it.amount }) }
            val appointmentsPerDay =
                dayRange.map { day -> AppointmentDayPoint(day, appointments.count { it.second == day }) }

            val revenueByMethod =
                payments
                    .groupBy { it.methodLabel }
                    .map { (method, rows) ->
                        RevenueByMethodSlice(method, rows.size, rows.sumOf { it.amount })
                    }
                    .sortedByDescending { it.revenue }

            val appointmentByStatus =
                appointments
                    .groupBy { it.first }
                    .map { (status, rows) -> AppointmentStatusSlice(status, rows.size) }
                    .sortedByDescending { it.count }

            val topProcedures =
                payments
                    .filter { it.procedureName != null }
                    .groupBy { it.procedureName!! }
                    .map { (name, rows) -> TopProcedureRow(name, rows.size, rows.sumOf { it.amount }) }
                    .sortedWith(compareByDescending<TopProcedureRow> { it.revenue }.thenBy { it.name })
                    .take(5)

            ReportsOverview(
                startDate = safeStart,
                endDate = safeEnd,
                totalRevenue = payments.sumOf { it.amount },
                paymentCount = payments.size,
                newPatientsCount = newPatients,
                appointmentCount = appointments.size,
                completedCount = appointments.count { it.first == AppointmentStatus.COMPLETED },
                cancelledCount = appointments.count { it.first == AppointmentStatus.CANCELLED },
                dailyRevenue = dailyRevenue,
                appointmentsPerDay = appointmentsPerDay,
                revenueByMethod = revenueByMethod,
                appointmentByStatus = appointmentByStatus,
                topProcedures = topProcedures,
            )
        }
    }

    /** Día (`LocalDate`) desde una cadena ISO fecha-hora o solo fecha. */
    private fun parseReportDay(value: String): LocalDate? {
        val t = value.trim()
        if (t.isEmpty()) return null
        return try {
            if (t.length <= 10) {
                LocalDate.parse(t, DateTimeFormatter.ISO_LOCAL_DATE)
            } else {
                LocalDateTime.parse(t, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate()
            }
        } catch (_: DateTimeParseException) {
            try {
                LocalDateTime.parse(t, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).toLocalDate()
            } catch (_: DateTimeParseException) {
                null
            }
        }
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
                        " · pagado Bs ${"%.2f".format(amountPaid)} de Bs ${"%.2f".format(treatment.totalPrice)}"
                    } else {
                        " · Bs ${"%.2f".format(treatment.totalPrice)}"
                    }
                TreatmentPaymentOption(
                    performedActionId = treatment.id,
                    procedureTypeId = treatment.procedureTypeId,
                    label = "${treatment.procedureTypeName}$paidLabel · pendiente Bs ${"%.2f".format(remaining)}",
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
                    detail = "Pago #$paymentId · Bs ${"%.2f".format(request.amount)}",
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
    ): List<PatientTreatmentRow> =
        transaction {
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
            PerformedActionsTable
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
        transaction {
            if (procedureTypeId == null) {
                PerformedActionsTable.deleteWhere { PerformedActionsTable.appointmentId eq appointmentId }
                return@transaction
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
    }

    private fun resolveProcedureStandardPrice(procedureTypeId: Int): Double? =
        transaction {
            ProcedureTypesTable
                .selectAll()
                .where { ProcedureTypesTable.id eq procedureTypeId }
                .firstOrNull()
                ?.get(ProcedureTypesTable.standardPrice)
        }

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
        transaction {
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
        }

    private fun listAppointmentAuditInternal(appointmentId: Int): List<AppointmentAuditEntry> =
        transaction {
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
        }

    private fun buildPaymentSummaryInternal(
        appointmentId: Int,
        appt: AppointmentRow,
    ): AppointmentPaymentSummary {
        return transaction {
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
            AppointmentPaymentSummary(
                treatmentCost = treatmentCost,
                amountPaid = paid,
                remainingBalance = (cost - paid).coerceAtLeast(0.0),
                status = status,
                paymentIds = paymentIds,
            )
        }
    }

    private fun appendAppointmentAudit(
        appointmentId: Int,
        action: String,
        detail: String? = null,
        actorLabel: String = "Recepción",
    ) {
        transaction {
            AppointmentAuditLogTable.insert {
                it[AppointmentAuditLogTable.appointmentId] = appointmentId
                it[AppointmentAuditLogTable.action] = action
                it[AppointmentAuditLogTable.actorLabel] = actorLabel
                it[AppointmentAuditLogTable.detail] = detail?.trim()?.takeIf { value -> value.isNotEmpty() }
                it[createdAtEpochMs] = System.currentTimeMillis()
            }
        }
    }

    // ── Inventory Product Management ──────────────────────────────────────────

    private fun inventoryProductFromRow(row: ResultRow): InventoryProduct {
        val catName = row.getOrNull(InventoryCategoriesTable.name)
        val supName = row.getOrNull(SuppliersTable.name)
        return InventoryProduct(
            id = row[InventoryProductsTable.id],
            name = row[InventoryProductsTable.name],
            code = row[InventoryProductsTable.code],
            description = row[InventoryProductsTable.description],
            categoryId = row[InventoryProductsTable.categoryId],
            categoryName = catName,
            unit = row[InventoryProductsTable.unit],
            purchasePrice = row[InventoryProductsTable.purchasePrice],
            sellingPrice = row[InventoryProductsTable.sellingPrice],
            currentStock = row[InventoryProductsTable.currentStock],
            minStock = row[InventoryProductsTable.minStock],
            maxStock = row[InventoryProductsTable.maxStock],
            supplierId = row[InventoryProductsTable.supplierId],
            supplierName = supName,
            expirationDate = row[InventoryProductsTable.expirationDate],
            barcode = row[InventoryProductsTable.barcode],
            color = row[InventoryProductsTable.color],
            icon = row[InventoryProductsTable.icon],
            notes = row[InventoryProductsTable.notes],
            isActive = row[InventoryProductsTable.isActive],
            isArchived = row[InventoryProductsTable.isArchived],
            createdAtEpochMs = row[InventoryProductsTable.createdAtEpochMs],
            updatedAtEpochMs = row[InventoryProductsTable.updatedAtEpochMs],
        )
    }

    private fun inventoryCategoryFromRow(row: ResultRow): InventoryProductCategory =
        InventoryProductCategory(
            id = row[InventoryCategoriesTable.id],
            name = row[InventoryCategoriesTable.name],
            description = row[InventoryCategoriesTable.description],
            icon = row[InventoryCategoriesTable.icon],
            color = row[InventoryCategoriesTable.color],
            sortOrder = row[InventoryCategoriesTable.sortOrder],
            isActive = row[InventoryCategoriesTable.isActive],
            isArchived = row[InventoryCategoriesTable.isArchived],
            createdAtEpochMs = row[InventoryCategoriesTable.createdAtEpochMs],
            updatedAtEpochMs = row[InventoryCategoriesTable.updatedAtEpochMs],
        )

    private fun supplierFromRow(row: ResultRow): Supplier =
        Supplier(
            id = row[SuppliersTable.id],
            name = row[SuppliersTable.name],
            contactName = row[SuppliersTable.contactName],
            phone = row[SuppliersTable.phone],
            email = row[SuppliersTable.email],
            address = row[SuppliersTable.address],
            notes = row[SuppliersTable.notes],
            isActive = row[SuppliersTable.isActive],
            isArchived = row[SuppliersTable.isArchived],
            createdAtEpochMs = row[SuppliersTable.createdAtEpochMs],
            updatedAtEpochMs = row[SuppliersTable.updatedAtEpochMs],
        )

    // ── Inventory Categories ──────────────────────────────────────────────────

    fun listInventoryCategories(): List<InventoryProductCategory> =
        transaction {
            InventoryCategoriesTable
                .selectAll()
                .where { InventoryCategoriesTable.isActive eq true and (InventoryCategoriesTable.isArchived eq false) }
                .orderBy(InventoryCategoriesTable.sortOrder to SortOrder.ASC, InventoryCategoriesTable.name to SortOrder.ASC)
                .map { inventoryCategoryFromRow(it) }
        }

    fun listAllInventoryCategoriesIncludingArchived(): List<InventoryProductCategory> =
        transaction {
            InventoryCategoriesTable
                .selectAll()
                .orderBy(InventoryCategoriesTable.sortOrder to SortOrder.ASC, InventoryCategoriesTable.name to SortOrder.ASC)
                .map { inventoryCategoryFromRow(it) }
        }

    fun findInventoryCategoryById(id: Int): InventoryProductCategory? =
        transaction {
            InventoryCategoriesTable
                .selectAll()
                .where { InventoryCategoriesTable.id eq id }
                .firstOrNull()
                ?.let { inventoryCategoryFromRow(it) }
        }

    fun findInventoryCategoryByName(name: String, excludeId: Int? = null): InventoryProductCategory? =
        transaction {
            var op = InventoryCategoriesTable.name.lowerCase() eq name.trim().lowercase()
            if (excludeId != null) op = op and (InventoryCategoriesTable.id neq excludeId)
            InventoryCategoriesTable.selectAll().where(op).firstOrNull()?.let { inventoryCategoryFromRow(it) }
        }

    fun registerInventoryCategory(request: InventoryCategoryRegisterRequest): Int =
        transaction {
            InventoryCategoriesTable.insert {
                it[name] = request.name.trim()
                it[description] = request.description?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[icon] = request.icon?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[color] = request.color?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[sortOrder] = request.sortOrder
                it[isActive] = request.isActive
                it[createdAtEpochMs] = System.currentTimeMillis()
            } get InventoryCategoriesTable.id
        }

    fun updateInventoryCategory(id: Int, request: InventoryCategoryUpdateRequest) {
        transaction {
            InventoryCategoriesTable.update({ InventoryCategoriesTable.id eq id }) {
                it[name] = request.name.trim()
                it[description] = request.description?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[icon] = request.icon?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[color] = request.color?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[sortOrder] = request.sortOrder
                it[isActive] = request.isActive
                it[updatedAtEpochMs] = System.currentTimeMillis()
            }
        }
    }

    fun archiveInventoryCategory(id: Int) {
        transaction { InventoryCategoriesTable.update({ InventoryCategoriesTable.id eq id }) { it[isArchived] = true } }
    }

    fun restoreInventoryCategory(id: Int) {
        transaction { InventoryCategoriesTable.update({ InventoryCategoriesTable.id eq id }) { it[isArchived] = false } }
    }

    fun hardDeleteInventoryCategory(id: Int) {
        transaction {
            val hasProducts = InventoryProductsTable.selectAll()
                .where { InventoryProductsTable.categoryId eq id }
                .count() > 0
            if (hasProducts) throw IllegalStateException("No se puede eliminar: existen productos vinculados a esta categoría.")
            InventoryCategoriesTable.deleteWhere { InventoryCategoriesTable.id eq id }
        }
    }

    // ── Suppliers ─────────────────────────────────────────────────────────────

    fun listSuppliers(): List<Supplier> =
        transaction {
            SuppliersTable
                .selectAll()
                .where { SuppliersTable.isActive eq true and (SuppliersTable.isArchived eq false) }
                .orderBy(SuppliersTable.name to SortOrder.ASC)
                .map { supplierFromRow(it) }
        }

    fun listAllSuppliersIncludingArchived(): List<Supplier> =
        transaction {
            SuppliersTable
                .selectAll()
                .orderBy(SuppliersTable.name to SortOrder.ASC)
                .map { supplierFromRow(it) }
        }

    fun findSupplierById(id: Int): Supplier? =
        transaction {
            SuppliersTable.selectAll().where { SuppliersTable.id eq id }.firstOrNull()?.let { supplierFromRow(it) }
        }

    fun findSupplierByName(name: String, excludeId: Int? = null): Supplier? =
        transaction {
            var op = SuppliersTable.name.lowerCase() eq name.trim().lowercase()
            if (excludeId != null) op = op and (SuppliersTable.id neq excludeId)
            SuppliersTable.selectAll().where(op).firstOrNull()?.let { supplierFromRow(it) }
        }

    fun registerSupplier(request: SupplierRegisterRequest): Int =
        transaction {
            require(findSupplierByName(request.name) == null) {
                "Ya existe un proveedor con el nombre \"${request.name.trim()}\"."
            }
            SuppliersTable.insert {
                it[name] = request.name.trim()
                it[contactName] = request.contactName?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[phone] = request.phone?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[email] = request.email?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[address] = request.address?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[notes] = request.notes?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[isActive] = request.isActive
                it[createdAtEpochMs] = System.currentTimeMillis()
            } get SuppliersTable.id
        }

    fun updateSupplier(id: Int, request: SupplierUpdateRequest) {
        transaction {
            require(findSupplierByName(request.name, excludeId = id) == null) {
                "Ya existe un proveedor con el nombre \"${request.name.trim()}\"."
            }
            SuppliersTable.update({ SuppliersTable.id eq id }) {
                it[name] = request.name.trim()
                it[contactName] = request.contactName?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[phone] = request.phone?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[email] = request.email?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[address] = request.address?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[notes] = request.notes?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[isActive] = request.isActive
                it[updatedAtEpochMs] = System.currentTimeMillis()
            }
        }
    }

    fun archiveSupplier(id: Int) {
        transaction { SuppliersTable.update({ SuppliersTable.id eq id }) { it[isArchived] = true } }
    }

    fun restoreSupplier(id: Int) {
        transaction { SuppliersTable.update({ SuppliersTable.id eq id }) { it[isArchived] = false } }
    }

    fun hardDeleteSupplier(id: Int) {
        transaction {
            val hasProducts = InventoryProductsTable.selectAll()
                .where { InventoryProductsTable.supplierId eq id }
                .count() > 0
            if (hasProducts) throw IllegalStateException("No se puede eliminar: existen productos vinculados a este proveedor.")
            val hasPendingOrders = PurchaseOrdersTable.selectAll()
                .where { (PurchaseOrdersTable.supplierId eq id) and (PurchaseOrdersTable.status eq "PENDING") }
                .count() > 0
            if (hasPendingOrders) throw IllegalStateException("No se puede eliminar: el proveedor tiene pedidos pendientes.")
            SuppliersTable.deleteWhere { SuppliersTable.id eq id }
        }
    }

    // ── Purchase Orders ──────────────────────────────────────────────────────

    fun listPurchaseOrders(): List<PurchaseOrderRow> =
        transaction {
            (PurchaseOrdersTable leftJoin SuppliersTable)
                .selectAll()
                .orderBy(PurchaseOrdersTable.orderDateEpochMs to SortOrder.DESC)
                .map { row ->
                    val orderId = row[PurchaseOrdersTable.id]
                    val itemCount =
                        PurchaseOrderItemsTable
                            .selectAll()
                            .where { PurchaseOrderItemsTable.orderId eq orderId }
                            .count()
                            .toInt()
                    PurchaseOrderRow(
                        id = orderId,
                        supplierId = row[PurchaseOrdersTable.supplierId],
                        supplierName = row.getOrNull(SuppliersTable.name),
                        status = row[PurchaseOrdersTable.status],
                        orderDateEpochMs = row[PurchaseOrdersTable.orderDateEpochMs],
                        receivedAtEpochMs = row[PurchaseOrdersTable.receivedAtEpochMs],
                        notes = row[PurchaseOrdersTable.notes],
                        totalCost = row[PurchaseOrdersTable.totalCost],
                        itemCount = itemCount,
                    )
                }
        }

    fun getPurchaseOrderItems(orderId: Int): List<PurchaseOrderItemRow> =
        transaction {
            PurchaseOrderItemsTable
                .selectAll()
                .where { PurchaseOrderItemsTable.orderId eq orderId }
                .orderBy(PurchaseOrderItemsTable.id to SortOrder.ASC)
                .map { row ->
                    PurchaseOrderItemRow(
                        orderItemId = row[PurchaseOrderItemsTable.id],
                        orderId = row[PurchaseOrderItemsTable.orderId],
                        consultoryId = row[PurchaseOrderItemsTable.consultoryId],
                        facilityId = row[PurchaseOrderItemsTable.facilityId],
                        quantity = row[PurchaseOrderItemsTable.quantity],
                        unitCost = row[PurchaseOrderItemsTable.unitCost],
                    )
                }
        }

    fun registerPurchaseOrder(request: PurchaseOrderRegisterRequest): Int {
        require(request.items.isNotEmpty()) { "El pedido debe incluir al menos una línea." }
        val validItems = request.items.filter { it.quantity > 0 }
        require(validItems.isNotEmpty()) { "Al menos una línea debe tener cantidad mayor a cero." }
        return transaction {
            val now = System.currentTimeMillis()
            val orderId =
                PurchaseOrdersTable.insert {
                    it[supplierId] = request.supplierId
                    it[status] = "PENDING"
                    it[orderDateEpochMs] = now
                    it[notes] = request.notes?.trim()?.takeIf { n -> n.isNotEmpty() }
                    it[totalCost] = validItems.sumOf { item -> item.quantity.toDouble() * item.unitCost }
                } get PurchaseOrdersTable.id
            validItems.forEach { item ->
                PurchaseOrderItemsTable.insert {
                    it[PurchaseOrderItemsTable.orderId] = orderId
                    it[consultoryId] = item.consultoryId
                    it[facilityId] = item.facilityId
                    it[quantity] = item.quantity
                    it[unitCost] = item.unitCost
                }
            }
            orderId
        }
    }

    /** Marca el pedido como recibido y acredita cada línea al stock del consultorio (crea la línea si no existe). */
    fun receivePurchaseOrder(orderId: Int) {
        transaction {
            val order =
                PurchaseOrdersTable
                    .selectAll()
                    .where { PurchaseOrdersTable.id eq orderId }
                    .firstOrNull()
                    ?: throw IllegalArgumentException("El pedido no existe.")
            require(order[PurchaseOrdersTable.status] == "PENDING") { "El pedido ya fue recibido." }
            val now = System.currentTimeMillis()
            PurchaseOrderItemsTable
                .selectAll()
                .where { PurchaseOrderItemsTable.orderId eq orderId }
                .forEach { item ->
                    val consultoryId = item[PurchaseOrderItemsTable.consultoryId]
                    val facilityId = item[PurchaseOrderItemsTable.facilityId]
                    val quantity = item[PurchaseOrderItemsTable.quantity]
                    val line =
                        MaterialInventoryLinesTable
                            .selectAll()
                            .where {
                                (MaterialInventoryLinesTable.consultoryId eq consultoryId) and
                                    (MaterialInventoryLinesTable.facilityId eq facilityId)
                            }
                            .firstOrNull()
                    if (line != null) {
                        MaterialInventoryLinesTable.update({ MaterialInventoryLinesTable.id eq line[MaterialInventoryLinesTable.id] }) {
                            it[MaterialInventoryLinesTable.quantity] = line[MaterialInventoryLinesTable.quantity] + quantity
                        }
                    } else {
                        MaterialInventoryLinesTable.insert {
                            it[MaterialInventoryLinesTable.consultoryId] = consultoryId
                            it[MaterialInventoryLinesTable.facilityId] = facilityId
                            it[MaterialInventoryLinesTable.quantity] = quantity
                        }
                    }
                    InventoryMovementsTable.insert {
                        it[InventoryMovementsTable.consultoryId] = consultoryId
                        it[InventoryMovementsTable.facilityId] = facilityId
                        it[InventoryMovementsTable.quantityChange] = quantity
                        it[InventoryMovementsTable.type] = "RESTOCK"
                        it[InventoryMovementsTable.note] = "Recepción de pedido #$orderId"
                        it[InventoryMovementsTable.createdAtEpochMs] = now
                    }
                }
            PurchaseOrdersTable.update({ PurchaseOrdersTable.id eq orderId }) {
                it[status] = "RECEIVED"
                it[receivedAtEpochMs] = now
            }
        }
    }

    fun deletePurchaseOrder(orderId: Int) {
        transaction {
            val order =
                PurchaseOrdersTable
                    .selectAll()
                    .where { PurchaseOrdersTable.id eq orderId }
                    .firstOrNull()
                    ?: throw IllegalArgumentException("El pedido no existe.")
            require(order[PurchaseOrdersTable.status] == "PENDING") { "Solo se pueden eliminar pedidos pendientes." }
            PurchaseOrderItemsTable.deleteWhere { PurchaseOrderItemsTable.orderId eq orderId }
            PurchaseOrdersTable.deleteWhere { PurchaseOrdersTable.id eq orderId }
        }
    }

    // ── Inventory Products ────────────────────────────────────────────────────

    private fun productBaseQuery() =
        InventoryProductsTable
            .leftJoin(InventoryCategoriesTable, { InventoryProductsTable.categoryId }, { InventoryCategoriesTable.id })
            .leftJoin(SuppliersTable, { InventoryProductsTable.supplierId }, { SuppliersTable.id })

    fun listInventoryProducts(): List<InventoryProduct> =
        transaction {
            productBaseQuery()
                .selectAll()
                .where { InventoryProductsTable.isActive eq true and (InventoryProductsTable.isArchived eq false) }
                .orderBy(InventoryProductsTable.name to SortOrder.ASC)
                .map { inventoryProductFromRow(it) }
        }

    fun listAllInventoryProductsIncludingArchived(): List<InventoryProduct> =
        transaction {
            productBaseQuery()
                .selectAll()
                .orderBy(InventoryProductsTable.name to SortOrder.ASC)
                .map { inventoryProductFromRow(it) }
        }

    fun findInventoryProductById(id: Int): InventoryProduct? =
        transaction {
            productBaseQuery()
                .selectAll()
                .where { InventoryProductsTable.id eq id }
                .firstOrNull()
                ?.let { inventoryProductFromRow(it) }
        }

    fun findInventoryProductByName(name: String, excludeId: Int? = null): InventoryProduct? =
        transaction {
            var op = InventoryProductsTable.name.lowerCase() eq name.trim().lowercase()
            if (excludeId != null) op = op and (InventoryProductsTable.id neq excludeId)
            productBaseQuery().selectAll().where(op).firstOrNull()?.let { inventoryProductFromRow(it) }
        }

    fun findInventoryProductByCode(code: String, excludeId: Int? = null): InventoryProduct? =
        transaction {
            var op = InventoryProductsTable.code.lowerCase() eq code.trim().lowercase()
            if (excludeId != null) op = op and (InventoryProductsTable.id neq excludeId)
            productBaseQuery().selectAll().where(op).firstOrNull()?.let { inventoryProductFromRow(it) }
        }

    fun searchInventoryProducts(query: String): List<InventoryProduct> =
        transaction {
            val q = "%${query.trim().lowercase()}%"
            productBaseQuery()
                .selectAll()
                .where {
                    (InventoryProductsTable.isActive eq true) and (InventoryProductsTable.isArchived eq false) and (
                        (InventoryProductsTable.name.lowerCase() like q) or
                            (InventoryProductsTable.code.lowerCase() like q) or
                            (InventoryProductsTable.description.lowerCase() like q) or
                            (InventoryProductsTable.notes.lowerCase() like q)
                    )
                }
                .orderBy(InventoryProductsTable.name to SortOrder.ASC)
                .map { inventoryProductFromRow(it) }
        }

    fun registerInventoryProduct(request: InventoryProductRegisterRequest): Int =
        transaction {
            val id = InventoryProductsTable.insert {
                it[name] = request.name.trim()
                it[code] = request.code.trim()
                it[description] = request.description?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[categoryId] = request.categoryId
                it[unit] = request.unit.trim().ifBlank { "uds" }
                it[purchasePrice] = request.purchasePrice
                it[sellingPrice] = request.sellingPrice
                it[currentStock] = request.currentStock
                it[minStock] = request.minStock
                it[maxStock] = request.maxStock
                it[supplierId] = request.supplierId
                it[expirationDate] = request.expirationDate?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[barcode] = request.barcode?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[color] = request.color?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[icon] = request.icon?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[notes] = request.notes?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[isActive] = request.isActive
                it[createdAtEpochMs] = System.currentTimeMillis()
            } get InventoryProductsTable.id

            if (request.currentStock != 0) {
                InventoryProductMovementsTable.insert {
                    it[productId] = id
                    it[quantityChange] = request.currentStock
                    it[type] = "INITIAL"
                    it[note] = "Stock inicial"
                    it[InventoryProductMovementsTable.previousStock] = 0
                    it[InventoryProductMovementsTable.currentStock] = request.currentStock
                    it[unitCost] = request.purchasePrice
                    it[reason] = "Stock inicial del producto"
                    it[status] = "COMPLETED"
                    it[createdAtEpochMs] = System.currentTimeMillis()
                    it[updatedAtEpochMs] = System.currentTimeMillis()
                }
            }
            id
        }

    fun updateInventoryProduct(id: Int, request: InventoryProductUpdateRequest) {
        transaction {
            InventoryProductsTable.update({ InventoryProductsTable.id eq id }) {
                it[name] = request.name.trim()
                it[code] = request.code.trim()
                it[description] = request.description?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[categoryId] = request.categoryId
                it[unit] = request.unit.trim().ifBlank { "uds" }
                it[purchasePrice] = request.purchasePrice
                it[sellingPrice] = request.sellingPrice
                it[currentStock] = request.currentStock
                it[minStock] = request.minStock
                it[maxStock] = request.maxStock
                it[supplierId] = request.supplierId
                it[expirationDate] = request.expirationDate?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[barcode] = request.barcode?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[color] = request.color?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[icon] = request.icon?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[notes] = request.notes?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[isActive] = request.isActive
                it[updatedAtEpochMs] = System.currentTimeMillis()
            }
        }
    }

    fun adjustInventoryProductStock(id: Int, quantityChange: Int, type: String, note: String?, reason: String? = null, costPerUnit: Double = 0.0, referenceNumber: String? = null) {
        transaction {
            val product = InventoryProductsTable.selectAll()
                .where { InventoryProductsTable.id eq id }
                .firstOrNull() ?: throw IllegalStateException("Producto no encontrado.")
            val prevStock = product[InventoryProductsTable.currentStock]
            val newStock = (prevStock + quantityChange).coerceAtLeast(0)
            if (newStock < 0) throw IllegalStateException("Stock no puede ser negativo.")
            InventoryProductsTable.update({ InventoryProductsTable.id eq id }) {
                it[InventoryProductsTable.currentStock] = newStock
                it[updatedAtEpochMs] = System.currentTimeMillis()
            }
            val noteValue = note?.trim()?.takeIf { v -> v.isNotEmpty() }
            val reasonValue = reason?.trim()?.takeIf { v -> v.isNotEmpty() }
            val referenceValue = referenceNumber?.trim()?.takeIf { v -> v.isNotEmpty() }
            InventoryProductMovementsTable.insert {
                it[InventoryProductMovementsTable.productId] = id
                it[InventoryProductMovementsTable.quantityChange] = quantityChange
                it[InventoryProductMovementsTable.type] = type
                it[InventoryProductMovementsTable.note] = noteValue
                it[InventoryProductMovementsTable.previousStock] = prevStock
                it[InventoryProductMovementsTable.currentStock] = newStock
                it[InventoryProductMovementsTable.unitCost] = costPerUnit
                it[InventoryProductMovementsTable.reason] = reasonValue
                it[InventoryProductMovementsTable.referenceNumber] = referenceValue
                it[InventoryProductMovementsTable.status] = "COMPLETED"
                it[createdAtEpochMs] = System.currentTimeMillis()
                it[updatedAtEpochMs] = System.currentTimeMillis()
            }
        }
    }

    fun listInventoryProductMovements(
        productId: Int? = null,
        type: String? = null,
        limit: Int = 200,
        offset: Int = 0,
        startDateMs: Long? = null,
        endDateMs: Long? = null,
    ): List<InventoryProductMovementRow> =
        transaction {
            var query = InventoryProductMovementsTable.selectAll()
            if (productId != null) query = query.where { InventoryProductMovementsTable.productId eq productId }
            if (type != null) query = query.where { InventoryProductMovementsTable.type eq type }
            if (startDateMs != null) query = query.where { InventoryProductMovementsTable.createdAtEpochMs.greaterEq(startDateMs) }
            if (endDateMs != null) query = query.where { InventoryProductMovementsTable.createdAtEpochMs.lessEq(endDateMs) }
            query
                .orderBy(InventoryProductMovementsTable.createdAtEpochMs to SortOrder.DESC)
                .limit(limit).offset(offset.toLong())
                .map { row ->
                    InventoryProductMovementRow(
                        id = row[InventoryProductMovementsTable.id],
                        productId = row[InventoryProductMovementsTable.productId],
                        quantityChange = row[InventoryProductMovementsTable.quantityChange],
                        type = row[InventoryProductMovementsTable.type],
                        note = row[InventoryProductMovementsTable.note],
                        previousStock = row.getOrNull(InventoryProductMovementsTable.previousStock) ?: 0,
                        currentStock = row.getOrNull(InventoryProductMovementsTable.currentStock) ?: 0,
                        unitCost = row.getOrNull(InventoryProductMovementsTable.unitCost) ?: 0.0,
                        reason = row.getOrNull(InventoryProductMovementsTable.reason),
                        referenceNumber = row.getOrNull(InventoryProductMovementsTable.referenceNumber),
                        status = row.getOrNull(InventoryProductMovementsTable.status) ?: "COMPLETED",
                        createdAtEpochMs = row[InventoryProductMovementsTable.createdAtEpochMs],
                        updatedAtEpochMs = row.getOrNull(InventoryProductMovementsTable.updatedAtEpochMs),
                    )
                }
        }

    fun archiveInventoryProduct(id: Int) {
        transaction { InventoryProductsTable.update({ InventoryProductsTable.id eq id }) { it[isArchived] = true } }
    }

    fun restoreInventoryProduct(id: Int) {
        transaction { InventoryProductsTable.update({ InventoryProductsTable.id eq id }) { it[isArchived] = false } }
    }

    fun hardDeleteInventoryProduct(id: Int) {
        transaction { InventoryProductsTable.deleteWhere { InventoryProductsTable.id eq id } }
    }

    fun loadInventoryProductDirectory(): Pair<InventoryProductKpis, List<InventoryProduct>> {
        val products = listInventoryProducts()
        val kpis = InventoryProductKpis(
            totalProducts = products.count { it.isActive },
            totalUnits = products.sumOf { it.currentStock },
            lowStockCount = products.count { resolveInventoryProductStatus(it.currentStock, it.minStock, it.expirationDate) == InventoryProductStatus.LOW_STOCK },
            outOfStockCount = products.count { it.currentStock <= 0 },
            expiringSoonCount = products.count { p ->
                p.expirationDate?.let { exp ->
                    runCatching { java.time.LocalDate.parse(exp) }.getOrNull()
                        ?.let { it.isAfter(java.time.LocalDate.now()) && it.isBefore(java.time.LocalDate.now().plusDays(30)) }
                } == true
            },
            totalValue = products.sumOf { it.sellingPrice * it.currentStock },
        )
        return kpis to products
    }

    // ── Enhanced Inventory Movement Methods ──────────────────────────────────

    fun getInventoryMovementStats(): InventoryMovementStats {
        val todayStart = java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val todayEnd = java.time.LocalDate.now().plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        val todayMovements = listInventoryProductMovements(startDateMs = todayStart, endDateMs = todayEnd, limit = 1000)
        val products = listInventoryProducts()
        return InventoryMovementStats(
            todayEntries = todayMovements.count { it.type in listOf("PURCHASE", "INITIAL") && it.quantityChange > 0 },
            todayConsumptions = todayMovements.count { it.type in listOf("TREATMENT_CONSUMPTION", "CONSUMPTION") && it.quantityChange < 0 },
            todayAdjustments = todayMovements.count { it.type in listOf("MANUAL_ADJUSTMENT", "INVENTORY_CORRECTION", "STOCK_TRANSFER") },
            todayValue = todayMovements.filter { it.quantityChange > 0 }.sumOf { it.unitCost * kotlin.math.abs(it.quantityChange) },
            recentMovementsCount = todayMovements.size,
            lowStockAlerts = products.count { resolveInventoryProductStatus(it.currentStock, it.minStock, it.expirationDate) in listOf(InventoryProductStatus.LOW_STOCK, InventoryProductStatus.OUT_OF_STOCK) },
        )
    }

    fun getRecentMovementsWithProduct(limit: Int = 20): List<Pair<InventoryProductMovementRow, InventoryProductSummary>> =
        transaction {
            val movements = InventoryProductMovementsTable.selectAll()
                .orderBy(InventoryProductMovementsTable.createdAtEpochMs to SortOrder.DESC)
                .limit(limit)
                .map { row ->
                    InventoryProductMovementRow(
                        id = row[InventoryProductMovementsTable.id],
                        productId = row[InventoryProductMovementsTable.productId],
                        quantityChange = row[InventoryProductMovementsTable.quantityChange],
                        type = row[InventoryProductMovementsTable.type],
                        note = row[InventoryProductMovementsTable.note],
                        previousStock = row.getOrNull(InventoryProductMovementsTable.previousStock) ?: 0,
                        currentStock = row.getOrNull(InventoryProductMovementsTable.currentStock) ?: 0,
                        unitCost = row.getOrNull(InventoryProductMovementsTable.unitCost) ?: 0.0,
                        reason = row.getOrNull(InventoryProductMovementsTable.reason),
                        referenceNumber = row.getOrNull(InventoryProductMovementsTable.referenceNumber),
                        status = row.getOrNull(InventoryProductMovementsTable.status) ?: "COMPLETED",
                        createdAtEpochMs = row[InventoryProductMovementsTable.createdAtEpochMs],
                        updatedAtEpochMs = row.getOrNull(InventoryProductMovementsTable.updatedAtEpochMs),
                    )
                }
            val productIds = movements.map { it.productId }.distinct()
            val products = if (productIds.isNotEmpty()) {
                InventoryProductsTable.selectAll()
                    .where { InventoryProductsTable.id inList productIds }
                    .associate { row ->
                        row[InventoryProductsTable.id] to InventoryProductSummary(
                            id = row[InventoryProductsTable.id],
                            name = row[InventoryProductsTable.name],
                            code = row[InventoryProductsTable.code],
                            currentStock = row[InventoryProductsTable.currentStock],
                            unit = row[InventoryProductsTable.unit],
                            purchasePrice = row[InventoryProductsTable.purchasePrice],
                        )
                    }
            } else emptyMap()
            movements.mapNotNull { mv -> products[mv.productId]?.let { prod -> mv to prod } }
        }

    fun searchInventoryProductsPaginated(
        query: String,
        categoryId: Int? = null,
        lowStockOnly: Boolean = false,
        limit: Int = 50,
        offset: Int = 0,
    ): Pair<Int, List<InventoryProduct>> =
        transaction {
            var baseQuery = productBaseQuery().selectAll()
            var countQuery = InventoryProductsTable.selectAll()
            val conditions = mutableListOf<Op<Boolean>>()
            conditions.add(InventoryProductsTable.isActive eq true)
            conditions.add(InventoryProductsTable.isArchived eq false)
            if (query.isNotBlank()) {
                val q = "%${query.trim().lowercase()}%"
                conditions.add(
                    (InventoryProductsTable.name.lowerCase() like q) or
                        (InventoryProductsTable.code.lowerCase() like q) or
                        (InventoryProductsTable.description.lowerCase() like q) or
                        (InventoryProductsTable.notes.lowerCase() like q)
                )
            }
            if (categoryId != null) {
                conditions.add(InventoryProductsTable.categoryId eq categoryId)
            }
            val combined = conditions.reduce { acc, op -> acc and op }
            val total = countQuery.where(combined).count().toInt()
            val products = baseQuery.where(combined)
                .orderBy(InventoryProductsTable.name to SortOrder.ASC)
                .limit(limit).offset(offset.toLong())
                .map { inventoryProductFromRow(it) }
            val filtered = if (lowStockOnly) {
                products.filter { resolveInventoryProductStatus(it.currentStock, it.minStock, it.expirationDate) in listOf(InventoryProductStatus.LOW_STOCK, InventoryProductStatus.OUT_OF_STOCK) }
            } else products
            total to filtered
        }

    fun getLowStockProducts(): List<InventoryProduct> =
        listInventoryProducts().filter {
            resolveInventoryProductStatus(it.currentStock, it.minStock, it.expirationDate) in listOf(InventoryProductStatus.LOW_STOCK, InventoryProductStatus.OUT_OF_STOCK)
        }

    fun getExpiringProducts(withinDays: Int = 30): List<InventoryProduct> {
        val cutoff = java.time.LocalDate.now().plusDays(withinDays.toLong())
        return listInventoryProducts().filter { p ->
            p.expirationDate?.let { exp ->
                runCatching { java.time.LocalDate.parse(exp) }.getOrNull()
                    ?.let { it.isAfter(java.time.LocalDate.now()) && !it.isAfter(cutoff) }
            } == true
        }
    }

    fun bulkAdjustStock(adjustments: List<Triple<Int, Int, String>>, reason: String) {
        transaction {
            for ((productId, quantityChange, type) in adjustments) {
                val product = InventoryProductsTable.selectAll()
                    .where { InventoryProductsTable.id eq productId }
                    .firstOrNull() ?: throw IllegalStateException("Producto ID $productId no encontrado.")
                val previousStock = product[InventoryProductsTable.currentStock]
                val newStock = (previousStock + quantityChange).coerceAtLeast(0)
                if (newStock < 0) throw IllegalStateException("Stock insuficiente para producto ID $productId.")
                InventoryProductsTable.update({ InventoryProductsTable.id eq productId }) {
                    it[currentStock] = newStock
                    it[updatedAtEpochMs] = System.currentTimeMillis()
                }
                InventoryProductMovementsTable.insert {
                    it[InventoryProductMovementsTable.productId] = productId
                    it[InventoryProductMovementsTable.quantityChange] = quantityChange
                    it[InventoryProductMovementsTable.type] = type
                    it[InventoryProductMovementsTable.previousStock] = previousStock
                    it[InventoryProductMovementsTable.currentStock] = newStock
                    it[InventoryProductMovementsTable.reason] = reason.trim().ifBlank { null }
                    it[InventoryProductMovementsTable.status] = "COMPLETED"
                    it[createdAtEpochMs] = System.currentTimeMillis()
                    it[updatedAtEpochMs] = System.currentTimeMillis()
                }
            }
        }
    }

    // ── Patient Clinical Workspace ─────────────────────────────────────────

    private fun doctorNameById(doctorId: Int?): String? {
        if (doctorId == null) return null
        return DoctorsTable
            .selectAll()
            .where { DoctorsTable.id eq doctorId }
            .firstOrNull()
            ?.let { "Dr. ${it[DoctorsTable.firstName].trim()} ${it[DoctorsTable.lastName].trim()}".trim() }
    }

    private fun procedureNameById(procedureTypeId: Int?): String? {
        if (procedureTypeId == null) return null
        return ProcedureTypesTable
            .selectAll()
            .where { ProcedureTypesTable.id eq procedureTypeId }
            .firstOrNull()
            ?.get(ProcedureTypesTable.name)
    }

    fun loadPatientClinicalProfile(patientId: Int): PatientClinicalProfile =
        transaction {
            val patient =
                PatientsTable
                    .selectAll()
                    .where { PatientsTable.id eq patientId }
                    .firstOrNull()
                    ?: throw IllegalArgumentException("No se encontró el paciente.")
            PatientClinicalProfile(
                patient = patientFromRow(patient),
                medicalRecords = listMedicalHistoryForPatientInternal(patientId),
                dentalRecords = listDentalHistoryForPatientInternal(patientId),
                documents = listDocumentsForPatientInternal(patientId),
                notes = listPatientNotesInternal(patientId),
                prescriptions = listPrescriptionsForPatientInternal(patientId),
                followUps = listFollowUpsForPatientInternal(patientId),
            )
        }

    // ── Medical history ────────────────────────────────────────────────────

    fun listMedicalHistoryForPatient(patientId: Int): List<PatientMedicalRecord> =
        transaction { listMedicalHistoryForPatientInternal(patientId) }

    private fun listMedicalHistoryForPatientInternal(patientId: Int): List<PatientMedicalRecord> =
        PatientMedicalHistoryTable
            .selectAll()
            .where { PatientMedicalHistoryTable.patientId eq patientId }
            .orderBy(PatientMedicalHistoryTable.recordedAt to SortOrder.DESC)
            .map {
                PatientMedicalRecord(
                    id = it[PatientMedicalHistoryTable.id],
                    patientId = patientId,
                    recordType = MedicalRecordType.fromDb(it[PatientMedicalHistoryTable.recordType]),
                    description = it[PatientMedicalHistoryTable.description],
                    recordedAt = it[PatientMedicalHistoryTable.recordedAt],
                    doctorId = it[PatientMedicalHistoryTable.doctorId],
                    doctorName = doctorNameById(it[PatientMedicalHistoryTable.doctorId]),
                    isActive = it[PatientMedicalHistoryTable.isActive],
                    notes = it[PatientMedicalHistoryTable.notes],
                    createdAtEpochMs = it[PatientMedicalHistoryTable.createdAtEpochMs],
                )
            }

    fun registerMedicalRecord(patientId: Int, request: MedicalRecordRegisterRequest) {
        transaction {
            require(request.description.isNotBlank()) { "La descripción es obligatoria." }
            require(
                PatientsTable.selectAll().where { PatientsTable.id eq patientId }.count() > 0,
            ) { "No se encontró el paciente." }
            PatientMedicalHistoryTable.insert {
                it[PatientMedicalHistoryTable.patientId] = patientId
                it[recordType] = request.recordType.name
                it[description] = request.description.trim()
                it[recordedAt] = request.recordedAt
                it[doctorId] = request.doctorId
                it[isActive] = request.isActive
                it[notes] = request.notes?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[createdAtEpochMs] = System.currentTimeMillis()
            }
        }
    }

    fun updateMedicalRecord(recordId: Int, request: MedicalRecordUpdateRequest) {
        transaction {
            require(request.description.isNotBlank()) { "La descripción es obligatoria." }
            val updated =
                PatientMedicalHistoryTable.update({ PatientMedicalHistoryTable.id eq recordId }) {
                    it[recordType] = request.recordType.name
                    it[description] = request.description.trim()
                    it[recordedAt] = request.recordedAt
                    it[doctorId] = request.doctorId
                    it[isActive] = request.isActive
                    it[notes] = request.notes?.trim()?.takeIf { v -> v.isNotEmpty() }
                }
            require(updated > 0) { "No se encontró el registro médico." }
        }
    }

    fun deleteMedicalRecord(recordId: Int) {
        transaction {
            val deleted = PatientMedicalHistoryTable.deleteWhere { PatientMedicalHistoryTable.id eq recordId }
            require(deleted > 0) { "No se encontró el registro médico." }
        }
    }

    // ── Dental history ─────────────────────────────────────────────────────

    fun listDentalHistoryForPatient(patientId: Int): List<PatientDentalRecord> =
        transaction { listDentalHistoryForPatientInternal(patientId) }

    private fun listDentalHistoryForPatientInternal(patientId: Int): List<PatientDentalRecord> =
        PatientDentalHistoryTable
            .selectAll()
            .where { PatientDentalHistoryTable.patientId eq patientId }
            .orderBy(PatientDentalHistoryTable.recordedAt to SortOrder.DESC)
            .map {
                PatientDentalRecord(
                    id = it[PatientDentalHistoryTable.id],
                    patientId = patientId,
                    toothNumber = it[PatientDentalHistoryTable.toothNumber],
                    toothQuadrant = it[PatientDentalHistoryTable.toothQuadrant],
                    diagnosis = it[PatientDentalHistoryTable.diagnosis],
                    treatmentPerformed = it[PatientDentalHistoryTable.treatmentPerformed],
                    procedureTypeId = it[PatientDentalHistoryTable.procedureTypeId],
                    procedureTypeName = procedureNameById(it[PatientDentalHistoryTable.procedureTypeId]),
                    recordedAt = it[PatientDentalHistoryTable.recordedAt],
                    doctorId = it[PatientDentalHistoryTable.doctorId],
                    doctorName = doctorNameById(it[PatientDentalHistoryTable.doctorId]),
                    notes = it[PatientDentalHistoryTable.notes],
                    createdAtEpochMs = it[PatientDentalHistoryTable.createdAtEpochMs],
                )
            }

    fun registerDentalRecord(patientId: Int, request: DentalRecordRegisterRequest) {
        transaction {
            require(request.diagnosis.isNotBlank()) { "El diagnóstico es obligatorio." }
            require(
                PatientsTable.selectAll().where { PatientsTable.id eq patientId }.count() > 0,
            ) { "No se encontró el paciente." }
            PatientDentalHistoryTable.insert {
                it[PatientDentalHistoryTable.patientId] = patientId
                it[toothNumber] = request.toothNumber?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[toothQuadrant] = request.toothQuadrant?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[diagnosis] = request.diagnosis.trim()
                it[treatmentPerformed] = request.treatmentPerformed?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[procedureTypeId] = request.procedureTypeId
                it[recordedAt] = request.recordedAt
                it[doctorId] = request.doctorId
                it[notes] = request.notes?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[createdAtEpochMs] = System.currentTimeMillis()
            }
        }
    }

    fun updateDentalRecord(recordId: Int, request: DentalRecordUpdateRequest) {
        transaction {
            require(request.diagnosis.isNotBlank()) { "El diagnóstico es obligatorio." }
            val updated =
                PatientDentalHistoryTable.update({ PatientDentalHistoryTable.id eq recordId }) {
                    it[toothNumber] = request.toothNumber?.trim()?.takeIf { v -> v.isNotEmpty() }
                    it[toothQuadrant] = request.toothQuadrant?.trim()?.takeIf { v -> v.isNotEmpty() }
                    it[diagnosis] = request.diagnosis.trim()
                    it[treatmentPerformed] = request.treatmentPerformed?.trim()?.takeIf { v -> v.isNotEmpty() }
                    it[procedureTypeId] = request.procedureTypeId
                    it[recordedAt] = request.recordedAt
                    it[doctorId] = request.doctorId
                    it[notes] = request.notes?.trim()?.takeIf { v -> v.isNotEmpty() }
                }
            require(updated > 0) { "No se encontró el registro dental." }
        }
    }

    fun deleteDentalRecord(recordId: Int) {
        transaction {
            val deleted = PatientDentalHistoryTable.deleteWhere { PatientDentalHistoryTable.id eq recordId }
            require(deleted > 0) { "No se encontró el registro dental." }
        }
    }

    // ── Documents ──────────────────────────────────────────────────────────

    fun listDocumentsForPatient(patientId: Int): List<PatientDocument> =
        transaction { listDocumentsForPatientInternal(patientId) }

    private fun listDocumentsForPatientInternal(patientId: Int): List<PatientDocument> =
        PatientDocumentsTable
            .selectAll()
            .where { PatientDocumentsTable.patientId eq patientId }
            .orderBy(PatientDocumentsTable.uploadedAtEpochMs to SortOrder.DESC)
            .map {
                PatientDocument(
                    id = it[PatientDocumentsTable.id],
                    patientId = patientId,
                    title = it[PatientDocumentsTable.title],
                    category = DocumentCategory.fromDb(it[PatientDocumentsTable.category]),
                    fileName = it[PatientDocumentsTable.fileName],
                    filePath = it[PatientDocumentsTable.filePath],
                    mimeType = it[PatientDocumentsTable.mimeType],
                    fileSize = it[PatientDocumentsTable.fileSize],
                    notes = it[PatientDocumentsTable.notes],
                    uploadedAtEpochMs = it[PatientDocumentsTable.uploadedAtEpochMs],
                )
            }

    fun registerPatientDocument(patientId: Int, request: PatientDocumentRegisterRequest) {
        transaction {
            require(request.title.isNotBlank()) { "El título es obligatorio." }
            require(request.fileName.isNotBlank()) { "Debe seleccionar un archivo." }
            require(
                PatientsTable.selectAll().where { PatientsTable.id eq patientId }.count() > 0,
            ) { "No se encontró el paciente." }
            PatientDocumentsTable.insert {
                it[PatientDocumentsTable.patientId] = patientId
                it[title] = request.title.trim()
                it[category] = request.category.name
                it[fileName] = request.fileName.trim()
                it[filePath] = request.filePath
                it[mimeType] = request.mimeType
                it[fileSize] = request.fileSize
                it[notes] = request.notes?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[uploadedAtEpochMs] = System.currentTimeMillis()
            }
        }
    }

    fun deletePatientDocument(documentId: Int) {
        transaction {
            val deleted = PatientDocumentsTable.deleteWhere { PatientDocumentsTable.id eq documentId }
            require(deleted > 0) { "No se encontró el documento." }
        }
    }

    // ── Notes ──────────────────────────────────────────────────────────────

    fun listPatientNotes(patientId: Int): List<PatientNote> =
        transaction { listPatientNotesInternal(patientId) }

    private fun listPatientNotesInternal(patientId: Int): List<PatientNote> =
        PatientNotesTable
            .selectAll()
            .where { PatientNotesTable.patientId eq patientId }
            .orderBy(PatientNotesTable.createdAtEpochMs to SortOrder.DESC)
            .map {
                PatientNote(
                    id = it[PatientNotesTable.id],
                    patientId = patientId,
                    body = it[PatientNotesTable.body],
                    authorLabel = it[PatientNotesTable.authorLabel],
                    isPinned = it[PatientNotesTable.isPinned],
                    createdAtEpochMs = it[PatientNotesTable.createdAtEpochMs],
                )
            }

    fun addPatientNote(patientId: Int, request: PatientNoteRegisterRequest) {
        transaction {
            require(request.body.isNotBlank()) { "El contenido de la nota es obligatorio." }
            PatientNotesTable.insert {
                it[PatientNotesTable.patientId] = patientId
                it[body] = request.body.trim()
                it[authorLabel] = request.authorLabel.trim().ifBlank { "Recepción" }
                it[isPinned] = request.isPinned
                it[createdAtEpochMs] = System.currentTimeMillis()
            }
        }
    }

    fun togglePinPatientNote(noteId: Int) {
        transaction {
            val row =
                PatientNotesTable
                    .selectAll()
                    .where { PatientNotesTable.id eq noteId }
                    .firstOrNull()
                    ?: throw IllegalArgumentException("No se encontró la nota.")
            PatientNotesTable.update({ PatientNotesTable.id eq noteId }) {
                it[isPinned] = !row[PatientNotesTable.isPinned]
            }
        }
    }

    fun deletePatientNote(noteId: Int) {
        transaction {
            val deleted = PatientNotesTable.deleteWhere { PatientNotesTable.id eq noteId }
            require(deleted > 0) { "No se encontró la nota." }
        }
    }

    // ── Prescriptions ──────────────────────────────────────────────────────

    fun listPrescriptionsForPatient(patientId: Int): List<Prescription> =
        transaction { listPrescriptionsForPatientInternal(patientId) }

    private fun listPrescriptionsForPatientInternal(patientId: Int): List<Prescription> =
        PrescriptionsTable
            .selectAll()
            .where { PrescriptionsTable.patientId eq patientId }
            .orderBy(PrescriptionsTable.prescribedAt to SortOrder.DESC)
            .map {
                Prescription(
                    id = it[PrescriptionsTable.id],
                    patientId = patientId,
                    medicine = it[PrescriptionsTable.medicine],
                    dosage = it[PrescriptionsTable.dosage],
                    frequency = it[PrescriptionsTable.frequency],
                    instructions = it[PrescriptionsTable.instructions],
                    prescribedAt = it[PrescriptionsTable.prescribedAt],
                    doctorId = it[PrescriptionsTable.doctorId],
                    doctorName = doctorNameById(it[PrescriptionsTable.doctorId]),
                    status = PrescriptionStatus.fromDb(it[PrescriptionsTable.status]),
                    createdAtEpochMs = it[PrescriptionsTable.createdAtEpochMs],
                )
            }

    fun registerPrescription(patientId: Int, request: PrescriptionRegisterRequest) {
        transaction {
            require(request.medicine.isNotBlank()) { "El medicamento es obligatorio." }
            require(request.dosage.isNotBlank()) { "La dosis es obligatoria." }
            require(request.frequency.isNotBlank()) { "La frecuencia es obligatoria." }
            require(
                PatientsTable.selectAll().where { PatientsTable.id eq patientId }.count() > 0,
            ) { "No se encontró el paciente." }
            PrescriptionsTable.insert {
                it[PrescriptionsTable.patientId] = patientId
                it[medicine] = request.medicine.trim()
                it[dosage] = request.dosage.trim()
                it[frequency] = request.frequency.trim()
                it[instructions] = request.instructions?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[prescribedAt] = request.prescribedAt
                it[doctorId] = request.doctorId
                it[status] = request.status.name
                it[createdAtEpochMs] = System.currentTimeMillis()
            }
        }
    }

    fun updatePrescriptionStatus(prescriptionId: Int, status: PrescriptionStatus) {
        transaction {
            val updated =
                PrescriptionsTable.update({ PrescriptionsTable.id eq prescriptionId }) {
                    it[PrescriptionsTable.status] = status.name
                }
            require(updated > 0) { "No se encontró la receta." }
        }
    }

    fun deletePrescription(prescriptionId: Int) {
        transaction {
            val deleted = PrescriptionsTable.deleteWhere { PrescriptionsTable.id eq prescriptionId }
            require(deleted > 0) { "No se encontró la receta." }
        }
    }

    // ── Follow-ups ─────────────────────────────────────────────────────────

    fun listFollowUpsForPatient(patientId: Int): List<FollowUp> =
        transaction { listFollowUpsForPatientInternal(patientId) }

    private fun listFollowUpsForPatientInternal(patientId: Int): List<FollowUp> =
        FollowUpsTable
            .selectAll()
            .where { FollowUpsTable.patientId eq patientId }
            .orderBy(FollowUpsTable.dueDate to SortOrder.ASC)
            .map {
                val apptId = it[FollowUpsTable.appointmentId]
                val apptDateLabel =
                    apptId?.let { id ->
                        AppointmentsTable
                            .selectAll()
                            .where { AppointmentsTable.id eq id }
                            .firstOrNull()
                            ?.let { a -> a[AppointmentsTable.scheduledAt] }
                    }
                FollowUp(
                    id = it[FollowUpsTable.id],
                    patientId = patientId,
                    dueDate = it[FollowUpsTable.dueDate],
                    notes = it[FollowUpsTable.notes],
                    status = FollowUpStatus.fromDb(it[FollowUpsTable.status]),
                    appointmentId = apptId,
                    appointmentDateLabel = apptDateLabel,
                    createdAtEpochMs = it[FollowUpsTable.createdAtEpochMs],
                )
            }

    fun registerFollowUp(patientId: Int, request: FollowUpRegisterRequest) {
        transaction {
            require(request.dueDate.isNotBlank()) { "La fecha del seguimiento es obligatoria." }
            require(
                PatientsTable.selectAll().where { PatientsTable.id eq patientId }.count() > 0,
            ) { "No se encontró el paciente." }
            FollowUpsTable.insert {
                it[FollowUpsTable.patientId] = patientId
                it[dueDate] = request.dueDate
                it[notes] = request.notes?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[status] = request.status.name
                it[appointmentId] = request.appointmentId
                it[createdAtEpochMs] = System.currentTimeMillis()
            }
        }
    }

    fun updateFollowUpStatus(followUpId: Int, status: FollowUpStatus) {
        transaction {
            val updated =
                FollowUpsTable.update({ FollowUpsTable.id eq followUpId }) {
                    it[FollowUpsTable.status] = status.name
                }
            require(updated > 0) { "No se encontró el seguimiento." }
        }
    }

    fun deleteFollowUp(followUpId: Int) {
        transaction {
            val deleted = FollowUpsTable.deleteWhere { FollowUpsTable.id eq followUpId }
            require(deleted > 0) { "No se encontró el seguimiento." }
        }
    }

    // ── Treatment Plans ────────────────────────────────────────────────────

    fun listTreatmentPlansForPatient(patientId: Int): List<TreatmentPlan> =
        transaction {
            val patientName =
                PatientsTable
                    .select(PatientsTable.firstName, PatientsTable.lastName)
                    .where { PatientsTable.id eq patientId }
                    .firstOrNull()
                    ?.let { "${it[PatientsTable.firstName].trim()} ${it[PatientsTable.lastName].trim()}".trim() }
                    ?: "Paciente #$patientId"
            val phasesByPlan =
                (TreatmentPlanPhasesTable innerJoin TreatmentPlansTable)
                    .selectAll()
                    .where { TreatmentPlansTable.patientId eq patientId }
                    .orderBy(
                        TreatmentPlanPhasesTable.sortOrder to SortOrder.ASC,
                        TreatmentPlanPhasesTable.id to SortOrder.ASC,
                    )
                    .groupBy { it[TreatmentPlanPhasesTable.planId] }
            TreatmentPlansTable
                .selectAll()
                .where { TreatmentPlansTable.patientId eq patientId }
                .orderBy(TreatmentPlansTable.createdAtEpochMs to SortOrder.DESC)
                .map { row ->
                    row.toTreatmentPlan(
                        patientName = patientName,
                        phaseRows = phasesByPlan[row[TreatmentPlansTable.id]].orEmpty(),
                    )
                }
        }

    private fun ResultRow.toTreatmentPlan(
        patientName: String,
        phaseRows: List<ResultRow>,
    ): TreatmentPlan =
        TreatmentPlan(
            id = this[TreatmentPlansTable.id],
            patientId = this[TreatmentPlansTable.patientId],
            patientName = patientName,
            title = this[TreatmentPlansTable.title],
            description = this[TreatmentPlansTable.description],
            status = TreatmentPlanStatus.fromDb(this[TreatmentPlansTable.status]),
            estimatedCost = this[TreatmentPlansTable.estimatedCost],
            createdAtEpochMs = this[TreatmentPlansTable.createdAtEpochMs],
            phases = phaseRows.map { it.toTreatmentPlanPhase() },
        )

    private fun ResultRow.toTreatmentPlanPhase(): TreatmentPlanPhase =
        TreatmentPlanPhase(
            id = this[TreatmentPlanPhasesTable.id],
            planId = this[TreatmentPlanPhasesTable.planId],
            name = this[TreatmentPlanPhasesTable.name],
            description = this[TreatmentPlanPhasesTable.description],
            estimatedCost = this[TreatmentPlanPhasesTable.estimatedCost],
            status = TreatmentPlanPhaseStatus.fromDb(this[TreatmentPlanPhasesTable.status]),
            sortOrder = this[TreatmentPlanPhasesTable.sortOrder],
            createdAtEpochMs = this[TreatmentPlanPhasesTable.createdAtEpochMs],
        )

    fun registerTreatmentPlan(patientId: Int, request: TreatmentPlanRegisterRequest): Int =
        transaction {
            require(
                PatientsTable.selectAll().where { PatientsTable.id eq patientId }.count() > 0,
            ) { "No se encontró el paciente." }
            TreatmentPlansTable.insert {
                it[TreatmentPlansTable.patientId] = patientId
                it[title] = request.title.trim()
                it[description] = request.description?.trim()?.takeIf { v -> v.isNotEmpty() }
                it[status] = request.status.name
                it[estimatedCost] = 0.0
                it[createdAtEpochMs] = System.currentTimeMillis()
            } get TreatmentPlansTable.id
        }

    fun updateTreatmentPlan(planId: Int, request: TreatmentPlanUpdateRequest) {
        transaction {
            require(request.title.isNotBlank()) { "El título del plan es obligatorio." }
            val updated =
                TreatmentPlansTable.update({ TreatmentPlansTable.id eq planId }) {
                    it[title] = request.title.trim()
                    it[description] = request.description?.trim()?.takeIf { v -> v.isNotEmpty() }
                    it[status] = request.status.name
                }
            require(updated > 0) { "No se encontró el plan de tratamiento." }
        }
    }

    fun updateTreatmentPlanStatus(planId: Int, status: TreatmentPlanStatus) {
        transaction {
            val updated =
                TreatmentPlansTable.update({ TreatmentPlansTable.id eq planId }) {
                    it[TreatmentPlansTable.status] = status.name
                }
            require(updated > 0) { "No se encontró el plan de tratamiento." }
        }
    }

    fun deleteTreatmentPlan(planId: Int) {
        transaction {
            val deleted = TreatmentPlansTable.deleteWhere { TreatmentPlansTable.id eq planId }
            require(deleted > 0) { "No se encontró el plan de tratamiento." }
        }
    }

    fun addTreatmentPlanPhase(planId: Int, request: TreatmentPlanPhaseRegisterRequest): Int =
        transaction {
            val nextOrder =
                TreatmentPlanPhasesTable
                    .selectAll()
                    .where { TreatmentPlanPhasesTable.planId eq planId }
                    .maxOfOrNull { it[TreatmentPlanPhasesTable.sortOrder] }
                    ?.plus(1)
                    ?: 0
            val phaseId =
                TreatmentPlanPhasesTable.insert {
                    it[TreatmentPlanPhasesTable.planId] = planId
                    it[name] = request.name.trim()
                    it[description] = request.description?.trim()?.takeIf { v -> v.isNotEmpty() }
                    it[estimatedCost] = request.estimatedCost.coerceAtLeast(0.0)
                    it[status] = request.status.name
                    it[sortOrder] = nextOrder
                    it[createdAtEpochMs] = System.currentTimeMillis()
                } get TreatmentPlanPhasesTable.id
            recomputeTreatmentPlanCost(planId)
            phaseId
        }

    fun updateTreatmentPlanPhase(phaseId: Int, request: TreatmentPlanPhaseUpdateRequest) {
        transaction {
            require(request.name.isNotBlank()) { "El nombre de la fase es obligatorio." }
            val planId =
                TreatmentPlanPhasesTable
                    .select(TreatmentPlanPhasesTable.planId)
                    .where { TreatmentPlanPhasesTable.id eq phaseId }
                    .firstOrNull()
                    ?.get(TreatmentPlanPhasesTable.planId)
                    ?: throw IllegalArgumentException("No se encontró la fase.")
            val updated =
                TreatmentPlanPhasesTable.update({ TreatmentPlanPhasesTable.id eq phaseId }) {
                    it[name] = request.name.trim()
                    it[description] = request.description?.trim()?.takeIf { v -> v.isNotEmpty() }
                    it[estimatedCost] = request.estimatedCost.coerceAtLeast(0.0)
                    it[status] = request.status.name
                    it[sortOrder] = request.sortOrder
                }
            require(updated > 0) { "No se encontró la fase." }
            recomputeTreatmentPlanCost(planId)
        }
    }

    fun updateTreatmentPlanPhaseStatus(phaseId: Int, status: TreatmentPlanPhaseStatus) {
        transaction {
            val planId =
                TreatmentPlanPhasesTable
                    .select(TreatmentPlanPhasesTable.planId)
                    .where { TreatmentPlanPhasesTable.id eq phaseId }
                    .firstOrNull()
                    ?.get(TreatmentPlanPhasesTable.planId)
                    ?: throw IllegalArgumentException("No se encontró la fase.")
            val updated =
                TreatmentPlanPhasesTable.update({ TreatmentPlanPhasesTable.id eq phaseId }) {
                    it[TreatmentPlanPhasesTable.status] = status.name
                }
            require(updated > 0) { "No se encontró la fase." }
            if (status == TreatmentPlanPhaseStatus.COMPLETED) {
                val allCompleted =
                    TreatmentPlanPhasesTable
                        .selectAll()
                        .where { TreatmentPlanPhasesTable.planId eq planId }
                        .all { it[TreatmentPlanPhasesTable.status] == TreatmentPlanPhaseStatus.COMPLETED.name }
                if (allCompleted) {
                    TreatmentPlansTable.update({ TreatmentPlansTable.id eq planId }) {
                        it[TreatmentPlansTable.status] = TreatmentPlanStatus.COMPLETED.name
                    }
                }
            }
        }
    }

    fun deleteTreatmentPlanPhase(phaseId: Int) {
        transaction {
            val planId =
                TreatmentPlanPhasesTable
                    .select(TreatmentPlanPhasesTable.planId)
                    .where { TreatmentPlanPhasesTable.id eq phaseId }
                    .firstOrNull()
                    ?.get(TreatmentPlanPhasesTable.planId)
                    ?: throw IllegalArgumentException("No se encontró la fase.")
            val deleted = TreatmentPlanPhasesTable.deleteWhere { TreatmentPlanPhasesTable.id eq phaseId }
            require(deleted > 0) { "No se encontró la fase." }
            recomputeTreatmentPlanCost(planId)
        }
    }

    /** Mantiene `treatment_plans.estimated_cost` como la suma del costo de sus fases. */
    private fun recomputeTreatmentPlanCost(planId: Int) {
        val sum =
            TreatmentPlanPhasesTable
                .select(TreatmentPlanPhasesTable.estimatedCost)
                .where { TreatmentPlanPhasesTable.planId eq planId }
                .sumOf { it[TreatmentPlanPhasesTable.estimatedCost] }
        TreatmentPlansTable.update({ TreatmentPlansTable.id eq planId }) {
            it[TreatmentPlansTable.estimatedCost] = sum
        }
    }
}
