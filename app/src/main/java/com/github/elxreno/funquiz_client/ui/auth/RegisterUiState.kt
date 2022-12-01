package com.github.elxreno.funquiz_client.ui.auth

data class RegisterUiState(
    var usernameError: Int? = null,
    var emailError: Int? = null,
    var passwordError: Int? = null,
    var passwordConfirmError: Int? = null,
    var isFieldsValid: Boolean = false,
    var isRegistering: Boolean = false,
    var isSuccess: Boolean = false,
)