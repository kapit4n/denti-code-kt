package com.denticode.kt.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector

sealed class ScreenRoute(
    val id: String,
    val title: String,
    val subtitle: String? = null,
) {
    data object Dashboard : ScreenRoute("dashboard", "Panel", "Resumen de clínica")

    data object Appointments : ScreenRoute("appointments", "Citas", "Agenda y estados")

    data object Patients : ScreenRoute("patients", "Pacientes", "Historial demográfico")

    data object Doctors : ScreenRoute("doctors", "Doctores", "Directorio clínico")

    data object Procedures : ScreenRoute("procedures", "Catálogo clínico", "Tipos de procedimiento")

    data object Inventory : ScreenRoute("inventory", "Stock insumos", "Por consultorio")

    data object Payments : ScreenRoute("payments", "Pagos", "Cobros recientes")

    companion object {
        val mainMenu: List<ScreenRoute> =
            listOf(
                Dashboard,
                Appointments,
                Patients,
                Doctors,
                Procedures,
                Inventory,
                Payments,
            )

        fun fromId(id: String): ScreenRoute? = mainMenu.find { it.id == id }
    }
}

data class NavigationItem(
    val route: ScreenRoute,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String = label,
)

fun navigationItems(): List<NavigationItem> =
    listOf(
        NavigationItem(ScreenRoute.Dashboard, "Panel", Icons.Default.Dashboard),
        NavigationItem(ScreenRoute.Appointments, "Citas", Icons.Default.CalendarMonth),
        NavigationItem(ScreenRoute.Patients, "Pacientes", Icons.Default.People),
        NavigationItem(ScreenRoute.Doctors, "Doctores", Icons.Default.LocalHospital),
        NavigationItem(ScreenRoute.Procedures, "Catálogo", Icons.Default.Category),
        NavigationItem(ScreenRoute.Inventory, "Stock", Icons.Default.Inventory2),
        NavigationItem(ScreenRoute.Payments, "Pagos", Icons.Default.Payments),
    )

data class BreadcrumbSegment(
    val label: String,
    val onClick: (() -> Unit)? = null,
)
