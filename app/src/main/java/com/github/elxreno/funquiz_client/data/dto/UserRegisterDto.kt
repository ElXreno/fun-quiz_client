package com.github.elxreno.funquiz_client.data.dto

data class UserRegisterDto(
    val userName: String,
    val email: String,
    val password: String,
    val passwordConfirm: String
)
