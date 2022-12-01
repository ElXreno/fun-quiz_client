package com.github.elxreno.funquiz_client.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.github.elxreno.funquiz_client.data.dto.QuizStageDto

@Entity(
    tableName = "quiz_stages",
    foreignKeys = [
        ForeignKey(
            entity = QuizEntity::class,
            parentColumns = ["id"],
            childColumns = ["quizId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["quizId"])
    ]
)
data class QuizStageEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val quizId: Int,
    val scorePerQuestion: Int,
) {
    fun toDto(quizQuestions: List<QuizQuestionEntity>) = QuizStageDto(
        id = id,
        scorePerQuestion = scorePerQuestion,
        quizQuestions = quizQuestions.map { it.toDto() },
    )
}
