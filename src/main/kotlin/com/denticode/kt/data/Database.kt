package com.denticode.kt.data

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Files
import kotlin.io.path.Path

object DentiDatabase {
    fun connectAndMigrate() {
        val dir = Path(System.getProperty("user.home"), ".denti-code-kt")
        Files.createDirectories(dir)
        val dbFile = dir.resolve("denti-clinic.db").toAbsolutePath().toString()
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
        }
        seedDentiReferenceDataIfEmpty()
        seedDemoClinicDataIfNeeded()
    }
}
