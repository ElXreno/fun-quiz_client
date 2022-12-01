package com.github.elxreno.funquiz_client.ui.pass_test

import android.app.AlertDialog
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.github.elxreno.funquiz_client.R
import com.github.elxreno.funquiz_client.data.ResultWrapper
import com.github.elxreno.funquiz_client.data.entity.QuizWithStages
import com.github.elxreno.funquiz_client.data.repository.QuizRepository
import com.github.elxreno.funquiz_client.databinding.ActivityPassTestBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class PassTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPassTestBinding
    private var quizId = -1

    private lateinit var quizWithStages: QuizWithStages
    private lateinit var stages: QuizWithStages

    @Inject
    lateinit var quizRepository: QuizRepository

    private var currentQuestionIndex = 0
    private var currentStageIndex = 0
    private var shuffledAnswers = HashMap<Int, List<String>>()
    private var answers = HashMap<Int, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                        AlertDialog.Builder(this@PassTestActivity)
                            .setTitle(R.string.quiz_title)
                            .setMessage(R.string.are_you_sure_leave_test)
                            .setPositiveButton(R.string.yes) { _, _ ->
                                finish()
                            }
                            .setNegativeButton(R.string.no) { _, _ -> }
                            .show()
                    }
                }
            }

        onBackPressedDispatcher.addCallback(this, callback)

        binding = ActivityPassTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        quizId = intent.getIntExtra("quizId", -1)

        if (quizId == -1) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            finish()
        }

        runBlocking(Dispatchers.IO) {
            val quiz = quizRepository.getQuizById(quizId)
            if (quiz is ResultWrapper.Success) {
                quizWithStages = quiz.value
                stages = quiz.value
            }
        }

        binding.textViewTitle.text = quizWithStages.quiz.name

        loadQuestion()

        binding.buttonNext.setOnClickListener {
            nextQuestion()
        }

        binding.buttonPrevious.setOnClickListener {
            previousQuestion()
        }

        binding.buttonSubmit.setOnClickListener {
            callback.isEnabled = false

            binding.radioGroupAnswers.visibility = android.view.View.GONE
            binding.linearLayoutButtons.visibility = android.view.View.GONE

            binding.textViewTitle.text = String.format(
                getString(R.string.quiz_result),
                quizWithStages.quiz.name
            )

            var correctAnswers = 0
            for (i in 0 until quizWithStages.quizStages?.first()?.quizQuestions?.size!!) {
                if (quizWithStages.quizStages?.first()?.quizQuestions!![i].rightAnswers.contains(answers[i])) {
                    correctAnswers++
                }
            }

            binding.textViewQuestion.text = String.format(
                getString(R.string.quiz_right_answers),
                correctAnswers,
                quizWithStages.quizStages?.first()?.quizQuestions!!.size
            )

            binding.buttonSubmit.setOnClickListener { finish() }
            binding.buttonSubmit.text = getString(R.string.quiz_finish)
        }

        binding.radioGroupAnswers.setOnCheckedChangeListener { _, _ ->
            saveAnswer()
        }

    }

    private fun nextQuestion() {
        if (currentQuestionIndex >= quizWithStages.quizStages?.first()?.quizQuestions?.size!! - 1) {
            Toast.makeText(this, "End", Toast.LENGTH_SHORT).show()
            return
        }

        currentQuestionIndex++

        loadQuestion()
    }

    private fun previousQuestion() {
        if (currentQuestionIndex <= 0) {
            Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show()
            return
        }

        currentQuestionIndex--

        loadQuestion()
    }

    private fun loadQuestion() {
        if (shuffledAnswers.isEmpty()) {
            for (i in 0 until quizWithStages.quizStages?.first()?.quizQuestions?.size!!) {
                val answers =
                    quizWithStages.quizStages?.first()?.quizQuestions!![i].rightAnswers + quizWithStages.quizStages?.first()?.quizQuestions!![i].wrongAnswers
                shuffledAnswers[i] = answers.shuffled()
            }
        }

        binding.textViewQuestion.text =
            quizWithStages.quizStages?.first()?.quizQuestions!![currentQuestionIndex].question

        binding.radioGroupAnswers.removeAllViews()
        binding.radioGroupAnswers.clearCheck()

        for (i in 0 until shuffledAnswers[currentQuestionIndex]?.size!!) {
            val radioButton = RadioButton(this)
            radioButton.text = shuffledAnswers[currentQuestionIndex]?.get(i)
            radioButton.textSize = 20f
            radioButton.id = i
            binding.radioGroupAnswers.addView(radioButton)
        }

        if (answers.containsKey(currentQuestionIndex)) {
            binding.radioGroupAnswers.check(
                shuffledAnswers[currentQuestionIndex]?.indexOf(answers[currentQuestionIndex])!!
            )
        }

        binding.buttonNext.isEnabled =
            currentQuestionIndex < quizWithStages.quizStages?.first()?.quizQuestions?.size!! - 1
        binding.buttonPrevious.isEnabled = currentQuestionIndex > 0
    }

    private fun saveAnswer() {
        val selectedAnswer = binding.radioGroupAnswers.checkedRadioButtonId
        if (selectedAnswer != -1) {
            answers[currentQuestionIndex] =
                shuffledAnswers[currentQuestionIndex]?.get(selectedAnswer)!!
        }
        binding.buttonSubmit.isEnabled = answers.size == quizWithStages.quizStages?.first()?.quizQuestions?.size
    }

}