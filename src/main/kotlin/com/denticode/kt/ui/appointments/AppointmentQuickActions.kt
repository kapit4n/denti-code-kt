package com.denticode.kt.ui.appointments

import com.denticode.kt.data.AppointmentStatus

enum class AppointmentQuickActionKind {
    EDIT,
    RESCHEDULE,
    CANCEL,
    VIEW_PATIENT,
    CLINICAL_HISTORY,
    REGISTER_PAYMENT,
    REGISTER_TREATMENT,
    CONFIRM,
    START,
    COMPLETE,
}

fun AppointmentStatus.isQuickActionEnabled(action: AppointmentQuickActionKind): Boolean =
    when (this) {
        AppointmentStatus.SCHEDULED,
        AppointmentStatus.RESCHEDULED,
        ->
            when (action) {
                AppointmentQuickActionKind.EDIT,
                AppointmentQuickActionKind.RESCHEDULE,
                AppointmentQuickActionKind.CANCEL,
                AppointmentQuickActionKind.CONFIRM,
                AppointmentQuickActionKind.REGISTER_TREATMENT,
                AppointmentQuickActionKind.VIEW_PATIENT,
                AppointmentQuickActionKind.CLINICAL_HISTORY,
                -> true
                else -> false
            }
        AppointmentStatus.CONFIRMED ->
            when (action) {
                AppointmentQuickActionKind.START,
                AppointmentQuickActionKind.CANCEL,
                AppointmentQuickActionKind.REGISTER_PAYMENT,
                AppointmentQuickActionKind.REGISTER_TREATMENT,
                AppointmentQuickActionKind.VIEW_PATIENT,
                AppointmentQuickActionKind.CLINICAL_HISTORY,
                -> true
                else -> false
            }
        AppointmentStatus.IN_PROGRESS ->
            when (action) {
                AppointmentQuickActionKind.COMPLETE,
                AppointmentQuickActionKind.REGISTER_PAYMENT,
                AppointmentQuickActionKind.REGISTER_TREATMENT,
                AppointmentQuickActionKind.VIEW_PATIENT,
                AppointmentQuickActionKind.CLINICAL_HISTORY,
                -> true
                else -> false
            }
        AppointmentStatus.COMPLETED ->
            when (action) {
                AppointmentQuickActionKind.VIEW_PATIENT,
                AppointmentQuickActionKind.CLINICAL_HISTORY,
                AppointmentQuickActionKind.REGISTER_PAYMENT,
                AppointmentQuickActionKind.REGISTER_TREATMENT,
                -> true
                else -> false
            }
        AppointmentStatus.CANCELLED,
        AppointmentStatus.NO_SHOW,
        ->
            when (action) {
                AppointmentQuickActionKind.VIEW_PATIENT,
                AppointmentQuickActionKind.CLINICAL_HISTORY,
                -> true
                else -> false
            }
    }

fun AppointmentStatus.isContextMenuActionEnabled(action: AppointmentQuickActionKind): Boolean =
    when (action) {
        AppointmentQuickActionKind.CONFIRM,
        AppointmentQuickActionKind.START,
        AppointmentQuickActionKind.COMPLETE,
        -> isQuickActionEnabled(action)
        else -> isQuickActionEnabled(action)
    }
