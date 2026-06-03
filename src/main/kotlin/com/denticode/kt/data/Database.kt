package com.denticode.kt.data

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.nio.file.Files
import java.nio.file.Path

object DentiDatabase {
    /** SQLite file used by the desktop app (`~/.denti-code-kt/denti-clinic.db`). */
    fun localDatabasePath(): Path =
        Path.of(System.getProperty("user.home"), ".denti-code-kt", "denti-clinic.db")

    /** Deletes the local DB file if it exists. Call only while the app is not using the DB (e.g. before `connectAndMigrate`). */
    fun deleteLocalDatabaseFileSilently() {
        try {
            Files.deleteIfExists(localDatabasePath())
        } catch (e: Exception) {
            System.err.println("[DentiDatabase] No se pudo borrar la base local: ${e.message}")
        }
    }

    /**
     * @param resetLocalDatabase if true, removes the SQLite file first so schema + seeds run on a clean file
     *     (demo data is inserted again by [seedDemoClinicDataIfNeeded]).
     */
    fun connectAndMigrate(resetLocalDatabase: Boolean = false) {
        if (resetLocalDatabase) {
            deleteLocalDatabaseFileSilently()
        }
        val dir = localDatabasePath().parent
        Files.createDirectories(dir)
        val dbFile = localDatabasePath().toAbsolutePath().toString()
        Database.connect(
            url = "jdbc:sqlite:$dbFile?foreign_keys=ON",
            driver = "org.sqlite.JDBC",
        )
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                UsersTable,
                UserRolesTable,
                DoctorsTable,
                PatientsTable,
                ProcedureTypesTable,
                TreatmentFacilitiesTable,
                ConsultoriesTable,
                AppointmentsTable,
                PerformedActionsTable,
                MaterialInventoryLinesTable,
                InventoryMovementsTable,
                PaymentsTable,
            )
            backfillPerformedActionPatientIds()
        }
        seedDentiReferenceDataIfEmpty()
        seedDemoClinicDataIfNeeded()
    }

    /** Fills patient_id on legacy performed_actions rows (added after initial schema). */
    private fun backfillPerformedActionPatientIds() {
        PerformedActionsTable
            .selectAll()
            .where { PerformedActionsTable.patientId.isNull() }
            .forEach { row ->
                val apptId = row[PerformedActionsTable.appointmentId]
                val patientId =
                    AppointmentsTable
                        .select(AppointmentsTable.patientId)
                        .where { AppointmentsTable.id eq apptId }
                        .firstOrNull()
                        ?.get(AppointmentsTable.patientId)
                        ?: return@forEach
                PerformedActionsTable.update({ PerformedActionsTable.id eq row[PerformedActionsTable.id] }) {
                    it[PerformedActionsTable.patientId] = patientId
                }
            }
    }
}
