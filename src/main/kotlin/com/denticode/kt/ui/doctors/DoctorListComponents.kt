@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.doctors

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.DoctorDirectoryKpis
import com.denticode.kt.data.DoctorListStatus
import com.denticode.kt.ui.appointments.PatientAvatar
import com.denticode.kt.ui.components.inputs.AppSearchField
import com.denticode.kt.ui.patients.PatientFilterDropdown
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

@Composable
fun DoctorsPageHeader(
    onNewDoctorClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Doctores",
                style = AppTypography.PageTitle,
                fontWeight = FontWeight.Bold,
                color = PatientsPremiumPalette.textPrimary,
            )
            Text(
                "Directorio del equipo médico de la clínica",
                style = AppTypography.Body,
                color = PatientsPremiumPalette.textSecondary,
            )
        }
        FilledTonalButton(
            onClick = onNewDoctorClick,
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier.heightIn(min = 44.dp),
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
            Text("Nuevo doctor", modifier = Modifier.padding(start = 6.dp))
        }
    }
}

@Composable
private fun DoctorKpiCard(
    caption: String,
    value: String,
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val scale by animateFloatAsState(if (hovered) 1.02f else 1f, tween(180), label = "docKpi")
    ElevatedCard(
        modifier =
            modifier
                .height(96.dp)
                .scale(scale)
                .hoverable(interaction),
        shape = AppShapes.medium,
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    value,
                    style = AppTypography.MetricMedium,
                    fontWeight = FontWeight.Bold,
                    color = PatientsPremiumPalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(caption, style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary)
            }
        }
    }
}

@Composable
fun DoctorsStatsRow(
    kpis: DoctorDirectoryKpis,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DoctorKpiCard(
            caption = "Equipo clínico",
            value = kpis.totalDoctors.toString(),
            icon = Icons.Outlined.Group,
            iconBackground = PatientsPremiumPalette.primary.copy(alpha = 0.12f),
            iconTint = PatientsPremiumPalette.primary,
            modifier = Modifier.weight(1f),
        )
        DoctorKpiCard(
            caption = "Disponibles",
            value = kpis.activeDoctors.toString(),
            icon = Icons.Outlined.CheckCircle,
            iconBackground = PatientsPremiumPalette.success.copy(alpha = 0.12f),
            iconTint = PatientsPremiumPalette.success,
            modifier = Modifier.weight(1f),
        )
        DoctorKpiCard(
            caption = "Programadas",
            value = kpis.todayAppointments.toString(),
            icon = Icons.Default.CalendarMonth,
            iconBackground = PatientsPremiumPalette.warning.copy(alpha = 0.14f),
            iconTint = PatientsPremiumPalette.warning,
            modifier = Modifier.weight(1f),
        )
        DoctorKpiCard(
            caption = "Áreas médicas",
            value = kpis.specialtyCount.toString(),
            icon = Icons.Default.MedicalServices,
            iconBackground = PatientsPremiumPalette.info.copy(alpha = 0.12f),
            iconTint = PatientsPremiumPalette.info,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun DoctorSearchFilters(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    specialtyLabel: String,
    specialtyOptions: List<String>,
    onSpecialtySelect: (String) -> Unit,
    statusLabel: String,
    statusOptions: List<String>,
    onStatusSelect: (String) -> Unit,
    sortLabel: String,
    sortOptions: List<String>,
    onSortSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth().heightIn(min = 64.dp),
        shape = AppShapes.medium,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppSearchField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.weight(1.4f),
                placeholder = "Buscar doctor, especialidad o correo…",
                keyboardShortcutHint = null,
            )
            PatientFilterDropdown(
                label = "Especialidad",
                displayValue = specialtyLabel,
                options = specialtyOptions,
                onSelect = onSpecialtySelect,
                modifier = Modifier.weight(0.85f),
            )
            PatientFilterDropdown(
                label = "Estado",
                displayValue = statusLabel,
                options = statusOptions,
                onSelect = onStatusSelect,
                modifier = Modifier.weight(0.75f),
            )
            PatientFilterDropdown(
                label = "Ordenar",
                displayValue = sortLabel,
                options = sortOptions,
                onSelect = onSortSelect,
                modifier = Modifier.weight(0.85f),
            )
        }
    }
}

@Composable
fun DoctorStatusChip(
    status: DoctorListStatus,
    modifier: Modifier = Modifier,
) {
    val colors =
        when (status) {
            DoctorListStatus.ACTIVE ->
                AssistChipDefaults.assistChipColors(
                    containerColor = PatientsPremiumPalette.success.copy(alpha = 0.14f),
                    labelColor = PatientsPremiumPalette.success,
                )
            DoctorListStatus.INACTIVE ->
                AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    labelColor = PatientsPremiumPalette.textSecondary,
                )
            DoctorListStatus.VACATION ->
                AssistChipDefaults.assistChipColors(
                    containerColor = PatientsPremiumPalette.warning.copy(alpha = 0.16f),
                    labelColor = Color(0xFFB45309),
                )
        }
    AssistChip(
        onClick = {},
        label = {
            Text(status.labelEs, style = AppTypography.Caption, fontWeight = FontWeight.SemiBold)
        },
        modifier = modifier,
        enabled = false,
        colors = colors,
        border = null,
    )
}

@Composable
fun DoctorActionsMenu(
    onViewProfile: () -> Unit,
    onEdit: () -> Unit,
    onViewSchedule: () -> Unit,
    onDeactivate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, "Acciones", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Ver perfil") },
                onClick = {
                    expanded = false
                    onViewProfile()
                },
            )
            DropdownMenuItem(
                text = { Text("Editar") },
                onClick = {
                    expanded = false
                    onEdit()
                },
            )
            DropdownMenuItem(
                text = { Text("Ver agenda") },
                onClick = {
                    expanded = false
                    onViewSchedule()
                },
            )
            DropdownMenuItem(
                text = { Text("Desactivar") },
                onClick = {
                    expanded = false
                    onDeactivate()
                },
            )
        }
    }
}

@Composable
private fun DoctorsTableHeader(modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(PatientsPremiumPalette.background)
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DoctorHeaderCell("DOCTOR", Modifier.weight(1.35f))
        DoctorHeaderCell("CONTACTO", Modifier.weight(1.15f))
        DoctorHeaderCell("HOY", Modifier.width(96.dp))
        DoctorHeaderCell("ESTADO", Modifier.width(108.dp))
        DoctorHeaderCell("EXPERIENCIA", Modifier.width(96.dp))
        DoctorHeaderCell("ACCIÓN", Modifier.width(56.dp))
    }
}

@Composable
private fun DoctorHeaderCell(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text,
        modifier = modifier,
        style = AppTypography.Caption,
        fontWeight = FontWeight.SemiBold,
        color = PatientsPremiumPalette.textSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun DoctorRow(
    doctor: DoctorUiModel,
    selected: Boolean,
    onClick: () -> Unit,
    onViewProfile: () -> Unit,
    onEdit: () -> Unit,
    onViewSchedule: () -> Unit,
    onDeactivate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember(doctor.id) { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val bg by animateColorAsState(
        when {
            selected -> PatientsPremiumPalette.primary.copy(alpha = 0.08f)
            hovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            else -> Color.Transparent
        },
        tween(140),
        label = "docRowBg",
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp)
                    .background(bg)
                    .clickable(interactionSource = interaction, indication = null, onClick = onClick)
                    .hoverable(interaction)
                    .padding(horizontal = AppSpacing.lg, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier.weight(1.35f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                PatientAvatar(doctor.fullName, size = 40.dp)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        doctor.fullName,
                        style = AppTypography.Body,
                        fontWeight = FontWeight.SemiBold,
                        color = PatientsPremiumPalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        doctor.specialty,
                        style = AppTypography.BodySmall,
                        color = PatientsPremiumPalette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Column(
                Modifier.weight(1.15f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    doctor.email,
                    style = AppTypography.BodySmall,
                    color = PatientsPremiumPalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    doctor.phone,
                    style = AppTypography.Caption,
                    color = PatientsPremiumPalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(Modifier.width(96.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PatientsPremiumPalette.primary.copy(alpha = 0.12f),
                    border =
                        androidx.compose.foundation.BorderStroke(
                            1.dp,
                            PatientsPremiumPalette.primary.copy(alpha = 0.22f),
                        ),
                ) {
                    Text(
                        "${doctor.todayAppointmentsCount} citas",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = AppTypography.Caption,
                        fontWeight = FontWeight.SemiBold,
                        color = PatientsPremiumPalette.primary,
                    )
                }
            }
            Box(Modifier.width(108.dp)) {
                DoctorStatusChip(status = doctor.status)
            }
            Text(
                doctor.experienceLabel,
                Modifier.width(96.dp),
                style = AppTypography.BodySmall,
                fontWeight = FontWeight.Medium,
                color = PatientsPremiumPalette.textPrimary,
            )
            Box(Modifier.width(56.dp), contentAlignment = Alignment.Center) {
                DoctorActionsMenu(
                    onViewProfile = onViewProfile,
                    onEdit = onEdit,
                    onViewSchedule = onViewSchedule,
                    onDeactivate = onDeactivate,
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    }
}

@Composable
fun DoctorsTable(
    doctors: List<DoctorUiModel>,
    selectedDoctorId: Int?,
    onSelectDoctor: (DoctorUiModel) -> Unit,
    onViewProfile: (DoctorUiModel) -> Unit,
    onEdit: (DoctorUiModel) -> Unit,
    onViewSchedule: (DoctorUiModel) -> Unit,
    onDeactivate: (DoctorUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth().fillMaxHeight(),
        shape = AppShapes.medium,
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "header") { DoctorsTableHeader() }
            items(doctors, key = { it.id }) { doctor ->
                DoctorRow(
                    doctor = doctor,
                    selected = doctor.id == selectedDoctorId,
                    onClick = { onSelectDoctor(doctor) },
                    onViewProfile = { onViewProfile(doctor) },
                    onEdit = { onEdit(doctor) },
                    onViewSchedule = { onViewSchedule(doctor) },
                    onDeactivate = { onDeactivate(doctor) },
                )
            }
        }
    }
}

@Composable
fun EmptyDoctorsState(
    hasFilters: Boolean,
    onNewDoctorClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Icon(
            Icons.Default.MedicalServices,
            null,
            Modifier.size(52.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
        )
        Text(
            if (hasFilters) "Ningún doctor coincide con los filtros" else "No hay doctores registrados",
            style = AppTypography.Body,
            fontWeight = FontWeight.Medium,
            color = PatientsPremiumPalette.textPrimary,
        )
        Text(
            if (hasFilters) {
                "Pruebe a ampliar la búsqueda o quitar filtros."
            } else {
                "Agrega el primer profesional para comenzar."
            },
            style = AppTypography.BodySmall,
            color = PatientsPremiumPalette.textSecondary,
        )
        if (!hasFilters) {
            FilledTonalButton(
                onClick = onNewDoctorClick,
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier.padding(top = AppSpacing.sm),
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Text("+ Nuevo doctor", modifier = Modifier.padding(start = 6.dp))
            }
        }
    }
}
