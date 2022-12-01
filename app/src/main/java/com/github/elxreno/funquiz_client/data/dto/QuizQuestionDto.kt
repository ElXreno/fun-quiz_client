package com.github.elxreno.funquiz_client.data.dto

import android.os.Parcelable
import com.github.elxreno.funquiz_client.data.entity.QuizQuestionEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuizQuestionDto(
    val id: Int,
    val question: String,
    val requiredAnswerType: String,
    val rightAnswers: List<String>,
    val wrongAnswers: List<String>,
) : Parcelable {
    fun toEntity(quizStageId: Int) = QuizQuestionEntity(
        id = id,
        quizStageId = quizStageId,
        question = question,
        requiredAnswerType = requiredAnswerType,
        rightAnswers = rightAnswers,
        wrongAnswers = wrongAnswers
    )
}