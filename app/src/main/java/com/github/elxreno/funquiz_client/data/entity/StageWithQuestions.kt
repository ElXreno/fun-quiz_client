package com.github.elxreno.funquiz_client.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class StageWithQuestions(
    @Embedded val quizStage: QuizStageEntity,
    @Relation(parentColumn = "id", entityColumn = "quizStageId")
    val quizQuestions: List<QuizQuestionEntity>? = null
) {
    fun toDto() = quizStage.toDto(quizQuestions ?: emptyList())
}
