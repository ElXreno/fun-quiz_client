package com.github.elxreno.funquiz_client.di

import com.github.elxreno.funquiz_client.data.ResultWrapper
import com.github.elxreno.funquiz_client.data.repository.UserRepository
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.HttpURLConnection
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkingModule {
    companion object {
        const val BASEURL = "https://funquiz.elxreno.me"
    }

    class TokenAuthenticator(private val userRepository: UserRepository) : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                val user =
                    runBlocking(Dispatchers.IO) { userRepository.getLocalUser() } ?: return null
                val refreshTokenResponse =
                    runBlocking(Dispatchers.IO) { userRepository.refreshToken(user) }
                return if (refreshTokenResponse is ResultWrapper.Success) {
                    val newTokens = refreshTokenResponse.value.tokens
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${newTokens.accessToken}")
                        .build()
                } else {
                    runBlocking(Dispatchers.IO) { userRepository.logout() }
                    null
                }
            }
            return null
        }
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().build()

    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASEURL)
            .addCallAdapterFactory(NetworkResponseAdapterFactory())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideAuthenticator(userRepository: UserRepository): Authenticator =
        TokenAuthenticator(userRepository)

    @Annotations.Auth
    @Provides
    @Singleton
    fun provideAuthOkHttpClient(
        authenticator: Authenticator,
        userRepository: UserRepository
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val user = runBlocking(Dispatchers.IO) { userRepository.getLocalUser() }
                    ?: return@addInterceptor chain.proceed(chain.request())
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${user.tokens.accessToken}")
                    .build()
                chain.proceed(request)
            }
            .authenticator(authenticator)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    @Annotations.Auth
    @Provides
    @Singleton
    fun provideAuthRetrofit(
        retrofit: Retrofit,
        @Annotations.Auth okHttpClient: OkHttpClient
    ): Retrofit =
        retrofit.newBuilder()
            .client(okHttpClient)
            .build()
}