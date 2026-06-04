package com.denticode.kt.ui.appointments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import com.denticode.kt.ui.components.buttons.AppButton
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.denticode.kt.data.AppointmentDetailSnapshot
import com.denticode.kt.data.AppointmentRow
import com.denticode.kt.data.AppointmentStatus
import com.denticode.kt.data.Doctor
import com.denticode.kt.data.Patient
import com.denticode.kt.ui.app.LocalAppMessenger
import com.denticode.kt.ui.components.inputs.AppSearchField
import com.denticode.kt.ui.navigation.ScreenRoute
import com.denticode.kt.ui.patientdetail.PatientDetailFocusSection
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import com.denticode.kt.ui.parseAppointmentScheduledAt
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsPremiumContent(
    appointmentRows: List<AppointmentRow>,
    patients: List<Patient>,
    doctors: List<Doctor>,
    appointmentDetail: AppointmentDetailSnapshot?,
    detailLoading: Boolean,
    appointmentCountsByDate: Map<LocalDate, Int>,
    selectedAppointmentId: Int?,
    onSelectedAppointmentChange: (Int?) -> Unit,
    onNewAppointment: () -> Unit,
    onEditAppointment: (Int) -> Unit,
    onRescheduleAppointment: (Int) -> Unit,
    onCancelAppointment: (Int) -> Unit,
    onConfirmAppointment: (Int) -> Unit,
    onStartAppointment: (Int) -> Unit,
    onCompleteAppointment: (Int) -> Unit,
    onRegisterPaymentForAppointment: (Int) -> Unit,
    onRegisterTreatmentForAppointment: (Int) -> Unit,
    onAddNote: (Int, String) -> Unit,
    onEditNote: (Int, Int, String) -> Unit,
    onDeleteNote: (Int, Int) -> Unit,
    onCopyReminder: () -> Unit,
    onOpenWhatsAppReminder: () -> Unit,
    onViewPayments: (Int) -> Unit,
    reminderPreview: String,
    onReminderPreviewChange: (String) -> Unit,
    onNavigate: (ScreenRoute) -> Unit,
    onOpenPatient: (Patient, PatientDetailFocusSection) -> Unit,
    busyQuickAction: AppointmentQuickActionKind? = null,
    modifier: Modifier = Modifier,
) {
    val messenger = LocalAppMessenger.current
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var calendarMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    var selectedDoctorId by remember { mutableStateOf<Int?>(null) }
    var selectedStatus by remember { mutableStateOf<AppointmentStatus?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    /** Any day inside the week shown in «Semana» mode and in the Rango filter (mini-calendar updates this). */
    var weekAnchorDate by remember { mutableStateOf(LocalDate.now()) }
    var timelineVisibleCount by remember { mutableIntStateOf(40) }
    var viewMode by remember { mutableStateOf(AppointmentViewMode.ALL) }
    val timelineListState = rememberLazyListState()

    var rangeMenu by remember { mutableStateOf(false) }
    var doctorMenu by remember { mutableStateOf(false) }
    var statusMenu by remember { mutableStateOf(false) }
    var filtersSummaryMenu by remember { mutableStateOf(false) }

    /** One-shot: if DB citas are outside "this week", jump to the earliest appointment's week so lists are not blank. */
    var didAlignNavigationToAppointments by remember { mutableStateOf(false) }

    val patientMap = remember(patients) { patientsById(patients) }
    val weekRange =
        remember(weekAnchorDate) {
            weekRangeFor(weekAnchorDate)
        }

    val uiModels =
        remember(appointmentRows, patientMap) {
            appointmentRows.map { row ->
                val p = patientMap[row.patientId]
                row.toUiModel(p?.contactPhone)
            }
        }

    /** Full list after search + doctor + status. Do not pre-limit by calendar week here — that made seeded/off-week appointments disappear. */
    val searched =
        remember(uiModels, searchQuery) {
            val q = searchQuery.trim()
            if (q.isEmpty()) {
                uiModels
            } else {
                uiModels.filter { a ->
                    a.patientName.contains(q, ignoreCase = true) ||
                        a.doctorName.contains(q, ignoreCase = true) ||
                        a.treatmentName.contains(q, ignoreCase = true) ||
                        a.id.toString() == q ||
                        a.patientId.toString() == q ||
                        a.primaryDoctorId.toString() == q
                }
            }
        }

    val doctorFilterLabel =
        remember(selectedDoctorId, doctors) {
            selectedDoctorId?.let { id -> doctors.find { it.id == id }?.fullName } ?: "Todos los doctores"
        }

    val doctorFiltered =
        remember(searched, selectedDoctorId) {
            val id = selectedDoctorId
            if (id == null) searched else searched.filter { it.primaryDoctorId == id }
        }

    val statusFiltered =
        remember(doctorFiltered, selectedStatus) {
            val s = selectedStatus
            if (s == null) doctorFiltered else doctorFiltered.filter { it.status == s }
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
            weekAnchorDate = firstDate
            selectedDate = firstDate
            calendarMonth = YearMonth.from(firstDate)
        }
        didAlignNavigationToAppointments = true
    }

    LaunchedEffect(viewMode, selectedDate, weekAnchorDate, calendarMonth, selectedDoctorId, selectedStatus, searchQuery) {
        timelineVisibleCount = 40
    }

    val timelineSource =
        remember(statusFiltered, viewMode, selectedDate, weekRange, calendarMonth) {
            when (viewMode) {
                AppointmentViewMode.ALL ->
                    statusFiltered.sortedByDescending { it.scheduledAt }
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

    val timelineRowsForDisplay = timelineSource

    val visibleTimeline =
        remember(timelineRowsForDisplay, viewMode, timelineVisibleCount) {
            when (viewMode) {
                AppointmentViewMode.ALL -> timelineRowsForDisplay
                else -> timelineRowsForDisplay.take(timelineVisibleCount)
            }
        }

    val showTimelineDayHeaders = viewMode != AppointmentViewMode.DAY

    val timelineEntries =
        remember(visibleTimeline, showTimelineDayHeaders, viewMode) {
            buildTimelineEntriesWithDayHeaders(
                appointments = visibleTimeline,
                insertDayHeaders = showTimelineDayHeaders,
                newestFirst = viewMode == AppointmentViewMode.ALL,
            )
        }

    val selectedUi =
        remember(selectedAppointmentId, statusFiltered) {
            selectedAppointmentId?.let { id -> statusFiltered.find { it.id == id } }
        }

    val selectedAppointmentDate = selectedUi?.scheduledAt?.toLocalDate()

    LaunchedEffect(selectedAppointmentId, selectedUi) {
        selectedUi?.let { appt ->
            selectedDate = appt.scheduledAt.toLocalDate()
            calendarMonth = YearMonth.from(selectedDate)
            weekAnchorDate = selectedDate
        }
    }

    LaunchedEffect(selectedAppointmentId, timelineEntries) {
        val id = selectedAppointmentId ?: return@LaunchedEffect
        val index = timelineEntries.indexOfFirst { it is TimelineListEntry.Appointment && it.model.id == id }
        if (index >= 0) {
            timelineListState.animateScrollToItem(index)
        }
    }

    val dayStats =
        remember(uiModels, selectedDate) {
            val day = uiModels.filter { it.scheduledAt.toLocalDate() == selectedDate }
            DaySummaryStats(
                total = day.count { scheduledStatusCount(it.status) },
                inProgress = day.count { it.status == AppointmentStatus.IN_PROGRESS },
                completed = day.count { it.status == AppointmentStatus.COMPLETED },
                cancelled = day.count { it.status == AppointmentStatus.CANCELLED || it.status == AppointmentStatus.NO_SHOW },
            )
        }

    val activeFilterCount = (if (selectedDoctorId != null) 1 else 0) + (if (selectedStatus != null) 1 else 0)

    val filtrosActiveCount =
        remember(activeFilterCount, searchQuery, viewMode) {
            activeFilterCount +
                (if (searchQuery.isNotBlank()) 1 else 0) +
                (if (viewMode != AppointmentViewMode.ALL) 1 else 0)
        }

    val filtrosSummaryLabel =
        remember(searchQuery, selectedDoctorId, selectedStatus, viewMode) {
            val bits = mutableListOf<String>()
            if (searchQuery.isNotBlank()) bits.add("Búsqueda")
            if (selectedDoctorId != null) bits.add("Doctor")
            if (selectedStatus != null) bits.add("Estado")
            if (viewMode != AppointmentViewMode.ALL) {
                bits.add(
                    when (viewMode) {
                        AppointmentViewMode.DAY -> "Vista: día"
                        AppointmentViewMode.WEEK -> "Vista: semana"
                        AppointmentViewMode.MONTH -> "Vista: mes"
                        AppointmentViewMode.ALL -> ""
                    },
                )
            }
            if (bits.isEmpty()) "Sin filtros · vista completa"
            else bits.joinToString(" · ")
        }

    val daySummaryCaption =
        remember(selectedDate) {
            "Totales del día $selectedDate: cuenta todas las citas de esa fecha (sin filtros de búsqueda, doctor ni estado de la lista)."
        }

    val timelineTitle =
        remember(statusFiltered, viewMode, selectedDate, weekRange, calendarMonth) {
            when (viewMode) {
                AppointmentViewMode.ALL -> "Todas las citas (${statusFiltered.size})"
                AppointmentViewMode.DAY -> formatTimelineDayHeader(selectedDate)
                AppointmentViewMode.WEEK -> "Semana del ${formatRangeLabel(weekRange.first, weekRange.second)}"
                AppointmentViewMode.MONTH -> formatMonthTitle(calendarMonth)
            }
        }

    val emptyTimelineHint =
        remember(
            appointmentRows,
            timelineRowsForDisplay,
            searchQuery,
            searched,
            doctorFiltered,
            statusFiltered,
            viewMode,
            selectedDoctorId,
            selectedStatus,
        ) {
            if (timelineRowsForDisplay.isNotEmpty()) {
                ""
            } else {
                when {
                    appointmentRows.isEmpty() ->
                        "No hay citas. Use «Nueva cita» para programar la primera."
                    searchQuery.trim().isNotEmpty() && searched.isEmpty() ->
                        "Ningún resultado para «${searchQuery.trim()}». Pruebe otras palabras."
                    selectedDoctorId != null && doctorFiltered.isEmpty() ->
                        "Ninguna cita con el doctor seleccionado."
                    selectedStatus != null &&
                        doctorFiltered.isNotEmpty() &&
                        statusFiltered.isEmpty() ->
                        "Ninguna cita con el estado seleccionado."
                    else ->
                        when (viewMode) {
                            AppointmentViewMode.ALL ->
                                "No se encontraron citas para los filtros seleccionados."
                            else ->
                                "No hay citas en el día, semana o mes mostrado. Pruebe la vista «Todas» o cambie la fecha."
                        }
                }
            }
        }

    val citasResultsSummary =
        remember(appointmentRows, timelineRowsForDisplay, visibleTimeline, viewMode) {
            val inDb = appointmentRows.size
            val inView = timelineRowsForDisplay.size
            val shown = visibleTimeline.size
            when {
                inDb == 0 -> "Sin citas en base de datos"
                shown < inView -> "$inView en lista · mostrando $shown · $inDb en base"
                inView < inDb -> "$inView visibles · $inDb en base"
                else -> "$inDb citas"
            }
        }

    fun dispatchContextAction(id: Int, action: AppointmentQuickActionKind) {
        when (action) {
            AppointmentQuickActionKind.EDIT -> onEditAppointment(id)
            AppointmentQuickActionKind.RESCHEDULE -> onRescheduleAppointment(id)
            AppointmentQuickActionKind.CANCEL -> onCancelAppointment(id)
            AppointmentQuickActionKind.CONFIRM -> onConfirmAppointment(id)
            AppointmentQuickActionKind.START -> onStartAppointment(id)
            AppointmentQuickActionKind.COMPLETE -> onCompleteAppointment(id)
            AppointmentQuickActionKind.VIEW_PATIENT -> {
                val patient = patientMap[statusFiltered.find { it.id == id }?.patientId ?: return]
                if (patient != null) onOpenPatient(patient, PatientDetailFocusSection.OVERVIEW)
            }
            AppointmentQuickActionKind.CLINICAL_HISTORY -> {
                val patient = patientMap[statusFiltered.find { it.id == id }?.patientId ?: return]
                if (patient != null) onOpenPatient(patient, PatientDetailFocusSection.CLINICAL_HISTORY)
            }
            AppointmentQuickActionKind.REGISTER_PAYMENT -> onRegisterPaymentForAppointment(id)
            AppointmentQuickActionKind.REGISTER_TREATMENT -> onRegisterTreatmentForAppointment(id)
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppointmentPremiumPalette.background)
                .padding(bottom = AppSpacing.md)
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    val id = selectedAppointmentId ?: return@onPreviewKeyEvent false
                    when {
                        event.key == Key.Enter -> true
                        event.key == Key.E && event.isCtrlPressed -> {
                            onEditAppointment(id)
                            true
                        }
                        event.key == Key.R && event.isCtrlPressed -> {
                            onRescheduleAppointment(id)
                            true
                        }
                        event.key == Key.P && event.isCtrlPressed -> {
                            onRegisterPaymentForAppointment(id)
                            true
                        }
                        event.key == Key.Delete -> {
                            onCancelAppointment(id)
                            true
                        }
                        else -> false
                    }
                },
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Column(Modifier.widthIn(min = 200.dp, max = 360.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
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
                    Text(
                        text = citasResultsSummary,
                        style = AppTypography.Caption,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
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
                                weekAnchorDate = weekAnchorDate.minusWeeks(1)
                                selectedDate = weekAnchorDate
                                calendarMonth = YearMonth.from(weekAnchorDate)
                                rangeMenu = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Esta semana") },
                            onClick = {
                                val today = LocalDate.now()
                                weekAnchorDate = today
                                selectedDate = today
                                calendarMonth = YearMonth.from(today)
                                rangeMenu = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Próxima semana") },
                            onClick = {
                                weekAnchorDate = weekAnchorDate.plusWeeks(1)
                                selectedDate = weekAnchorDate
                                calendarMonth = YearMonth.from(weekAnchorDate)
                                rangeMenu = false
                            },
                        )
                    }
                    FilterDropdown(
                        label = "Doctor",
                        displayValue = doctorFilterLabel,
                        expanded = doctorMenu,
                        onExpandedChange = { doctorMenu = it },
                        modifier = Modifier.weight(1f, fill = false),
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos los doctores") },
                            onClick = {
                                selectedDoctorId = null
                                doctorMenu = false
                            },
                        )
                        doctors.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d.fullName) },
                                onClick = {
                                    selectedDoctorId = d.id
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
                            AppointmentStatus.RESCHEDULED,
                            AppointmentStatus.COMPLETED,
                            AppointmentStatus.CANCELLED,
                            AppointmentStatus.NO_SHOW,
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
                        displayValue = filtrosSummaryLabel,
                        expanded = filtersSummaryMenu,
                        onExpandedChange = { filtersSummaryMenu = it },
                        badgeCount = filtrosActiveCount.takeIf { it > 0 },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Quitar filtros y vista completa") },
                            onClick = {
                                searchQuery = ""
                                selectedDoctorId = null
                                selectedStatus = null
                                viewMode = AppointmentViewMode.ALL
                                filtersSummaryMenu = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Ir a hoy") },
                            onClick = {
                                val today = LocalDate.now()
                                selectedDate = today
                                weekAnchorDate = today
                                calendarMonth = YearMonth.from(today)
                                filtersSummaryMenu = false
                            },
                        )
                    }
                }
            }
            AppointmentViewModeToggle(
                mode = viewMode,
                onModeChange = { viewMode = it },
                modifier = Modifier.widthIn(min = 300.dp, max = 400.dp),
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
                                weekAnchorDate = it
                                calendarMonth = YearMonth.from(it)
                                if (viewMode == AppointmentViewMode.DAY || viewMode == AppointmentViewMode.WEEK) {
                                    viewMode = AppointmentViewMode.DAY
                                }
                                onSelectedAppointmentChange(null)
                            },
                            appointmentCountsByDate = appointmentCountsByDate,
                            highlightedDate = selectedAppointmentDate,
                        )
                        DaySummaryCard(
                            stats = dayStats,
                            caption = daySummaryCaption,
                            onViewAgenda = {
                                viewMode = AppointmentViewMode.DAY
                                selectedDate = selectedDate
                                weekAnchorDate = selectedDate
                            },
                        )
                    }
                    Row(
                        Modifier.weight(1f).fillMaxHeight(),
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
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
                                    AppointmentsTimelineList(
                                        entries = timelineEntries,
                                        selectedAppointmentId = selectedAppointmentId,
                                        onAppointmentClick = { onSelectedAppointmentChange(it) },
                                        onAppointmentDoubleClick = { onSelectedAppointmentChange(it) },
                                        onContextAction = { id, action -> dispatchContextAction(id, action) },
                                        modifier = Modifier.fillMaxSize(),
                                        useLazyColumn = true,
                                        listState = timelineListState,
                                    )
                                }
                            }
                            if (viewMode != AppointmentViewMode.ALL && visibleTimeline.size < timelineSource.size) {
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
                        AppointmentSelectionConnector(visible = selectedUi != null)
                        SelectedAppointmentDetailPanel(
                            selectedUi = selectedUi,
                            selectedAppointmentId = selectedAppointmentId,
                            appointmentDetail = appointmentDetail,
                            detailLoading = detailLoading,
                            reminderText = reminderPreview,
                            onReminderTextChange = onReminderPreviewChange,
                            onCloseSelection = { onSelectedAppointmentChange(null) },
                            onEditAppointment = onEditAppointment,
                            onRescheduleAppointment = onRescheduleAppointment,
                            onCancelAppointment = onCancelAppointment,
                            onConfirmAppointment = onConfirmAppointment,
                            onStartAppointment = onStartAppointment,
                            onCompleteAppointment = onCompleteAppointment,
                            onRegisterPaymentForAppointment = onRegisterPaymentForAppointment,
                            onRegisterTreatmentForAppointment = onRegisterTreatmentForAppointment,
                            onAddNote = onAddNote,
                            onEditNote = onEditNote,
                            onDeleteNote = onDeleteNote,
                            onCopyReminder = onCopyReminder,
                            onOpenWhatsAppReminder = onOpenWhatsAppReminder,
                            onViewPayments = onViewPayments,
                            onOpenPatient = onOpenPatient,
                            patientMap = patientMap,
                            busyQuickAction = busyQuickAction,
                            modifier = Modifier.width(360.dp),
                        )
                    }
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
                            weekAnchorDate = it
                            calendarMonth = YearMonth.from(it)
                            viewMode = AppointmentViewMode.DAY
                            onSelectedAppointmentChange(null)
                        },
                        appointmentCountsByDate = appointmentCountsByDate,
                        highlightedDate = selectedAppointmentDate,
                    )
                    DaySummaryCard(
                        stats = dayStats,
                        caption = daySummaryCaption,
                        onViewAgenda = { viewMode = AppointmentViewMode.DAY },
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
                        AppointmentsTimelineList(
                            entries = timelineEntries,
                            selectedAppointmentId = selectedAppointmentId,
                            onAppointmentClick = { onSelectedAppointmentChange(it) },
                            onAppointmentDoubleClick = { onSelectedAppointmentChange(it) },
                            onContextAction = { id, action -> dispatchContextAction(id, action) },
                            useLazyColumn = false,
                        )
                    }
                    if (viewMode != AppointmentViewMode.ALL && visibleTimeline.size < timelineSource.size) {
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
                    SelectedAppointmentDetailPanel(
                        selectedUi = selectedUi,
                        selectedAppointmentId = selectedAppointmentId,
                        appointmentDetail = appointmentDetail,
                        detailLoading = detailLoading,
                        reminderText = reminderPreview,
                        onReminderTextChange = onReminderPreviewChange,
                        onCloseSelection = { onSelectedAppointmentChange(null) },
                        onEditAppointment = onEditAppointment,
                        onRescheduleAppointment = onRescheduleAppointment,
                        onCancelAppointment = onCancelAppointment,
                        onConfirmAppointment = onConfirmAppointment,
                        onStartAppointment = onStartAppointment,
                        onCompleteAppointment = onCompleteAppointment,
                        onRegisterPaymentForAppointment = onRegisterPaymentForAppointment,
                        onRegisterTreatmentForAppointment = onRegisterTreatmentForAppointment,
                        onAddNote = onAddNote,
                        onEditNote = onEditNote,
                        onDeleteNote = onDeleteNote,
                        onCopyReminder = onCopyReminder,
                        onOpenWhatsAppReminder = onOpenWhatsAppReminder,
                        onViewPayments = onViewPayments,
                        onOpenPatient = onOpenPatient,
                        patientMap = patientMap,
                        busyQuickAction = busyQuickAction,
                        modifier = Modifier.fillMaxWidth().height(520.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedAppointmentDetailPanel(
    selectedUi: AppointmentUiModel?,
    selectedAppointmentId: Int?,
    appointmentDetail: AppointmentDetailSnapshot?,
    detailLoading: Boolean,
    reminderText: String,
    onReminderTextChange: (String) -> Unit,
    onCloseSelection: () -> Unit,
    onEditAppointment: (Int) -> Unit,
    onRescheduleAppointment: (Int) -> Unit,
    onCancelAppointment: (Int) -> Unit,
    onConfirmAppointment: (Int) -> Unit,
    onStartAppointment: (Int) -> Unit,
    onCompleteAppointment: (Int) -> Unit,
    onRegisterPaymentForAppointment: (Int) -> Unit,
    onRegisterTreatmentForAppointment: (Int) -> Unit,
    onAddNote: (Int, String) -> Unit,
    onEditNote: (Int, Int, String) -> Unit,
    onDeleteNote: (Int, Int) -> Unit,
    onCopyReminder: () -> Unit,
    onOpenWhatsAppReminder: () -> Unit,
    onViewPayments: (Int) -> Unit,
    onOpenPatient: (Patient, PatientDetailFocusSection) -> Unit,
    patientMap: Map<Int, Patient>,
    busyQuickAction: AppointmentQuickActionKind?,
    modifier: Modifier = Modifier,
) {
    val messenger = LocalAppMessenger.current

    fun requireAppointmentId(): Int? {
        val id = selectedAppointmentId
        if (id == null) {
            messenger.showError("Selecciona una cita primero.")
            return null
        }
        return id
    }

    AppointmentDetailPanel(
        appointment = selectedUi,
        detail = appointmentDetail,
        reminderPreview = reminderText,
        onReminderPreviewChange = onReminderTextChange,
        onCopyReminder = onCopyReminder,
        onOpenWhatsApp = onOpenWhatsAppReminder,
        onClose = onCloseSelection,
        onEditAppointment = {
            requireAppointmentId()?.let(onEditAppointment)
        },
        onReschedule = {
            requireAppointmentId()?.let(onRescheduleAppointment)
        },
        onCancelAppointment = {
            requireAppointmentId()?.let(onCancelAppointment)
        },
        onConfirmAppointment = {
            requireAppointmentId()?.let(onConfirmAppointment)
        },
        onStartAppointment = {
            requireAppointmentId()?.let(onStartAppointment)
        },
        onCompleteAppointment = {
            requireAppointmentId()?.let(onCompleteAppointment)
        },
        onViewPatient = {
            val ui = selectedUi
            if (ui == null) {
                messenger.showError("Selecciona una cita primero.")
                return@AppointmentDetailPanel
            }
            val patient = patientMap[ui.patientId]
            if (patient == null) {
                messenger.showError("Paciente no encontrado.")
            } else {
                onOpenPatient(patient, PatientDetailFocusSection.OVERVIEW)
            }
        },
        onClinicalHistory = {
            val ui = selectedUi
            if (ui == null) {
                messenger.showError("Selecciona una cita primero.")
                return@AppointmentDetailPanel
            }
            val patient = patientMap[ui.patientId]
            if (patient == null) {
                messenger.showError("Paciente no encontrado.")
            } else {
                onOpenPatient(patient, PatientDetailFocusSection.CLINICAL_HISTORY)
            }
        },
        onRegisterPayment = {
            requireAppointmentId()?.let(onRegisterPaymentForAppointment)
        },
        onRegisterTreatment = {
            requireAppointmentId()?.let(onRegisterTreatmentForAppointment)
        },
        onViewPayments = {
            requireAppointmentId()?.let(onViewPayments)
        },
        onAddNote = { body ->
            requireAppointmentId()?.let { onAddNote(it, body) }
        },
        onEditNote = { noteId, body ->
            requireAppointmentId()?.let { onEditNote(it, noteId, body) }
        },
        onDeleteNote = { noteId ->
            requireAppointmentId()?.let { onDeleteNote(it, noteId) }
        },
        modifier = modifier,
        linkedToSelection = selectedUi != null,
        busyAction = busyQuickAction,
        detailLoading = detailLoading,
    )
}
