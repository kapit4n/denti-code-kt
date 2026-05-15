package com.denticode.kt.ui.appointments

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.data.AppointmentRow
import com.denticode.kt.data.AppointmentStatus
import com.denticode.kt.data.Doctor
import com.denticode.kt.data.Patient
import com.denticode.kt.ui.app.LocalAppMessenger
import com.denticode.kt.ui.components.inputs.AppSearchField
import com.denticode.kt.ui.navigation.ScreenRoute
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import com.denticode.kt.ui.parseAppointmentScheduledAt
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsPremiumContent(
    appointmentRows: List<AppointmentRow>,
    patients: List<Patient>,
    doctors: List<Doctor>,
    onNewAppointment: () -> Unit,
    onEditAppointment: (Int) -> Unit,
    onNavigate: (ScreenRoute) -> Unit,
    onOpenPatient: (Patient) -> Unit,
    modifier: Modifier = Modifier,
) {
    val messenger = LocalAppMessenger.current
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var calendarMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    var selectedAppointmentId by remember { mutableStateOf<Int?>(null) }
    var selectedDoctor by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<AppointmentStatus?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var weekOffset by remember { mutableIntStateOf(0) }
    var timelineVisibleCount by remember { mutableIntStateOf(40) }
    var reminderText by remember { mutableStateOf("Hola, le recordamos su cita en la clínica. ¡Gracias!") }
    var viewMode by remember { mutableStateOf(AppointmentViewMode.WEEK) }

    var rangeMenu by remember { mutableStateOf(false) }
    var doctorMenu by remember { mutableStateOf(false) }
    var statusMenu by remember { mutableStateOf(false) }
    var filtersSummaryMenu by remember { mutableStateOf(false) }

    /** One-shot: if DB citas are outside "this week", jump to the earliest appointment's week so lists are not blank. */
    var didAlignNavigationToAppointments by remember { mutableStateOf(false) }

    val patientMap = remember(patients) { patientsById(patients) }
    val weekRange =
        remember(weekOffset) {
            val anchor = LocalDate.now().plusWeeks(weekOffset.toLong())
            weekRangeFor(anchor)
        }

    val uiModels =
        remember(appointmentRows, patientMap) {
            appointmentRows.map { row ->
                val p = patientMap[row.patientId]
                row.toUiModel(p?.contactPhone, p?.createdAtEpochMs)
            }
        }

    /** Full list after search + doctor + status. Do not pre-limit by calendar week here — that made seeded/off-week appointments disappear. */
    val searched by remember {
        derivedStateOf {
            val q = searchQuery.trim()
            if (q.isEmpty()) {
                uiModels
            } else {
                uiModels.filter { a ->
                    a.patientName.contains(q, ignoreCase = true) ||
                        a.doctorName.contains(q, ignoreCase = true) ||
                        a.treatmentName.contains(q, ignoreCase = true)
                }
            }
        }
    }

    val doctorFiltered by remember {
        derivedStateOf {
            val d = selectedDoctor
            if (d == null) searched else searched.filter { it.doctorName.trim().equals(d.trim(), ignoreCase = false) }
        }
    }

    val statusFiltered by remember {
        derivedStateOf {
            val s = selectedStatus
            if (s == null) doctorFiltered else doctorFiltered.filter { it.status == s }
        }
    }

    LaunchedEffect(appointmentRows.isEmpty()) {
        if (appointmentRows.isEmpty()) didAlignNavigationToAppointments = false
    }

    LaunchedEffect(appointmentRows) {
        if (didAlignNavigationToAppointments || appointmentRows.isEmpty()) return@LaunchedEffect
        val dates = appointmentRows.map { parseAppointmentScheduledAt(it.scheduledAt).toLocalDate() }
        val defaultWeek = weekRangeFor(LocalDate.now())
        val anyInDefaultWeek =
            dates.any { d ->
                !d.isBefore(defaultWeek.first) && !d.isAfter(defaultWeek.second)
            }
        if (!anyInDefaultWeek) {
            val firstDate = dates.minOrNull() ?: return@LaunchedEffect
            val anchorMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val targetMonday = firstDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val deltaWeeks = ChronoUnit.WEEKS.between(anchorMonday, targetMonday).toInt().coerceIn(-520, 520)
            weekOffset = deltaWeeks
            selectedDate = firstDate
            calendarMonth = YearMonth.from(firstDate)
        }
        didAlignNavigationToAppointments = true
    }

    LaunchedEffect(viewMode, selectedDate, weekOffset, calendarMonth, selectedDoctor, selectedStatus, searchQuery) {
        timelineVisibleCount = 40
    }

    val timelineSource by remember {
        derivedStateOf {
            when (viewMode) {
                AppointmentViewMode.DAY ->
                    statusFiltered
                        .filter { it.scheduledAt.toLocalDate() == selectedDate }
                        .sortedBy { it.scheduledAt }
                AppointmentViewMode.WEEK ->
                    statusFiltered
                        .filter { a ->
                            val d = a.scheduledAt.toLocalDate()
                            !d.isBefore(weekRange.first) && !d.isAfter(weekRange.second)
                        }.sortedBy { it.scheduledAt }
                AppointmentViewMode.MONTH ->
                    statusFiltered
                        .filter { YearMonth.from(it.scheduledAt) == calendarMonth }
                        .sortedBy { it.scheduledAt }
            }
        }
    }

    val visibleTimeline by remember {
        derivedStateOf { timelineSource.take(timelineVisibleCount) }
    }

    val selectedUi by remember {
        derivedStateOf { selectedAppointmentId?.let { id -> statusFiltered.find { it.id == id } } }
    }

    val dayStats by remember {
        derivedStateOf {
            val day = statusFiltered.filter { it.scheduledAt.toLocalDate() == selectedDate }
            DaySummaryStats(
                total = day.size,
                inProgress = day.count { it.status == AppointmentStatus.IN_PROGRESS },
                completed = day.count { it.status == AppointmentStatus.COMPLETED },
                cancelled = day.count { it.status == AppointmentStatus.CANCELLED || it.status == AppointmentStatus.NO_SHOW },
            )
        }
    }

    val activeFilterCount = (if (selectedDoctor != null) 1 else 0) + (if (selectedStatus != null) 1 else 0)

    val timelineTitle by remember {
        derivedStateOf {
            when (viewMode) {
                AppointmentViewMode.DAY -> formatTimelineDayHeader(selectedDate)
                AppointmentViewMode.WEEK -> "Semana del ${formatRangeLabel(weekRange.first, weekRange.second)}"
                AppointmentViewMode.MONTH -> formatMonthTitle(calendarMonth)
            }
        }
    }

    val emptyTimelineHint by remember {
        derivedStateOf {
            if (timelineSource.isNotEmpty()) return@derivedStateOf ""
            when {
                appointmentRows.isEmpty() ->
                    "No hay citas. Use «Nueva cita» para programar la primera."
                searchQuery.trim().isNotEmpty() && searched.isEmpty() ->
                    "Ningún resultado para «${searchQuery.trim()}». Pruebe otras palabras."
                selectedDoctor != null && doctorFiltered.isEmpty() ->
                    "Ninguna cita con el doctor seleccionado."
                selectedStatus != null &&
                    doctorFiltered.isNotEmpty() &&
                    statusFiltered.isEmpty() ->
                    "Ninguna cita con el estado seleccionado."
                else ->
                    "No hay citas en el día, semana o mes mostrado. Cambie la fecha o use «Rango»."
            }
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppointmentPremiumPalette.background)
                .padding(bottom = AppSpacing.md),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Column(Modifier.widthIn(min = 200.dp, max = 260.dp)) {
                Text(
                    buildString {
                        append("Denti-Code")
                        append("  ")
                        append('>')
                        append("  ")
                        append("Citas")
                    },
                    style = AppTypography.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AppSearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f).widthIn(max = 520.dp),
                placeholder = "Buscar paciente, doctor o tratamiento…",
                keyboardShortcutHint = "Ctrl+K",
            )
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
                AppButton(
                    text = "Nueva cita",
                    onClick = onNewAppointment,
                    minHeight = 40.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    },
                )
                HeaderActionButton("Nuevo paciente", { onNavigate(ScreenRoute.Patients) }, leadingIcon = Icons.Default.People)
                HeaderActionButton("Nuevo pago", { onNavigate(ScreenRoute.Payments) }, leadingIcon = Icons.Default.Payments)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(0.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { /* focus search */ }) {
                    Icon(Icons.Default.Search, "Buscar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { /* theme placeholder */ }) {
                    Icon(Icons.Outlined.DarkMode, "Tema", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { messenger.showSuccess("Sin notificaciones nuevas.") }) {
                    Box {
                        Icon(Icons.Default.Notifications, "Alertas", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Box(
                            Modifier
                                .align(Alignment.TopEnd)
                                .size(7.dp)
                                .background(AppointmentPremiumPalette.error, shape = CircleShape),
                        )
                    }
                }
                PatientAvatar("Recepción", size = 36.dp)
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BoxWithConstraints(Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), modifier = Modifier.fillMaxWidth()) {
                    FilterDropdown(
                        label = "Rango",
                        displayValue = formatRangeLabel(weekRange.first, weekRange.second),
                        expanded = rangeMenu,
                        onExpandedChange = { rangeMenu = it },
                        modifier = Modifier.weight(1f, fill = false),
                    ) {
                        DropdownMenuItem(
                            text = { Text("Semana anterior") },
                            onClick = {
                                weekOffset--
                                rangeMenu = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Esta semana") },
                            onClick = {
                                weekOffset = 0
                                rangeMenu = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Próxima semana") },
                            onClick = {
                                weekOffset++
                                rangeMenu = false
                            },
                        )
                    }
                    FilterDropdown(
                        label = "Doctor",
                        displayValue = selectedDoctor ?: "Todos los doctores",
                        expanded = doctorMenu,
                        onExpandedChange = { doctorMenu = it },
                        modifier = Modifier.weight(1f, fill = false),
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos los doctores") },
                            onClick = {
                                selectedDoctor = null
                                doctorMenu = false
                            },
                        )
                        doctors.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d.fullName) },
                                onClick = {
                                    selectedDoctor = d.fullName
                                    doctorMenu = false
                                },
                            )
                        }
                    }
                    FilterDropdown(
                        label = "Estado",
                        displayValue = selectedStatus?.displayLabel ?: "Todos los estados",
                        expanded = statusMenu,
                        onExpandedChange = { statusMenu = it },
                        modifier = Modifier.weight(1f, fill = false),
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos los estados") },
                            onClick = {
                                selectedStatus = null
                                statusMenu = false
                            },
                        )
                        listOf(
                            AppointmentStatus.CONFIRMED,
                            AppointmentStatus.IN_PROGRESS,
                            AppointmentStatus.SCHEDULED,
                            AppointmentStatus.COMPLETED,
                            AppointmentStatus.CANCELLED,
                        ).forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st.displayLabel) },
                                onClick = {
                                    selectedStatus = st
                                    statusMenu = false
                                },
                            )
                        }
                    }
                    FilterDropdown(
                        label = "Filtros",
                        displayValue = if (activeFilterCount > 0) "$activeFilterCount activos" else "Ninguno",
                        expanded = filtersSummaryMenu,
                        onExpandedChange = { filtersSummaryMenu = it },
                        badgeCount = activeFilterCount.takeIf { it > 0 },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Limpiar filtros") },
                            onClick = {
                                selectedDoctor = null
                                selectedStatus = null
                                filtersSummaryMenu = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Ir a hoy") },
                            onClick = {
                                selectedDate = LocalDate.now()
                                calendarMonth = YearMonth.from(selectedDate)
                                filtersSummaryMenu = false
                            },
                        )
                    }
                }
            }
            AppointmentViewModeToggle(
                mode = viewMode,
                onModeChange = { viewMode = it },
                modifier = Modifier.width(236.dp),
            )
        }

        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = AppSpacing.sm),
        ) {
            val wide = maxWidth >= 1080.dp
            if (wide) {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    Column(
                        Modifier.width(280.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    ) {
                        MiniCalendar(
                            month = calendarMonth,
                            selectedDate = selectedDate,
                            onMonthChange = { calendarMonth = it },
                            onSelectDate = {
                                selectedDate = it
                                calendarMonth = YearMonth.from(it)
                                selectedAppointmentId = null
                            },
                        )
                        DaySummaryCard(
                            stats = dayStats,
                            onViewAgenda = { messenger.showSuccess("Agenda del ${selectedDate}") },
                        )
                    }
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        Text(
                            timelineTitle,
                            style = AppTypography.CardTitle,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = AppSpacing.sm),
                        )
                        Box(Modifier.weight(1f).fillMaxWidth()) {
                            if (visibleTimeline.isEmpty()) {
                                Text(
                                    emptyTimelineHint,
                                    style = AppTypography.Body,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier =
                                        Modifier
                                            .align(Alignment.Center)
                                            .padding(horizontal = AppSpacing.lg),
                                )
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    items(visibleTimeline, key = { it.id }) { item ->
                                        AppointmentTimelineCard(
                                            item = item,
                                            selected = item.id == selectedAppointmentId,
                                            onClick = {
                                                selectedAppointmentId = item.id
                                            },
                                        )
                                    }
                                }
                            }
                        }
                        if (visibleTimeline.size < timelineSource.size) {
                            com.denticode.kt.ui.components.buttons.AppOutlinedButton(
                                text = "Cargar más citas",
                                onClick = { timelineVisibleCount += 30 },
                                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.md),
                                minHeight = 44.dp,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                },
                            )
                        }
                    }
                    AppointmentDetailPanel(
                        appointment = selectedUi,
                        reminderPreview = reminderText,
                        onReminderPreviewChange = { reminderText = it },
                        onReminderSend = {
                            messenger.showSuccess("Recordatorio preparado (simulación).")
                        },
                        onClose = { selectedAppointmentId = null },
                        onEditAppointment = { selectedAppointmentId?.let { onEditAppointment(it) } },
                        onReschedule = { messenger.showSuccess("Reprogramación: use editar cita.") },
                        onCancelAppointment = { messenger.showSuccess("Cancelación: use editar cita.") },
                        onViewPatient = {
                            selectedUi?.let { su ->
                                val p = patientMap[su.patientId]
                                if (p != null) {
                                    onOpenPatient(p)
                                } else {
                                    messenger.showError("Paciente no encontrado.")
                                }
                            }
                        },
                        onClinicalHistory = { onNavigate(ScreenRoute.Patients) },
                        onRegisterPayment = { onNavigate(ScreenRoute.Payments) },
                        modifier = Modifier.width(360.dp),
                    )
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    MiniCalendar(
                        month = calendarMonth,
                        selectedDate = selectedDate,
                        onMonthChange = { calendarMonth = it },
                        onSelectDate = {
                            selectedDate = it
                            calendarMonth = YearMonth.from(it)
                            selectedAppointmentId = null
                        },
                    )
                    DaySummaryCard(
                        stats = dayStats,
                        onViewAgenda = { messenger.showSuccess("Agenda del día") },
                    )
                    Text(
                        timelineTitle,
                        style = AppTypography.CardTitle,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (visibleTimeline.isEmpty()) {
                        Text(
                            emptyTimelineHint,
                            style = AppTypography.Body,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = AppSpacing.lg),
                        )
                    } else {
                        visibleTimeline.forEach { item ->
                            AppointmentTimelineCard(
                                item = item,
                                selected = item.id == selectedAppointmentId,
                                onClick = { selectedAppointmentId = item.id },
                            )
                        }
                    }
                    if (visibleTimeline.size < timelineSource.size) {
                        com.denticode.kt.ui.components.buttons.AppOutlinedButton(
                            text = "Cargar más citas",
                            onClick = { timelineVisibleCount += 30 },
                            modifier = Modifier.fillMaxWidth(),
                            minHeight = 44.dp,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            },
                        )
                    }
                    AppointmentDetailPanel(
                        appointment = selectedUi,
                        reminderPreview = reminderText,
                        onReminderPreviewChange = { reminderText = it },
                        onReminderSend = { messenger.showSuccess("Recordatorio preparado (simulación).") },
                        onClose = { selectedAppointmentId = null },
                        onEditAppointment = { selectedAppointmentId?.let { onEditAppointment(it) } },
                        onReschedule = { },
                        onCancelAppointment = { },
                        onViewPatient = {
                            selectedUi?.let { su ->
                                patientMap[su.patientId]?.let { onOpenPatient(it) }
                            }
                        },
                        onClinicalHistory = { onNavigate(ScreenRoute.Patients) },
                        onRegisterPayment = { onNavigate(ScreenRoute.Payments) },
                        modifier = Modifier.fillMaxWidth().height(520.dp),
                    )
                }
            }
        }
    }
}
