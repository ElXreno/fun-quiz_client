package com.github.elxreno.funquiz_client.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.github.elxreno.funquiz_client.data.dto.QuizQuestionDto

@Entity(
    tableName = "quiz_questions",
    foreignKeys = [
        ForeignKey(
            entity = QuizStageEntity::class,
            parentColumns = ["id"],
            childColumns = ["quizStageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["quizStageId"])
    ]
)
data class QuizQuestionEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    var quizStageId: Int,
    val question: String,
    val requiredAnswerType: String,
    val rightAnswers: List<String>,
    val wrongAnswers: List<String>
) {
    fun toDto() = QuizQuestionDto(
        id = id,
        question = question,
        requiredAnswerType = requiredAnswerType,
        rightAnswers = rightAnswers,
        wrongAnswers = wrongAnswers
    )
}
