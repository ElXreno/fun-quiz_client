package com.github.elxreno.funquiz_client.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.elxreno.funquiz_client.data.dto.QuizDto

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    var name: String,
    val createdBy: String,
    val isPublic: Boolean
) {
    fun toDto(quizStageEntity: List<StageWithQuestions>) = QuizDto(
        id = id,
        name = name,
        createdBy = createdBy,
        isPublic = isPublic,
        quizStages = quizStageEntity.map { it.toDto() },
    )
}
