package com.github.elxreno.funquiz_client.di

import com.github.elxreno.funquiz_client.data.database.AppDatabase
import com.github.elxreno.funquiz_client.data.repository.QuizRepository
import com.github.elxreno.funquiz_client.data.repository.UserRepository
import com.github.elxreno.funquiz_client.data.service.QuizService
import com.github.elxreno.funquiz_client.data.service.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        appDatabase: AppDatabase,
        userService: UserService
    ): UserRepository = UserRepository(appDatabase, userService)

    @Annotations.Auth
    @Provides
    @Singleton
    fun provideAuthUserRepository(
        appDatabase: AppDatabase,
        @Annotations.Auth userService: UserService
    ): UserRepository = UserRepository(appDatabase, userService)

    @Provides
    @Singleton
    fun provideQuizRepository(
        appDatabase: AppDatabase,
        quizService: QuizService
    ): QuizRepository = QuizRepository(appDatabase, quizService)
}