package com.github.elxreno.funquiz_client.data.repository

import androidx.lifecycle.LiveData
import com.github.elxreno.funquiz_client.data.ResultWrapper
import com.github.elxreno.funquiz_client.data.database.AppDatabase
import com.github.elxreno.funquiz_client.data.dto.QuizDto
import com.github.elxreno.funquiz_client.data.entity.QuizWithStages
import com.github.elxreno.funquiz_client.data.safeApiCall
import com.github.elxreno.funquiz_client.data.service.QuizService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class QuizRepository(
    private val appDatabase: AppDatabase,
    private val quizService: QuizService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    val quizzes: LiveData<List<QuizWithStages>>
        get() = appDatabase.quizDao().getQuizzesByCurrentUser()

    val publicQuizzes: LiveData<List<QuizWithStages>>
        get() = appDatabase.quizDao().getPublicQuizzes()

    suspend fun loadQuizzes(public: Boolean = false): ResultWrapper<Unit> {
        return safeApiCall(dispatcher) {
            val response = quizService.getQuizzes(public)
            //val response = if (public) quizService.getPublicQuizzes() else quizService.getQuizzes()

            val quizIds = response.map { it.id }
            appDatabase.quizDao().deleteQuizzesNotIn(quizIds, public)

            response.forEach { quiz ->
                val quizEntity = quiz.toEntity()
                appDatabase.quizDao().insertQuizWithStages(quizEntity)
            }
        }
    }

    suspend fun getQuizById(id: Int): ResultWrapper<QuizWithStages> {
        return safeApiCall(dispatcher) {
            appDatabase.quizDao().getQuizById(id)
        }
    }

    suspend fun addQuiz(quiz: QuizDto): ResultWrapper<Unit> {
        return safeApiCall(dispatcher) {
            quizService.addQuiz(quiz)
            loadQuizzes()
        }
    }

    suspend fun updateQuiz(quiz: QuizDto): ResultWrapper<Unit> {
        return safeApiCall(dispatcher) {
            quizService.updateQuiz(quiz.id, quiz)
            loadQuizzes()
        }
    }

    suspend fun deleteQuiz(quizId: Int): ResultWrapper<Unit> {
        return safeApiCall(dispatcher) {
            appDatabase.quizDao().deleteQuiz(quizId)
            quizService.deleteQuiz(quizId)
        }
    }
}