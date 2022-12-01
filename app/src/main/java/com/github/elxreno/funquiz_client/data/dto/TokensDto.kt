package com.github.elxreno.funquiz_client.data.dto

import android.os.Parcelable
import com.github.elxreno.funquiz_client.data.entity.TokensEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class TokensDto(
    val accessToken: String,
    val refreshToken: String
) : Parcelable {
    fun toEntity(): TokensEntity {
        return TokensEntity(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }
}