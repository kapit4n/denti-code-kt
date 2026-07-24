package com.denticode.kt.data.seeders

import com.denticode.kt.data.AppointmentStatus
import com.denticode.kt.data.PaymentMethod
import com.denticode.kt.data.PaymentsTable
import com.denticode.kt.data.ProcedureTypesTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

object PaymentsSeeder {

    private val ISO_DT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    private val paymentNotes =
        listOf(
            "Pago completo",
            "Pago en efectivo",
            "Tarjeta de crédito",
            "Transferencia bancaria",
            "Pago con seguro",
            "Anticipo de tratamiento",
            "Cuota parcial",
            "Pago diferido",
            null,
            null,
        )

    fun seed(
        config: DemoDataConfig,
        appointments: List<AppointmentsSeeder.SeedAppointment>,
        patients: List<PatientsSeeder.SeedPatient>,
        procedures: List<ProceduresSeeder.SeedProcedure>,
    ) {
        if (PaymentsTable.selectAll().count() > 0) return

        val rng = Random(55)
        val procedureMap = procedures.associateBy { it.id }
        val paymentMethods = PaymentMethod.entries

        var paymentCount = 0
        val targetPayments = 500

        val completedAppts = appointments.filter { it.status == AppointmentStatus.COMPLETED }

        for (appt in completedAppts) {
            if (paymentCount >= targetPayments) break

            val procedure = procedureMap[appt.procedureId]
            val baseAmount = procedure?.priceBs ?: 200.0

            val shouldPayFull = rng.nextDouble() < config.minimumPaymentRatio
            val amount =
                if (shouldPayFull) {
                    baseAmount * (0.9 + rng.nextDouble() * 0.2)
                } else {
                    baseAmount * (0.3 + rng.nextDouble() * 0.4)
                }

            val method = paymentMethods[rng.nextInt(paymentMethods.size)]
            val paidAt = LocalDateTime.parse(appt.scheduledAt).plusMinutes(rng.nextLong(0, 120))

            PaymentsTable.insert {
                it[patientId] = appt.patientId
                it[appointmentId] = appt.id
                it[PaymentsTable.amount] = amount
                it[PaymentsTable.method] = method.name
                it[PaymentsTable.paidAt] = paidAt.format(ISO_DT)
                it[note] = paymentNotes[rng.nextInt(paymentNotes.size)]
                it[procedureTypeId] = appt.procedureId
            }
            paymentCount++

            if (!shouldPayFull && rng.nextDouble() > 0.5) {
                val secondAmount = baseAmount - amount
                if (secondAmount > 0) {
                    val secondMethod = paymentMethods[rng.nextInt(paymentMethods.size)]
                    val secondPaidAt = paidAt.plusDays(rng.nextLong(1, 15))

                    PaymentsTable.insert {
                        it[patientId] = appt.patientId
                        it[appointmentId] = appt.id
                        it[PaymentsTable.amount] = secondAmount
                        it[PaymentsTable.method] = secondMethod.name
                        it[PaymentsTable.paidAt] = secondPaidAt.format(ISO_DT)
                        it[note] = "Pago complementario"
                        it[procedureTypeId] = appt.procedureId
                    }
                    paymentCount++
                }
            }
        }

        val scheduledAppts = appointments.filter { it.status == AppointmentStatus.SCHEDULED || it.status == AppointmentStatus.CONFIRMED }
        for (appt in scheduledAppts) {
            if (paymentCount >= targetPayments) break
            if (rng.nextDouble() > 0.25) continue

            val procedure = procedureMap[appt.procedureId]
            val baseAmount = procedure?.priceBs ?: 200.0
            val anticipo = baseAmount * (0.2 + rng.nextDouble() * 0.3)

            val method = paymentMethods[rng.nextInt(paymentMethods.size)]
            val paidAt = LocalDateTime.parse(appt.scheduledAt).minusDays(rng.nextLong(1, 7))

            PaymentsTable.insert {
                it[patientId] = appt.patientId
                it[appointmentId] = appt.id
                it[PaymentsTable.amount] = anticipo
                it[PaymentsTable.method] = method.name
                it[PaymentsTable.paidAt] = paidAt.format(ISO_DT)
                it[note] = "Anticipo de cita"
                it[procedureTypeId] = appt.procedureId
            }
            paymentCount++
        }
    }
}
