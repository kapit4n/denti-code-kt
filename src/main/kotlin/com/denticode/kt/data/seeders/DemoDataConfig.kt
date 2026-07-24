package com.denticode.kt.data.seeders

data class DemoDataConfig(
    val doctorCount: Int = 5,
    val patientCount: Int = 150,
    val procedureTypeCount: Int = 28,
    val facilityCount: Int = 32,
    val consultoryCount: Int = 3,
    val appointmentCount: Int = 300,
    val appointmentPastDays: Int = 30,
    val appointmentFutureDays: Int = 15,
    val minimumPaymentRatio: Double = 0.75,
    val clinicName: String = "Clínica Dental Sonrisa",
    val clinicCity: String = "Cochabamba",
    val clinicCountry: String = "Bolivia",
    val currencySymbol: String = "Bs.",
) {
    companion object {
        val DEFAULT = DemoDataConfig()
    }
}
