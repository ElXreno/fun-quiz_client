package com.github.elxreno.funquiz_client.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.elxreno.funquiz_client.data.dao.QuizDao
import com.github.elxreno.funquiz_client.data.dao.UserDao
import com.github.elxreno.funquiz_client.data.database.converters.StringListConverter
import com.github.elxreno.funquiz_client.data.entity.QuizEntity
import com.github.elxreno.funquiz_client.data.entity.QuizQuestionEntity
import com.github.elxreno.funquiz_client.data.entity.QuizStageEntity
import com.github.elxreno.funquiz_client.data.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        QuizEntity::class,
        QuizStageEntity::class,
        QuizQuestionEntity::class
    ],
    version = 1,
    autoMigrations = []
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao
    abstract fun userDao(): UserDao
}