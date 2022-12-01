package com.github.elxreno.funquiz_client.data.entity

import com.github.elxreno.funquiz_client.data.dto.TokensDto

data class TokensEntity(
    val accessToken: String,
    val refreshToken: String
) {
    fun toDto() = TokensDto(accessToken, refreshToken)
}
