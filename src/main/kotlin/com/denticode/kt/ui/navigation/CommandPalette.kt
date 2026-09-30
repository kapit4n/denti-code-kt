package com.denticode.kt.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ViewSidebar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.Doctor
import com.denticode.kt.data.Patient
import com.denticode.kt.ui.app.LocalAppMessenger
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Resultado del buscador global: comando de navegación o entidad (paciente / doctor). */
private data class CommandItem(
    val key: String,
    val group: String,
    val label: String,
    val subtitle: String,
    val leadingIcon: ImageVector,
    val onInvoke: () -> Unit,
)

/** Tope de resultados por grupo: la paleta es un atajo, no un listado de la base de datos. */
private const val MaxEntityResults = 6

@Composable
fun CommandPaletteDialog(
    navigationState: NavigationState,
    repo: DentiRepository,
    onOpenPatient: (Patient) -> Unit,
    onOpenDoctor: (Doctor) -> Unit,
    onDismiss: () -> Unit,
    initialQuery: String = "",
) {
    val messenger = LocalAppMessenger.current
    var query by remember { mutableStateOf(initialQuery) }
    var patients by remember { mutableStateOf<List<Patient>>(emptyList()) }
    var doctors by remember { mutableStateOf<List<Doctor>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableIntStateOf(0) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    val trimmed = query.trim()
    LaunchedEffect(trimmed) {
        if (trimmed.isEmpty()) {
            patients = emptyList()
            doctors = emptyList()
            searching = false
            return@LaunchedEffect
        }
        searching = true
        val search =
            withContext(Dispatchers.IO) {
                runCatching { repo.searchPatients(trimmed) to repo.searchDoctors(trimmed) }
            }
        val found = search.getOrNull()
        if (found == null) {
            patients = emptyList()
            doctors = emptyList()
            messenger.showError("No se pudo completar la búsqueda.")
        } else {
            patients = found.first.take(MaxEntityResults)
            doctors = found.second.take(MaxEntityResults)
        }
        searching = false
    }
    val routeIcons = remember { navigationItems().associate { it.route.id to it.icon } }
    val commands =
        remember(navigationState.useDarkTheme, navigationState.sidebarCollapsed, trimmed, routeIcons) {
            buildList {
                ScreenRoute.mainMenu.forEach { route ->
                    add(
                        CommandItem(
                            key = "cmd-${route.id}",
                            group = "Comandos",
                            label = route.title,
                            subtitle = "Ir a · ${route.subtitle ?: route.title}",
                            leadingIcon = routeIcons[route.id] ?: Icons.Default.Search,
                            onInvoke = {
                                navigationState.navigateTo(route)
                                onDismiss()
                            },
                        ),
                    )
                }
                add(
                    CommandItem(
                        key = "cmd-theme",
                        group = "Comandos",
                        label = if (navigationState.useDarkTheme) "Modo claro" else "Modo oscuro",
                        subtitle = "Apariencia",
                        leadingIcon = if (navigationState.useDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        onInvoke = {
                            navigationState.toggleDarkTheme()
                            onDismiss()
                        },
                    ),
                )
                add(
                    CommandItem(
                        key = "cmd-sidebar",
                        group = "Comandos",
                        label = if (navigationState.sidebarCollapsed) "Expandir barra lateral" else "Contraer barra lateral",
                        subtitle = "Navegación",
                        leadingIcon = Icons.Default.ViewSidebar,
                        onInvoke = {
                            navigationState.toggleSidebarCollapsed()
                            onDismiss()
                        },
                    ),
                )
            }.filter { trimmed.isEmpty() || it.label.lowercase().contains(trimmed.lowercase()) || it.subtitle.lowercase().contains(trimmed.lowercase()) }
        }
    val results =
        remember(commands, patients, doctors) {
            buildList {
                addAll(commands)
                patients.forEach { patient ->
                    add(
                        CommandItem(
                            key = "patient-${patient.id}",
                            group = "Pacientes",
                            label = patient.fullName,
                            subtitle =
                                listOfNotNull(
                                    patient.documentNumber?.let { "CI $it" },
                                    patient.contactPhone,
                                ).joinToString(" · ").ifEmpty { "Paciente" },
                            leadingIcon = Icons.Default.Person,
                            onInvoke = {
                                onOpenPatient(patient)
                                onDismiss()
                            },
                        ),
                    )
                }
                doctors.forEach { doctor ->
                    add(
                        CommandItem(
                            key = "doctor-${doctor.id}",
                            group = "Doctores",
                            label = doctor.fullName,
                            subtitle =
                                listOfNotNull(
                                    doctor.specialization,
                                    doctor.contactPhone ?: doctor.email,
                                ).joinToString(" · ").ifEmpty { "Doctor" },
                            leadingIcon = Icons.Default.Badge,
                            onInvoke = {
                                onOpenDoctor(doctor)
                                onDismiss()
                            },
                        ),
                    )
                }
            }
        }
    LaunchedEffect(results.size, trimmed) {
        selectedIndex = 0
    }
    val invokeSelected = { results.getOrNull(selectedIndex)?.onInvoke() }
    fun handleKey(event: KeyEvent): Boolean {
        if (event.type != KeyEventType.KeyDown) return false
        return when (event.key) {
            Key.Escape -> {
                onDismiss()
                true
            }
            Key.DirectionDown -> {
                selectedIndex = (selectedIndex + 1).coerceAtMost((results.size - 1).coerceAtLeast(0))
                true
            }
            Key.DirectionUp -> {
                selectedIndex = (selectedIndex - 1).coerceAtLeast(0)
                true
            }
            Key.Enter -> {
                invokeSelected()
                true
            }
            else -> false
        }
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
            ),
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth(0.55f)
                    .padding(AppSpacing.xl)
                    .onPreviewKeyEvent { event -> handleKey(event) },
            shape = AppShapes.large,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                Text("Buscar en la aplicación", style = AppTypography.SectionTitle)
                Text(
                    "Ctrl+K · Pacientes, doctores y navegación",
                    style = AppTypography.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    placeholder = "Buscar paciente, doctor o comando…",
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        if (searching) {
                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else if (query.isNotEmpty()) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = "Limpiar búsqueda",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier =
                                    Modifier
                                        .size(18.dp)
                                        .clickable {
                                            query = ""
                                            patients = emptyList()
                                            doctors = emptyList()
                                        },
                            )
                        }
                    },
                    shape = AppShapes.small,
                    keyboardActions = KeyboardActions(onDone = { invokeSelected() }),
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    results
                        .groupBy { it.group }
                        .forEach { (group, groupItems) ->
                            item(key = "header-$group") {
                                Text(
                                    group,
                                    style = AppTypography.Caption,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                                )
                            }
                            items(groupItems, key = { it.key }) { item ->
                                CommandItemRow(
                                    item = item,
                                    selected = results.indexOfFirst { it.key == item.key } == selectedIndex,
                                    onClick = item.onInvoke,
                                )
                            }
                        }
                    if (results.isEmpty()) {
                        item(key = "empty") {
                            Text(
                                if (trimmed.isEmpty()) "Escriba para buscar." else "Sin resultados para “$trimmed”.",
                                style = AppTypography.BodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(AppSpacing.md),
                            )
                        }
                    }
                }
                Text(
                    "↑ ↓ navegar · Enter abrir · Esc cerrar",
                    style = AppTypography.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CommandItemRow(
    item: CommandItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(AppShapes.small)
                .clickable(onClick = onClick),
        shape = AppShapes.small,
        color =
            if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Box(
                Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    item.leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    item.label,
                    style = AppTypography.Body,
                    fontWeight = if (selected) FontWeight.SemiBold else null,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    item.subtitle,
                    style = AppTypography.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (selected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
