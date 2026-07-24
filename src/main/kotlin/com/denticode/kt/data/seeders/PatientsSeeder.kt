package com.denticode.kt.data.seeders

import com.denticode.kt.data.PatientsTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

object PatientsSeeder {

    data class SeedPatient(
        val id: Int,
        val firstName: String,
        val lastName: String,
        val dateOfBirth: String,
        val gender: String?,
        val phone: String,
        val email: String?,
        val address: String?,
        val medicalSummary: String?,
    )

    private val bolivianFirstNames =
        listOf(
            "Ana", "María", "Juan", "Carlos", "Luis", "Rocío", "Patricia", "Miguel",
            "Sandra", "Jorge", "Claudia", "Fernando", "Gloria", "Roberto", "Elena",
            "Francisco", "Carmen", "Alejandro", "Teresa", "Raúl", "Silvia", "Oscar",
            "Verónica", "Mauricio", "Daniela", "Andrés", "Laura", "Diego", "Camila",
            "Mateo", "Valentina", "Santiago", "Isabella", "Sebastián", "Luciana",
            "Daniel", "Sofía", "Adrián", "Martina", "Pablo", "Regina", "Tomás",
            "Emilia", "Lucas", "Gabriela", "Nicolás", "Fernanda", "Benjamín",
            "Antonella", "Emiliano", "Mía", "Leonardo", "Paula", "Samuel", "Julia",
            "Ian", "Ariana", "Thiago", "Renata", "Gabriel", "Alejandra", "Mateo",
            "Victoria", "Ángel", "Lucero", "Maximiliano", "Daniela", "Iker",
            "Mariana", "Joaquín", "Ximena", "David", "Anaís", "Bruno", "Florencia",
            "Felipe", "Jazmín", "Agustín", "Luna", "Rafael", "Juliette", "Rodrigo",
            "Paola", "Martín", "Valeria", "Esteban", "Natalia", "Cristóbal", "Brenda",
            "Álvaro", "Micaela", "Hugo", "Carla", "Eduardo", "Lorena", "Gonzalo",
            "Stephanie", "Ricardo", "Tatiana", "Sergio", "Nayeli", "Alberto", "Diana",
            "Gustavo", "Eva", "Víctor", "Claudia", "Arturo", "Rosario", "Enrique",
            "Ivette", "Pedro", "Rocío", "Javier", "Andrea", "Fabián", "Liliana",
            "Mauricio", "Nora", "Óscar", "Elizabeth", "Raúl", "Sandra", "Luis",
            "Carolina", "Fernando", "Mónica", "Roberto", "Patricia", "Eduardo",
            "Rocío", "Gonzalo", "Adriana", "Jorge", "Gloria", "Ricardo", "Teresa",
            "Sergio", "Elena", "Hugo", "Carmen", "Iván", "Silvia",
        )

    private val bolivianLastNames =
        listOf(
            "Mamani", "Quispe", "Torres", "Gutiérrez", "López", "Cáceres", "Flores",
            "Rojas", "Vargas", "Mendoza", "Huanca", "Condori", "Aguirre", "Paredes",
            "Suárez", "Medina", "Arce", "Rivera", "Villarroel", "Claros", "Salazar",
            "Peñarrieta", "Terán", "Díaz", "Ávila", "Fernández", "García", "Martínez",
            "Sánchez", "Ramírez", "Ortiz", "Morales", "Castro", "Reyes", "Guzmán",
            "Navarro", "Romero", "Herrera", "Espinoza", "Córdoba", "Miranda", "Alvarado",
            "Cardozo", "Pinto", "Bustamante", "Soliz", "Lazarte", "Caballero", "Montaño",
            "Yañez", "Zegarra", "Ocampo", "Palacios", "Ricaldi", "Nogales", "Beltrán",
            "Escalante", "Vizcarra", "Segovia", "Balcazar", "Chávez", "Pinaya", "Quispe",
            "Chipana", "Achá", "Cornejo", "Gallardo", "Limachi", "Parra", "Tola",
            "Villca", "Zabala", "Bustillos", "Monasterios", "Pérez", "Torrico", "Uzeda",
            "Vallejos", "Wilde", "Zegarra", "Zuna", "Brañez", "Cusi", "Fuentes",
            "Guachalla", "Iriarte", "Kattan", "Luna", "Montes", "Orellana", "Prado",
            "Quiroga", "Ríos", "Soria", "Ureña", "Vidal", "Werner", "Zabalaga",
            "Acha", "Barrientos", "Choque", "Delgado", "Estrada", "Franco", "Herbas",
            "Iñiguez", "Jaldin", "Koldobsky", "Lazcano", "Machaca", "Nava", "Ochoa",
            "Pacheco", "Quintana", "Ramos", "Siles", "Taborga", "Uriona", "Vásquez",
            "X", "Yáñez", "Zárate",
        )

    private val cochabambaAddresses =
        listOf(
            "Av. Ballivián, Zona San Jorge", "Calle España, Zona Centro", "Av. Oquendo, Zona Cala Cala",
            "Calle Bolívar, Zona Sutususto", "Av. América, Zona Ticti", "Calle Colón, Zona Villa Fátima",
            "Av. Blanco Galindo, Zona Queru Queru", "Calle Mendez Arcos, Zona Glorieta",
            "Av. Circunvalación, Zona Tupuraya", "Calle 9 de Abril, Zona Miraflores",
            "Av. Heroínas, Zona Centro", "Calle Pando, Zona San Sebastián",
            "Av. Jordán, Zona Cercado", "Calle Sucre, Zona La Portada",
            "Av. Principal, Zona Villa Pagador", "Calle Junín, Zona San Ernesto",
        )

    private val medicalSummaries =
        listOf(
            "Sin antecedentes relevantes.",
            "Alergia a la penicilina.",
            "Hipertensión controlada.",
            "Diabetes tipo 2, en tratamiento.",
            "Embarazada, 2do trimestre.",
            "Antecedente de caries múltiples.",
            "Bruxismo leve.",
            "Prótesis dental superior.",
            "Fumador ocasional.",
            "Control odontológico periódico.",
            null,
            null,
            null,
        )

    fun seed(config: DemoDataConfig): List<SeedPatient> {
        if (PatientsTable.selectAll().count() > 0) return emptyList()

        val now = System.currentTimeMillis()
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        val rng = Random(42)

        return (1..config.patientCount).map { i ->
            val firstName = bolivianFirstNames[rng.nextInt(bolivianFirstNames.size)]
            val lastName1 = bolivianLastNames[rng.nextInt(bolivianLastNames.size)]
            val lastName2 = bolivianLastNames[rng.nextInt(bolivianLastNames.size)]
            val gender = if (i % 2 == 0) "F" else "M"
            val birthYear = rng.nextInt(1945, 2012)
            val birthMonth = rng.nextInt(1, 13)
            val birthDay = rng.nextInt(1, 29)
            val dob = LocalDate.of(birthYear, birthMonth, birthDay).format(fmt)
            val phone = "7${rng.nextInt(10, 99)}${rng.nextInt(100000, 999999)}"
            val email =
                if (rng.nextDouble() > 0.35) {
                    "${firstName.lowercase()}.${lastName1.lowercase()}${i}@mail.bo"
                } else {
                    null
                }
            val address = cochabambaAddresses[rng.nextInt(cochabambaAddresses.size)]
            val summary = medicalSummaries[rng.nextInt(medicalSummaries.size)]

            val patientId =
                PatientsTable.insert {
                    it[PatientsTable.firstName] = firstName
                    it[PatientsTable.lastName] = "$lastName1 $lastName2"
                    it[PatientsTable.dateOfBirth] = dob
                    it[PatientsTable.gender] = gender
                    it[PatientsTable.contactPhone] = phone
                    it[PatientsTable.email] = email
                    it[PatientsTable.address] = address
                    it[PatientsTable.medicalHistorySummary] = summary
                    it[PatientsTable.createdAtEpochMs] = now - rng.nextLong(0, 90L * 86_400_000)
                } get PatientsTable.id

            SeedPatient(
                id = patientId,
                firstName = firstName,
                lastName = "$lastName1 $lastName2",
                dateOfBirth = dob,
                gender = gender,
                phone = phone,
                email = email,
                address = address,
                medicalSummary = summary,
            )
        }
    }
}
