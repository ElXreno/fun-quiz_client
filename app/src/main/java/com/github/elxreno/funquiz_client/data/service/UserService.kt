package com.github.elxreno.funquiz_client.data.service

import com.github.elxreno.funquiz_client.data.dto.PasswordRequirementsDto
import com.github.elxreno.funquiz_client.data.dto.TokensDto
import com.github.elxreno.funquiz_client.data.dto.UserDto
import com.github.elxreno.funquiz_client.data.dto.UserInfoDto
import com.github.elxreno.funquiz_client.data.dto.UserLoginDto
import com.github.elxreno.funquiz_client.data.dto.UserRegisterDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserService {
    @GET("getPasswordRequirements")
    suspend fun getPasswordRequirements(): PasswordRequirementsDto

    @POST("login")
    suspend fun login(@Body userLoginDto: UserLoginDto): UserDto

    @POST("register")
    suspend fun register(@Body userRegisterDto: UserRegisterDto)

    @POST("refreshToken")
    suspend fun refreshToken(@Body tokensDto: TokensDto): TokensDto

    @GET("getUsers")
    suspend fun getUsers(): List<UserInfoDto>
}