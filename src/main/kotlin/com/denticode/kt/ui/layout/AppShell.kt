package com.denticode.kt.ui.layout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.Doctor
import com.denticode.kt.data.Patient
import com.denticode.kt.ui.AppointmentsScreen
import com.denticode.kt.ui.DashboardScreen
import com.denticode.kt.ui.DoctorsScreen
import com.denticode.kt.ui.InventoryStockScreen
import com.denticode.kt.ui.PatientsScreen
import com.denticode.kt.ui.PaymentsScreen
import com.denticode.kt.ui.PatientDetailWindow
import com.denticode.kt.ui.DoctorDetailWindow
import com.denticode.kt.ui.PlaceholderScreen
import com.denticode.kt.ui.ProceduresScreen
import com.denticode.kt.ui.app.AppMessenger
import com.denticode.kt.ui.app.LocalAppMessenger
import com.denticode.kt.ui.app.LocalSnackbarHostState
import com.denticode.kt.ui.navigation.AppSidebar
import com.denticode.kt.ui.navigation.AppTopBar
import com.denticode.kt.ui.navigation.BreadcrumbSegment
import com.denticode.kt.ui.navigation.CommandPaletteDialog
import com.denticode.kt.ui.navigation.ScreenRoute
import com.denticode.kt.ui.navigation.rememberNavigationState
import com.denticode.kt.ui.patientdetail.PatientDetailFocusSection
import com.denticode.kt.ui.theme.AppTheme
import kotlinx.coroutines.launch

private data class PatientDetailLaunch(
    val patient: Patient,
    val focusSection: PatientDetailFocusSection = PatientDetailFocusSection.OVERVIEW,
)

private data class DoctorDetailLaunch(
    val doctor: Doctor,
)

@Composable
fun AppShell(repo: DentiRepository) {
    val navigationState = rememberNavigationState()
    var searchQuery by remember { mutableStateOf("") }
    var patientDetailLaunch by remember { mutableStateOf<PatientDetailLaunch?>(null) }
    var doctorDetailLaunch by remember { mutableStateOf<DoctorDetailLaunch?>(null) }
    var appointmentsDoctorFilterId by remember { mutableStateOf<Int?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val messenger =
        remember(snackbarHostState, scope) {
            AppMessenger(
                showSuccess = { msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                },
                showError = { msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                },
            )
        }
    AppTheme(darkTheme = navigationState.useDarkTheme) {
        CompositionLocalProvider(
            LocalSnackbarHostState provides snackbarHostState,
            LocalAppMessenger provides messenger,
        ) {
            BoxWithWindowSize { windowSize ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .onPreviewKeyEvent { ev ->
                            if (ev.type == KeyEventType.KeyDown && ev.key == Key.K && ev.isCtrlPressed) {
                                navigationState.toggleCommandPalette()
                                true
                            } else {
                                false
                            }
                        },
                ) {
                    AppScaffold(
                        sidebar = { AppSidebar(navigationState = navigationState) },
                        topBar = {
                            AppTopBar(
                                title = navigationState.currentRoute.title,
                                windowSize = windowSize,
                                breadcrumbs =
                                    listOf(
                                        BreadcrumbSegment("Denti-Code"),
                                        BreadcrumbSegment(navigationState.currentRoute.title),
                                    ),
                                searchValue = searchQuery,
                                onSearchValueChange = { searchQuery = it },
                                searchPlaceholder = "Buscar en la aplicación…",
                                useDarkTheme = navigationState.useDarkTheme,
                                onToggleDarkTheme = { navigationState.toggleDarkTheme() },
                                onOpenCommandPalette = { navigationState.updateCommandPaletteVisible(true) },
                                onQuickNewAppointment = { navigationState.navigateTo(ScreenRoute.Appointments) },
                                onQuickNewPatient = { navigationState.navigateTo(ScreenRoute.Patients) },
                                onQuickNewPayment = { navigationState.navigateTo(ScreenRoute.Payments) },
                                showQuickActions = navigationState.currentRoute != ScreenRoute.Appointments,
                            )
                        },
                        content = {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AnimatedContent(
                                    targetState = navigationState.currentRoute,
                                    modifier = Modifier.fillMaxSize(),
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(220)) togetherWith
                                            fadeOut(animationSpec = tween(180))
                                    },
                                    label = "shellRoute",
                                ) { route ->
                                    ContentContainer(
                                        windowSize = windowSize,
                                            maxContentWidth =
                                            if (route == ScreenRoute.Dashboard || route == ScreenRoute.Appointments) {
                                                null
                                            } else {
                                                responsiveMaxContentWidth(windowSize)
                                            },
                                    ) {
                                        when (route) {
                                            ScreenRoute.Dashboard ->
                                                DashboardScreen(
                                                    repo = repo,
                                                    onNavigate = { navigationState.navigateTo(it) },
                                                )
                                            ScreenRoute.Appointments ->
                                                AppointmentsScreen(
                                                    repo = repo,
                                                    onNavigate = { navigationState.navigateTo(it) },
                                                    onOpenPatient = { patient, section ->
                                                        patientDetailLaunch =
                                                            PatientDetailLaunch(patient, section)
                                                    },
                                                    initialDoctorFilterId = appointmentsDoctorFilterId,
                                                    onInitialDoctorFilterConsumed = {
                                                        appointmentsDoctorFilterId = null
                                                    },
                                                )
                                            ScreenRoute.Patients ->
                                                PatientsScreen(
                                                    repo = repo,
                                                    onOpenPatientDetail = { patient ->
                                                        patientDetailLaunch =
                                                            PatientDetailLaunch(patient)
                                                    },
                                                )
                                            ScreenRoute.Doctors ->
                                                DoctorsScreen(
                                                    repo = repo,
                                                    onOpenDoctorDetail = { doctor ->
                                                        doctorDetailLaunch = DoctorDetailLaunch(doctor)
                                                    },
                                                    onViewDoctorSchedule = { doctor ->
                                                        appointmentsDoctorFilterId = doctor.id
                                                        navigationState.navigateTo(ScreenRoute.Appointments)
                                                    },
                                                )
                                            ScreenRoute.Procedures -> ProceduresScreen(repo)
                                            ScreenRoute.Inventory -> InventoryStockScreen(repo)
                                            ScreenRoute.Payments ->
                                                PaymentsScreen(
                                                    repo = repo,
                                                    onOpenPatient = { patient ->
                                                        patientDetailLaunch =
                                                            PatientDetailLaunch(patient)
                                                    },
                                                )
                                            ScreenRoute.Reports,
                                            ScreenRoute.Users,
                                            ScreenRoute.Settings,
                                            -> PlaceholderScreen(route)
                                        }
                                    }
                                }
                                SnackbarHost(
                                    hostState = snackbarHostState,
                                    modifier =
                                        Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(24.dp),
                                )
                            }
                        },
                    )
                    if (navigationState.commandPaletteVisible) {
                        CommandPaletteDialog(
                            navigationState = navigationState,
                            onDismiss = { navigationState.updateCommandPaletteVisible(false) },
                        )
                    }
                    patientDetailLaunch?.let { launch ->
                        Window(
                            onCloseRequest = { patientDetailLaunch = null },
                            title = "Paciente · ${launch.patient.fullName}",
                            state = rememberWindowState(size = DpSize(920.dp, 720.dp)),
                        ) {
                            AppTheme(darkTheme = navigationState.useDarkTheme) {
                                CompositionLocalProvider(LocalAppMessenger provides messenger) {
                                    PatientDetailWindow(
                                        repo = repo,
                                        patient = launch.patient,
                                        focusSection = launch.focusSection,
                                        onClose = { patientDetailLaunch = null },
                                    )
                                }
                            }
                        }
                    }
                    doctorDetailLaunch?.let { launch ->
                        Window(
                            onCloseRequest = { doctorDetailLaunch = null },
                            title = "Doctor · ${launch.doctor.fullName}",
                            state = rememberWindowState(size = DpSize(920.dp, 720.dp)),
                        ) {
                            AppTheme(darkTheme = navigationState.useDarkTheme) {
                                CompositionLocalProvider(LocalAppMessenger provides messenger) {
                                    DoctorDetailWindow(
                                        repo = repo,
                                        doctor = launch.doctor,
                                        onClose = { doctorDetailLaunch = null },
                                        onViewSchedule = { doctor ->
                                            appointmentsDoctorFilterId = doctor.id
                                            navigationState.navigateTo(ScreenRoute.Appointments)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
