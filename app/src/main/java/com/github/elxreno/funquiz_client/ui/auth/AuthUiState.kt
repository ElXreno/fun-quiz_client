package com.github.elxreno.funquiz_client.ui.auth

import com.github.elxreno.funquiz_client.data.error.ErrorResponse

data class AuthUiState(
    val isNetworkError: Boolean = false,
    val isServerError: Boolean = false,
    val emailError: Int? = null,
    val isFieldsValid: Boolean = false,
    val serverErrorResponse: ErrorResponse? = null,
    val isInvalidCredentials: Boolean = false,
    val isFetchingPasswordRequirements: Boolean = false,
    val isRegistrationSuccess: Boolean = false,
    val isLoggingIn: Boolean = false,
    val isLoginSuccess: Boolean = false,
)