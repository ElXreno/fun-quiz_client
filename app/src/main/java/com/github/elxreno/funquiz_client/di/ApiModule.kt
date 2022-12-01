package com.github.elxreno.funquiz_client.di

import com.github.elxreno.funquiz_client.data.service.QuizService
import com.github.elxreno.funquiz_client.data.service.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserService =
        retrofit.create(UserService::class.java)

    @Annotations.Auth
    @Provides
    @Singleton
    fun provideAuthUserApi(@Annotations.Auth retrofit: Retrofit): UserService =
        retrofit.create(UserService::class.java)

    @Provides
    @Singleton
    fun provideQuizApi(@Annotations.Auth retrofit: Retrofit): QuizService =
        retrofit.create(QuizService::class.java)
}