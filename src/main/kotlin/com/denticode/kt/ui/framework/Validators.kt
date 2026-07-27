package com.denticode.kt.ui.framework

typealias ValidationResult = String?

data class FieldError(val field: String, val message: String)

sealed class ValidationError {
    data class Single(val message: String) : ValidationError()
    data class Multiple(val errors: List<FieldError>) : ValidationError()
}

object Validators {
    fun required(value: String, fieldName: String = "Este campo"): ValidationResult {
        if (value.trim().isEmpty()) return "$fieldName es obligatorio."
        return null
    }

    fun email(value: String): ValidationResult {
        if (value.trim().isEmpty()) return null
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        if (!emailRegex.matches(value.trim())) return "Correo electrónico no válido."
        return null
    }

    fun phone(value: String): ValidationResult {
        if (value.trim().isEmpty()) return null
        val digits = value.filter { it.isDigit() }
        if (digits.length < 7) return "El teléfono debe tener al menos 7 dígitos."
        if (digits.length > 15) return "El teléfono no puede tener más de 15 dígitos."
        return null
    }

    fun length(value: String, max: Int, fieldName: String = "Este campo"): ValidationResult {
        if (value.length > max) return "$fieldName no puede tener más de $max caracteres."
        return null
    }

    fun minLength(value: String, min: Int, fieldName: String = "Este campo"): ValidationResult {
        if (value.trim().length < min) return "$fieldName debe tener al menos $min caracteres."
        return null
    }

    fun numeric(value: String, fieldName: String = "Este campo"): ValidationResult {
        if (value.trim().isEmpty()) return null
        if (value.toDoubleOrNull() == null) return "$fieldName debe ser un número válido."
        return null
    }

    fun positiveNumber(value: String, fieldName: String = "Este campo"): ValidationResult {
        if (value.trim().isEmpty()) return null
        val num = value.toDoubleOrNull()
        if (num == null) return "$fieldName debe ser un número válido."
        if (num < 0) return "$fieldName no puede ser negativo."
        return null
    }

    fun licenseNumber(value: String): ValidationResult {
        if (value.trim().isEmpty()) return null
        if (value.trim().length < 3) return "El número de colegiado es muy corto."
        return null
    }

    fun futureDate(value: String): ValidationResult {
        if (value.trim().isEmpty()) return null
        return null
    }

    fun url(value: String): ValidationResult {
        if (value.trim().isEmpty()) return null
        val urlRegex =
            "^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$".toRegex()
        if (!urlRegex.matches(value.trim())) return "URL no válida."
        return null
    }
}
