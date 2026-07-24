package com.denticode.kt.data.seeders

import com.denticode.kt.data.DoctorsTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

object DoctorsSeeder {

    data class SeedDoctor(
        val id: Int,
        val firstName: String,
        val lastName: String,
        val email: String,
        val phone: String,
        val specialization: String,
        val licenseNumber: String,
        val officeRoom: String?,
    )

    fun seed(config: DemoDataConfig): List<SeedDoctor> {
        val existing =
            DoctorsTable
                .selectAll()
                .map {
                    SeedDoctor(
                        id = it[DoctorsTable.id],
                        firstName = it[DoctorsTable.firstName],
                        lastName = it[DoctorsTable.lastName],
                        email = it[DoctorsTable.email],
                        phone = it[DoctorsTable.contactPhone] ?: "",
                        specialization = it[DoctorsTable.specialization] ?: "",
                        licenseNumber = it[DoctorsTable.licenseNumber] ?: "",
                        officeRoom = it[DoctorsTable.officeRoom],
                    )
                }
        if (existing.isNotEmpty()) return existing

        val doctors =
            listOf(
                SeedDoctor(0, "Carlos", "Mamani Quispe", "c.mamani@sonrisa-clinica.bo", "76451230", "Odontología General", "LB-1234", "C1"),
                SeedDoctor(0, "Ana", "Torres Ríos", "a.torres@sonrisa-clinica.bo", "76543210", "Endodoncia", "LB-2345", "C2"),
                SeedDoctor(0, "Miguel", "Gutiérrez Vaca", "m.gutierrez@sonrisa-clinica.bo", "77123456", "Ortodoncia", "LB-3456", "C3"),
                SeedDoctor(0, "Patricia", "López Fernández", "p.lopez@sonrisa-clinica.bo", "78901234", "Periodoncia", "LB-4567", "C1"),
                SeedDoctor(0, "Roberto", "Cáceres Mendoza", "r.caceres@sonrisa-clinica.bo", "76098765", "Cirugía Oral", "LB-5678", "C2"),
            )

        return doctors.take(config.doctorCount).map { d ->
            val doctorId =
                DoctorsTable.insert {
                    it[firstName] = d.firstName
                    it[lastName] = d.lastName
                    it[email] = d.email
                    it[contactPhone] = d.phone
                    it[specialization] = d.specialization
                    it[licenseNumber] = d.licenseNumber
                    it[officeRoom] = d.officeRoom
                    it[isActive] = true
                } get DoctorsTable.id

            d.copy(id = doctorId)
        }
    }
}
