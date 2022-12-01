package com.github.elxreno.funquiz_client.data.dto

data class UserInfoDto(
    val id: String,
    val userName: String,
    val displayName: String,
    val email: String,
    val roles: List<String>
)
