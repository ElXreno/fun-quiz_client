package com.github.elxreno.funquiz_client.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.github.elxreno.funquiz_client.data.entity.QuizEntity
import com.github.elxreno.funquiz_client.data.entity.QuizQuestionEntity
import com.github.elxreno.funquiz_client.data.entity.QuizStageEntity
import com.github.elxreno.funquiz_client.data.entity.QuizWithStages
import com.github.elxreno.funquiz_client.data.entity.StageWithQuestions

@Dao
interface QuizDao {

    @Transaction
    @Query("SELECT * FROM quizzes WHERE createdBy = (SELECT id FROM users WHERE isCurrent = 1 LIMIT 1)")
    fun getQuizzesByCurrentUser(): LiveData<List<QuizWithStages>>

    @Query("SELECT id FROM users WHERE isCurrent = 1 LIMIT 1")
    fun getCurrentUserId(): String

    @Transaction
    @Query("SELECT * FROM quizzes WHERE isPublic = 1")
    fun getPublicQuizzes(): LiveData<List<QuizWithStages>>

    @Transaction
    @Query("SELECT * FROM quizzes WHERE id = :id")
    fun getQuizById(id: Int): QuizWithStages

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStage(quizStage: QuizStageEntity)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questionEntity: List<QuizQuestionEntity>)

    @Transaction
    suspend fun insertQuizWithStages(quizWithStages: QuizWithStages) {
        insertQuiz(quizWithStages.quiz)

        quizWithStages.quizStages?.let { stage ->
            insertStagesWithQuestions(stage)
        }
    }

    @Transaction
    suspend fun insertStagesWithQuestions(stagesWithQuestions: List<StageWithQuestions>) {
        stagesWithQuestions.forEach {
            insertStage(it.quizStage)

            it.quizQuestions?.let { questions ->
                questions.forEach { q -> q.quizStageId = it.quizStage.id }
                insertQuestions(questions)
            }
        }
    }

    @Transaction
    @Query("DELETE FROM quizzes WHERE id = :quizId")
    suspend fun deleteQuiz(quizId: Int)

    @Transaction
    @Query("DELETE FROM quizzes WHERE id NOT IN (:quizIds) AND isPublic = :isPublic")
    suspend fun deleteQuizzesNotIn(quizIds: List<Int>, isPublic: Boolean)
}