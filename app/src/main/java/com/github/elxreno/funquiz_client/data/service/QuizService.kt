package com.github.elxreno.funquiz_client.data.service

import com.github.elxreno.funquiz_client.data.dto.QuizDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface QuizService {
    @GET("api/Quiz")
    suspend fun getQuizzes(@Query("public") public: Boolean): List<QuizDto>

    @POST("api/Quiz")
    suspend fun addQuiz(@Body quizDto: QuizDto): Response<QuizDto>

    @PUT("api/Quiz/{id}")
    suspend fun updateQuiz(@Path("id") id: Int, @Body quizDto: QuizDto): Response<QuizDto>

    @DELETE("api/Quiz/{id}")
    suspend fun deleteQuiz(@Path("id") id: Int): Response<Void>
}