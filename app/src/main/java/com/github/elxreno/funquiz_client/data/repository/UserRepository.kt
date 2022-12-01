package com.github.elxreno.funquiz_client.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.auth0.android.jwt.JWT
import com.github.elxreno.funquiz_client.data.ResultWrapper
import com.github.elxreno.funquiz_client.data.database.AppDatabase
import com.github.elxreno.funquiz_client.data.dto.PasswordRequirementsDto
import com.github.elxreno.funquiz_client.data.dto.TokensDto
import com.github.elxreno.funquiz_client.data.dto.UserInfoDto
import com.github.elxreno.funquiz_client.data.dto.UserLoginDto
import com.github.elxreno.funquiz_client.data.dto.UserRegisterDto
import com.github.elxreno.funquiz_client.data.entity.UserEntity
import com.github.elxreno.funquiz_client.data.entity.UserRoles
import com.github.elxreno.funquiz_client.data.safeApiCall
import com.github.elxreno.funquiz_client.data.service.UserService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class UserRepository(
    private val appDatabase: AppDatabase,
    private val userService: UserService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val currentUser: LiveData<UserEntity>
        get() = appDatabase.userDao().getUser()

    private val _userRoles: MutableLiveData<List<String>> = MutableLiveData(listOf())
    val userRoles: LiveData<List<String>>
        get() = _userRoles

    private val _users = MutableLiveData<List<UserInfoDto>>()
    val users: LiveData<List<UserInfoDto>>
        get() = _users

    init {
        currentUser.observeForever { userEntity ->
            if (userEntity != null) {
                val jwtToken = JWT(userEntity.tokens.accessToken)
                val claims = jwtToken.claims
                val rolesEntry = claims.entries.firstOrNull { entry ->
                    entry.key == "http://schemas.microsoft.com/ws/2008/06/identity/claims/role"
                }

                if (rolesEntry != null) {
                    var roles = rolesEntry.value.asArray(String::class.java).toList()
                    if (roles.isEmpty() && rolesEntry.value.asString() != null) {
                        roles = rolesEntry.value.asString()!!.split(",")
                    }
                    _userRoles.postValue(roles)
                }
            }
        }
    }

    suspend fun login(email: String, password: String): ResultWrapper<Unit> {
        return safeApiCall(dispatcher) {
            val response = userService.login(UserLoginDto(email, password))
            appDatabase.userDao().resetCurrent()
            appDatabase.userDao().insertUser(response.toEntity(true))
        }
    }

    suspend fun alreadyLoggedIn(): Boolean {
        val user = getLocalUser()
        return user != null
    }

    suspend fun loginWithPrevAuth(user: UserEntity?): ResultWrapper<UserEntity> {
        if (user != null) {
            return refreshToken(user)
        }
        return ResultWrapper.GenericError()
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        passwordConfirm: String
    ): ResultWrapper<Unit> {
        return safeApiCall(dispatcher) {
            userService.register(UserRegisterDto(username, email, password, passwordConfirm))
        }
    }

    suspend fun getLocalUser(): UserEntity? {
        return appDatabase.userDao().getUserAsync()
    }

    suspend fun refreshToken(user: UserEntity): ResultWrapper<UserEntity> {
        return safeApiCall(dispatcher) {
            val tokens = user.tokens
            val response =
                userService.refreshToken(TokensDto(tokens.accessToken, tokens.refreshToken))
            appDatabase.userDao().updateUser(user.copy(tokens = response.toEntity()))
            return@safeApiCall user.copy(tokens = response.toEntity())
        }
    }

    suspend fun loginAsGuest() {
        appDatabase.userDao().resetCurrent()
        _userRoles.postValue(listOf("Guest"))
    }

    suspend fun logout() {
        appDatabase.userDao().resetCurrent()
    }

    suspend fun fetchPasswordRequirements(): ResultWrapper<PasswordRequirementsDto> {
        return safeApiCall(dispatcher) {
            val response = userService.getPasswordRequirements()
            response
        }
    }

    suspend fun fetchUsers(): ResultWrapper<List<UserInfoDto>> {
            return safeApiCall(dispatcher) {
                val response = userService.getUsers()
                _users.postValue(response)
                response
            }
    }
}