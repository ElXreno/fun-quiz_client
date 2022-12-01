package com.github.elxreno.funquiz_client.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class QuizWithStages(
    @Embedded val quiz: QuizEntity,
    @Relation(
        entity = QuizStageEntity::class,
        parentColumn = "id",
        entityColumn = "quizId"
    )
    val quizStages: List<StageWithQuestions>? = null
) {
    fun toDto(stagesWithQuestions: List<StageWithQuestions>) = quiz.toDto(stagesWithQuestions)
}