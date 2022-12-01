package com.github.elxreno.funquiz_client.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val username: String,
    val displayName: String,
    val email: String,
    @Embedded
    var tokens: TokensEntity,
    var isCurrent: Boolean
)