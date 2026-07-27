package com.denticode.kt.data.seeders

import com.denticode.kt.data.SuppliersTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

object SuppliersSeeder {

    data class SeedSupplier(
        val id: Int,
        val name: String,
        val contactName: String?,
        val phone: String?,
        val email: String?,
    )

    fun seed(config: DemoDataConfig): List<SeedSupplier> {
        val existing = SuppliersTable.selectAll().map {
            SeedSupplier(
                id = it[SuppliersTable.id],
                name = it[SuppliersTable.name],
                contactName = it[SuppliersTable.contactName],
                phone = it[SuppliersTable.phone],
                email = it[SuppliersTable.email],
            )
        }
        if (existing.isNotEmpty()) return existing

        val suppliers = listOf(
            SeedSupplier(0, "Dental Supply Bolivia", "Carlos Mamani", "+591 2 2345678", "ventas@dentalbo.com"),
            SeedSupplier(0, "Insumos Médicos Plus", "María Condori", "+591 2 3456789", "info@insumosplus.com"),
            SeedSupplier(0, "DentalTech SRL", "José Quispe", "+591 2 4567890", "ventas@dentaltech.com"),
            SeedSupplier(0, "MediDental Bolivia", "Ana Flores", "+591 2 5678901", "pedidos@medidental.com"),
            SeedSupplier(0, "Suministros Odontológicos", "Luis Copa", "+591 2 6789012", "contacto@suminodonto.com"),
        )

        return suppliers.map { s ->
            val supId = SuppliersTable.insert {
                it[name] = s.name
                it[contactName] = s.contactName
                it[phone] = s.phone
                it[email] = s.email
                it[isActive] = true
            } get SuppliersTable.id
            s.copy(id = supId)
        }
    }
}
