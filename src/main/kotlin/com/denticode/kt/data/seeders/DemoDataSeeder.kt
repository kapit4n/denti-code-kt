package com.denticode.kt.data.seeders

import com.denticode.kt.data.AppointmentAuditLogTable
import com.denticode.kt.data.AppointmentNotesTable
import com.denticode.kt.data.AppointmentsTable
import com.denticode.kt.data.ConsultoriesTable
import com.denticode.kt.data.DoctorsTable
import com.denticode.kt.data.InventoryCategoriesTable
import com.denticode.kt.data.InventoryMovementsTable
import com.denticode.kt.data.MaterialInventoryLinesTable
import com.denticode.kt.data.PatientsTable
import com.denticode.kt.data.PaymentsTable
import com.denticode.kt.data.PerformedActionsTable
import com.denticode.kt.data.ProcedureTypesTable
import com.denticode.kt.data.SuppliersTable
import com.denticode.kt.data.TreatmentCategoriesTable
import com.denticode.kt.data.TreatmentFacilitiesTable
import com.denticode.kt.data.UsersTable
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object DemoDataSeeder {

    fun seedIfEmpty(config: DemoDataConfig = DemoDataConfig.DEFAULT) {
        transaction {
            val anyEmpty =
                UsersTable.selectAll().count() == 0L ||
                    DoctorsTable.selectAll().count() == 0L ||
                    PatientsTable.selectAll().count() == 0L ||
                    ProcedureTypesTable.selectAll().count() == 0L ||
                    TreatmentCategoriesTable.selectAll().count() == 0L ||
                    InventoryCategoriesTable.selectAll().count() == 0L ||
                    SuppliersTable.selectAll().count() == 0L

            if (!anyEmpty) return@transaction

            println("[DemoData] Seeding demo clinic data...")

            val users = UsersSeeder.seed(config)
            val doctors = DoctorsSeeder.seed(config)
            val patients = PatientsSeeder.seed(config)
            val categories = TreatmentCategoriesSeeder.seed(config)
            val procedures = ProceduresSeeder.seed(config)
            val facilities = FacilitiesSeeder.seed(config)
            val consultories = ConsultoriesSeeder.seed(config)

            MaterialInventorySeeder.seed(config, consultories, facilities)

            val inventoryCategories = InventoryCategoriesSeeder.seed(config)
            val suppliers = SuppliersSeeder.seed(config)
            InventoryProductsSeeder.seed(config, inventoryCategories, suppliers)

            val appointments =
                AppointmentsSeeder.seed(
                    config = config,
                    patients = patients,
                    doctors = doctors,
                    procedures = procedures,
                )

            PerformedActionsSeeder.seed(config, appointments, patients, doctors, procedures)
            PaymentsSeeder.seed(config, appointments, patients, procedures)
            NotesAuditSeeder.seed(config, appointments)
            ClinicalWorkspaceSeeder.seed(config, patients, doctors, procedures)
            TreatmentPlansSeeder.seed(config, patients)

            println("[DemoData] Seeding complete.")
        }
    }

    fun isSeeded(): Boolean =
        transaction {
            UsersTable.selectAll().count() > 0 &&
                DoctorsTable.selectAll().count() > 0 &&
                PatientsTable.selectAll().count() > 0
        }
}
