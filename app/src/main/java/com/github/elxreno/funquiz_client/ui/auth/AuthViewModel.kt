package com.github.elxreno.funquiz_client.ui.auth

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.elxreno.funquiz_client.R
import com.github.elxreno.funquiz_client.data.ResultWrapper
import com.github.elxreno.funquiz_client.data.dto.PasswordRequirementsDto
import com.github.elxreno.funquiz_client.data.entity.UserEntity
import com.github.elxreno.funquiz_client.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authUiState = MutableLiveData(AuthUiState())
    val authUiState: LiveData<AuthUiState>
        get() = _authUiState

    private val _registerUiState = MutableLiveData(RegisterUiState())
    val registerUiState: LiveData<RegisterUiState>
        get() = _registerUiState

    private val _passwordRequirements: MutableLiveData<PasswordRequirementsDto?> = MutableLiveData()
    val passwordRequirements: LiveData<PasswordRequirementsDto?>
        get() = _passwordRequirements

    val username = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val passwordConfirm = MutableLiveData<String>()

    private var fetchJob: Job? = null
    private var validateJob: Job? = null
    private var registerJob: Job? = null
    private var loginJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.Default) {
            fetchPasswordRequirements()
            if (userRepository.alreadyLoggedIn()) {
                loginViaPrevAuth(userRepository.getLocalUser())
            }
        }

        username.observeForever { validateRegisterFields() }
        email.observeForever {
            validateRegisterFields()
            validateLoginFields()
        }
        password.observeForever {
            validateRegisterFields()
            validateLoginFields()
        }
        passwordConfirm.observeForever { validateRegisterFields() }
    }

    private suspend fun fetchPasswordRequirements() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _authUiState.value = AuthUiState(isFetchingPasswordRequirements = true)
            when (val passwordRequirements = userRepository.fetchPasswordRequirements()) {
                is ResultWrapper.Success -> {
                    _authUiState.value = AuthUiState()
                    _passwordRequirements.value = passwordRequirements.value
                }

                is ResultWrapper.NetworkError -> {
                    _authUiState.value = AuthUiState(isNetworkError = true)
                }

                is ResultWrapper.GenericError -> {
                    _authUiState.value = AuthUiState(isServerError = true)
                }
            }
        }
    }

    private fun validateUsername(username: String?): Int? {
        return if (username.isNullOrEmpty()) {
            R.string.username_can_t_be_empty
        } else if (username.length < 4) {
            R.string.username_must_be_at_least_4_characters
        } else {
            null
        }
    }

    private fun validateEmail(email: String?): Int? {
        return if (email.isNullOrEmpty()) {
            R.string.enter_email
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            R.string.enter_valid_email
        } else {
            null
        }
    }

    private fun validatePassword(
        password: String?,
        passwordRequirements: PasswordRequirementsDto
    ): Int? {
        return if (password.isNullOrEmpty()) {
            R.string.enter_password
        } else if (passwordRequirements.requireDigit && !password.any { it.isDigit() }) {
            R.string.password_must_contain_at_least_one_digit
        } else if (passwordRequirements.requireLowercase && !password.any { it.isLowerCase() }) {
            R.string.password_must_contain_at_least_one_lowercase_letter
        } else if (passwordRequirements.requireUppercase && !password.any { it.isUpperCase() }) {
            R.string.password_must_contain_at_least_one_uppercase_letter
        } else if (passwordRequirements.requireNonAlphanumeric && !password.any { !it.isLetterOrDigit() }) {
            R.string.password_must_contain_at_least_one_special_character
        } else if (password.length < passwordRequirements.requiredLength) {
            R.string.password_must_be_at_least_s_characters_long
        } else
            null
    }

    private fun validateLoginFields() {
        val emailError = validateEmail(email.value)

        _authUiState.value = AuthUiState(
            emailError = emailError,
            isFieldsValid = emailError == null && password.value?.isNotEmpty() == true
        )
    }

    private fun validateRegisterFields() {
        validateJob?.cancel()
        validateJob = viewModelScope.launch {
            val username = username.value
            val email = email.value
            val password = password.value
            val passwordConfirm = passwordConfirm.value
            val passwordRequirements = passwordRequirements.value

            val state = RegisterUiState()

            if (passwordRequirements == null)
                return@launch

            state.usernameError = validateUsername(username)
            state.emailError = validateEmail(email)
            state.passwordError = validatePassword(password, passwordRequirements)

            if (password != passwordConfirm) {
                state.passwordConfirmError = R.string.passwords_do_not_match
            }

            state.isFieldsValid = state.usernameError == null
                    && state.emailError == null
                    && state.passwordError == null
                    && state.passwordConfirmError == null
            _registerUiState.value = state
        }
    }

    fun register() {
        registerJob?.cancel()
        registerJob = viewModelScope.launch {
            _registerUiState.value = RegisterUiState(isRegistering = true)
            when (val result = userRepository.register(
                username.value!!,
                email.value!!,
                password.value!!,
                passwordConfirm.value!!
            )) {
                is ResultWrapper.Success -> {
                    _authUiState.value = AuthUiState(isRegistrationSuccess = true)
                    _registerUiState.value = RegisterUiState(isSuccess = true)
                }

                is ResultWrapper.NetworkError -> {
                    _authUiState.value = AuthUiState(isNetworkError = true)
                }

                is ResultWrapper.GenericError -> {
                    when (result.code) {
                        204 -> {
                            _registerUiState.value = RegisterUiState(isSuccess = true)
                        }

                        else -> {
                            _authUiState.value =
                                AuthUiState(
                                    isServerError = true,
                                    serverErrorResponse = result.error
                                )
                        }
                    }
                }
            }
        }
    }

    private suspend fun loginViaCredentials(): ResultWrapper<Unit> {
        return userRepository.login(email.value!!, password.value!!)
    }

    private suspend fun loginViaPrevAuth(localUser: UserEntity?) {
        loginJob?.cancel()
        loginJob = viewModelScope.launch {
            _authUiState.value = AuthUiState(isLoggingIn = true)
            email.value = localUser?.email
            password.value = localUser?.tokens?.accessToken
            when (val result = userRepository.loginWithPrevAuth(localUser)) {
                is ResultWrapper.Success -> {
                    _authUiState.value = AuthUiState(isLoginSuccess = true)
                }

                is ResultWrapper.NetworkError -> {
                    _authUiState.value = AuthUiState(isNetworkError = true)
                }

                is ResultWrapper.GenericError -> {
                    when (result.code) {
                        401 -> {
                            _authUiState.value = AuthUiState(isInvalidCredentials = true)
                        }

                        else -> {
                            _authUiState.value =
                                AuthUiState(
                                    isServerError = true,
                                    serverErrorResponse = result.error
                                )
                        }
                    }
                }
            }
        }
    }

    fun login() {
        loginJob?.cancel()
        loginJob = viewModelScope.launch {
            _authUiState.value = AuthUiState(isLoggingIn = true)
            when (val result = loginViaCredentials()) {
                is ResultWrapper.Success -> {
                    _authUiState.value = AuthUiState(isLoginSuccess = true)
                }

                is ResultWrapper.NetworkError -> {
                    _authUiState.value = AuthUiState(isNetworkError = true)
                }

                is ResultWrapper.GenericError -> {
                    when (result.code) {
                        401 -> {
                            _authUiState.value = AuthUiState(isInvalidCredentials = true)
                        }

                        else -> {
                            _authUiState.value =
                                AuthUiState(
                                    isServerError = true,
                                    serverErrorResponse = result.error
                                )
                        }
                    }
                }
            }
        }
    }

    fun loginAsGuest() {
        loginJob?.cancel()
        loginJob = viewModelScope.launch {
            _authUiState.value = AuthUiState(isLoggingIn = true)
            userRepository.loginAsGuest()
            _authUiState.value = AuthUiState(isLoginSuccess = true)
        }
    }

    fun onLoginSuccess() {
        _authUiState.value = AuthUiState()
    }
}