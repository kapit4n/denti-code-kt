package com.denticode.kt.data

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

/** SQLite schema aligned with denti-code-desktop `V1__init_schema.sql`. */
object UsersTable : Table("users") {
    val id = integer("user_id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 512)
    val displayName = varchar("display_name", 255).nullable()
    val preferredLocale = varchar("preferred_locale", 32).nullable()
    val avatarUrl = text("avatar_url").nullable()
    val isActive = bool("is_active").default(true)
    val createdAtEpochMs = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object UserRolesTable : Table("user_roles") {
    val userId = integer("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val role = varchar("role", 64)
    override val primaryKey = PrimaryKey(userId, role)
}

object DoctorsTable : Table("doctors") {
    val id = integer("doctor_id").autoIncrement()
    val userId = integer("user_id").references(UsersTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val firstName = varchar("first_name", 128)
    val lastName = varchar("last_name", 128)
    val email = varchar("email", 255)
    val contactPhone = varchar("contact_phone", 64).nullable()
    val licenseNumber = varchar("license_number", 128).nullable()
    val officeRoom = varchar("office_room", 64).nullable()
    val specialization = varchar("specialization", 255).nullable()
    val avatarUrl = text("avatar_url").nullable()
    val isActive = bool("is_active").default(true)
    override val primaryKey = PrimaryKey(id)
}

object PatientsTable : Table("patients") {
    val id = integer("patient_id").autoIncrement()
    val userId = integer("user_id").references(UsersTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val firstName = varchar("first_name", 128)
    val lastName = varchar("last_name", 128)
    val dateOfBirth = varchar("date_of_birth", 32)
    val gender = varchar("gender", 32).nullable()
    val address = text("address").nullable()
    val contactPhone = varchar("contact_phone", 64)
    val email = varchar("email", 255).nullable()
    val avatarUrl = text("avatar_url").nullable()
    val medicalHistorySummary = text("medical_history_summary").nullable()
    val createdAtEpochMs = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object ProcedureTypesTable : Table("procedure_types") {
    val id = integer("procedure_type_id").autoIncrement()
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val defaultDurationMinutes = integer("default_duration_minutes").nullable()
    val standardPrice = double("standard_price").nullable()
    val requiresToothSpecification = bool("requires_tooth_specification").default(false)
    val category = varchar("category", 128).nullable()
    val isActive = bool("is_active").default(true)
    override val primaryKey = PrimaryKey(id)
}

object TreatmentFacilitiesTable : Table("treatment_facilities") {
    val id = integer("facility_id").autoIncrement()
    val facilityCode = varchar("facility_code", 64).uniqueIndex()
    val categoryKey = varchar("category_key", 64)
    val displayName = varchar("display_name", 255)
    val sortOrder = integer("sort_order").default(0)
    val isActive = bool("is_active").default(true)
    override val primaryKey = PrimaryKey(id)
}

object ConsultoriesTable : Table("consultories") {
    val id = integer("consultory_id").autoIncrement()
    val name = varchar("name", 255)
    val shortCode = varchar("short_code", 32).nullable()
    val sortOrder = integer("sort_order").default(0)
    val isActive = bool("is_active").default(true)
    override val primaryKey = PrimaryKey(id)
}

object AppointmentsTable : Table("appointments") {
    val id = integer("appointment_id").autoIncrement()
    val patientId =
        integer("patient_id").references(PatientsTable.id, onDelete = ReferenceOption.RESTRICT)
    val primaryDoctorId =
        integer("primary_doctor_id").references(DoctorsTable.id, onDelete = ReferenceOption.RESTRICT)
    val scheduledAt = varchar("scheduled_at", 64)
    val estimatedDurationMinutes = integer("estimated_duration_minutes").nullable()
    val purpose = text("purpose").nullable()
    val notes = text("notes").nullable()
    val status = varchar("status", 32)
    override val primaryKey = PrimaryKey(id)
}

object PerformedActionsTable : Table("performed_actions") {
    val id = integer("performed_action_id").autoIncrement()
    val appointmentId =
        integer("appointment_id").references(AppointmentsTable.id, onDelete = ReferenceOption.CASCADE)
    val procedureTypeId =
        integer("procedure_type_id").references(ProcedureTypesTable.id, onDelete = ReferenceOption.RESTRICT)
    val performingDoctorId =
        integer("performing_doctor_id").references(DoctorsTable.id, onDelete = ReferenceOption.RESTRICT)
    val actionAt = varchar("action_at", 64)
    val toothInvolved = varchar("tooth_involved", 64).nullable()
    val surfacesInvolved = varchar("surfaces_involved", 128).nullable()
    val anesthesiaUsed = varchar("anesthesia_used", 128).nullable()
    val facilitiesUsed = text("facilities_used").nullable()
    val descriptionNotes = text("description_notes").nullable()
    val quantity = integer("quantity").default(1)
    val unitPrice = double("unit_price").default(0.0)
    val totalPrice = double("total_price").default(0.0)
    override val primaryKey = PrimaryKey(id)
}

object MaterialInventoryLinesTable : Table("material_inventory_lines") {
    val id = integer("line_id").autoIncrement()
    val consultoryId =
        integer("consultory_id").references(ConsultoriesTable.id, onDelete = ReferenceOption.CASCADE)
    val facilityId =
        integer("facility_id").references(TreatmentFacilitiesTable.id, onDelete = ReferenceOption.CASCADE)
    val quantity = integer("quantity").default(0)
    override val primaryKey = PrimaryKey(id)
}

object InventoryMovementsTable : Table("inventory_movements") {
    val id = integer("movement_id").autoIncrement()
    val consultoryId =
        integer("consultory_id").references(ConsultoriesTable.id, onDelete = ReferenceOption.CASCADE)
    val facilityId =
        integer("facility_id").references(TreatmentFacilitiesTable.id, onDelete = ReferenceOption.CASCADE)
    val quantityChange = integer("quantity_change")
    val type = varchar("type", 32)
    val note = text("note").nullable()
    val createdAtEpochMs = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object PaymentsTable : Table("payments") {
    val id = integer("payment_id").autoIncrement()
    val patientId =
        integer("patient_id").references(PatientsTable.id, onDelete = ReferenceOption.CASCADE)
    val appointmentId =
        integer("appointment_id").references(AppointmentsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val amount = double("amount")
    val method = varchar("method", 32).nullable()
    val note = text("note").nullable()
    val paidAt = varchar("paid_at", 64)
    override val primaryKey = PrimaryKey(id)
}
