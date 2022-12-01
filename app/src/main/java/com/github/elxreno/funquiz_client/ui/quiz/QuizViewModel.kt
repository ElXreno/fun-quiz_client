package com.github.elxreno.funquiz_client.ui.quiz

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.github.elxreno.funquiz_client.data.ResultWrapper
import com.github.elxreno.funquiz_client.data.dto.QuizDto
import com.github.elxreno.funquiz_client.data.dto.QuizQuestionDto
import com.github.elxreno.funquiz_client.data.entity.QuizEntity
import com.github.elxreno.funquiz_client.data.entity.QuizWithStages
import com.github.elxreno.funquiz_client.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _currentQuiz = MutableLiveData<QuizWithStages>()
    val currentQuiz: LiveData<QuizWithStages> = _currentQuiz

    val quizzes = quizRepository.quizzes
    val publicQuizzes = quizRepository.publicQuizzes

    val questions: MutableLiveData<QuizQuestionDto?> = MutableLiveData()

    fun loadQuizzes(onlyPublic: Boolean = false) {
        viewModelScope.launch {
            val result = quizRepository.loadQuizzes(onlyPublic)
            println(result)
        }
    }

    fun loadQuizById(id: Int) {
        viewModelScope.launch {
            val result = quizRepository.getQuizById(id)
            if (result is ResultWrapper.Success) {
                result.value.let {
                    _currentQuiz.value = it
                }
            }
        }
    }

    suspend fun addQuiz(quizDto: QuizDto) {
        quizRepository.addQuiz(quizDto)
    }

    suspend fun updateQuiz(quizDto: QuizDto) {
        quizRepository.updateQuiz(quizDto)
    }

    suspend fun deleteQuiz(quizDto: QuizDto) {
        quizRepository.deleteQuiz(quizDto.id)
    }

    fun addQuestion(question: QuizQuestionDto) {
        questions.postValue(question)
    }

    fun clearQuestions() {
        questions.postValue(null)
    }

    fun clearCurrentQuiz() {
        _currentQuiz.postValue(
            QuizWithStages(
                QuizEntity(-1, "", "", false), null
            )
        )
    }

    fun exportToCsv(): String {
        val quizzes = quizzes.value
        quizzes?.let {
            val head = listOf(
                "Название викторины",
                "Количество вопросов",
                "Количество стадий",
                "Доступно для всех",
            )
            val data = it.map { quiz ->
                listOf(
                    quiz.quiz.name,
                    quiz.quizStages?.sumOf { stage -> stage.quizQuestions?.size ?: 0 }.toString(),
                    quiz.quizStages?.size ?: 0,
                    quiz.quiz.isPublic.toString(),
                )
            }
            val csv = csvWriter().writeAllAsString(listOf(head) + data)

            println(csv)
            return csv
        }
        return ""
    }
}