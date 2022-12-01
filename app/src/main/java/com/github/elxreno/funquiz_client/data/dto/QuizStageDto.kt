package com.github.elxreno.funquiz_client.data.dto

import android.os.Parcelable
import com.github.elxreno.funquiz_client.data.entity.QuizStageEntity
import com.github.elxreno.funquiz_client.data.entity.StageWithQuestions
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuizStageDto(
    val id: Int,
    val scorePerQuestion: Int,
    val quizQuestions: List<QuizQuestionDto>,
) : Parcelable {
    fun toEntity(quizId: Int) = StageWithQuestions(
        quizStage = QuizStageEntity(
            id = id,
            quizId = quizId,
            scorePerQuestion = scorePerQuestion,
        ),
        quizQuestions = quizQuestions.map { it.toEntity(id) },
    )
}
