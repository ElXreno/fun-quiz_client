package com.github.elxreno.funquiz_client.data.dto

import android.os.Parcelable
import com.github.elxreno.funquiz_client.data.entity.QuizEntity
import com.github.elxreno.funquiz_client.data.entity.QuizWithStages
import com.github.elxreno.funquiz_client.data.entity.StageWithQuestions
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuizDto(
    val id: Int,
    var name: String,
    val createdBy: String,
    val isPublic: Boolean,
    val quizStages: List<QuizStageDto>,
) : Parcelable {
    fun toEntity() = QuizWithStages(
        quiz = QuizEntity(
            id = id,
            name = name,
            createdBy = createdBy,
            isPublic = isPublic,
        ),
        quizStages = quizStages.map { it.toEntity(id) }
    )
}