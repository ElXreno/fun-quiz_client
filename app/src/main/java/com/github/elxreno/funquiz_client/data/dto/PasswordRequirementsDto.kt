package com.github.elxreno.funquiz_client.data.dto

data class PasswordRequirementsDto(
    val requiredLength: Int,
    val requiredUniqueChars: Int,
    val requireNonAlphanumeric: Boolean,
    val requireLowercase: Boolean,
    val requireUppercase: Boolean,
    val requireDigit: Boolean
)
