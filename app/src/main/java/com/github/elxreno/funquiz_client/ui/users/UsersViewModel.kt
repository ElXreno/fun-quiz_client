package com.github.elxreno.funquiz_client.ui.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.elxreno.funquiz_client.data.ResultWrapper
import com.github.elxreno.funquiz_client.data.dto.UserInfoDto
import com.github.elxreno.funquiz_client.data.repository.UserRepository
import com.github.elxreno.funquiz_client.di.Annotations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    @Annotations.Auth private val userRepository: UserRepository
) : ViewModel(), UserListener {

    private val _uiState = MutableLiveData<UsersUiState>()
    val uiState: LiveData<UsersUiState>
        get() = _uiState

    val users = userRepository.users

    private var loadJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadUsers()
        }
    }

    private suspend fun loadUsers() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = UsersUiState(isLoading = true)
            when (userRepository.fetchUsers()) {
                is ResultWrapper.Success -> {
                    _uiState.value = UsersUiState(isLoaded = true)
                }

                else -> {
                    _uiState.value = UsersUiState(error = "Unknown error")
                }
            }
        }
    }

    override fun onUserClicked(user: UserInfoDto) {
        println("User clicked: $user")
    }
}