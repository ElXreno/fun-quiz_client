package com.github.elxreno.funquiz_client.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.github.elxreno.funquiz_client.data.entity.UserEntity

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE isCurrent = 1 LIMIT 1")
    fun getUser(): LiveData<UserEntity>

    @Query("SELECT * FROM users WHERE isCurrent = 1 LIMIT 1")
    suspend fun getUserAsync(): UserEntity?

    @Query("UPDATE users SET isCurrent = 0")
    suspend fun resetCurrent()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}
