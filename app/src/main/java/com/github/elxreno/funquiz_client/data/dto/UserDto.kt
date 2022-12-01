package com.github.elxreno.funquiz_client.data.dto

import android.os.Parcelable
import com.github.elxreno.funquiz_client.data.entity.UserEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserDto(
    val id: String,
    val userName: String,
    val displayName: String,
    val email: String,
    val tokens: TokensDto
) : Parcelable {
    fun toEntity(isCurrent: Boolean): UserEntity {
        return UserEntity(
            id = id,
            username = userName,
            displayName = displayName,
            email = email,
            tokens = tokens.toEntity(),
            isCurrent = isCurrent
        )
    }
}