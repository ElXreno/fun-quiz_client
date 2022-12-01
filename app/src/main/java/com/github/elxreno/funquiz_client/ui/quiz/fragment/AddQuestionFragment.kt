package com.github.elxreno.funquiz_client.ui.quiz.fragment

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.github.elxreno.funquiz_client.R
import com.github.elxreno.funquiz_client.data.dto.QuizQuestionDto
import com.github.elxreno.funquiz_client.databinding.FragmentAddQuestionBinding
import com.github.elxreno.funquiz_client.ui.ClickListener
import com.github.elxreno.funquiz_client.ui.quiz.QuizViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddQuestionFragment(
    private val questionToReplace: Pair<String, Pair<List<String>, List<String>>>? = null,
    private val onClickListener: ClickListener? = null
) : DialogFragment() {

    private lateinit var binding: FragmentAddQuestionBinding

    private val viewModel by activityViewModels<QuizViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonCancelQuestion.setOnClickListener {
            dismiss()
        }

        if (questionToReplace != null) {
            binding.banner.text = getString(R.string.update_quiz_question)
            binding.editTextQuestionName.text =
                Editable.Factory.getInstance().newEditable(questionToReplace.first)
            binding.editTextQuestionRightAnswers.text = Editable.Factory.getInstance().newEditable(
                questionToReplace.second.first.joinToString(separator = "; ")
            )
            binding.editTextQuestionWrongAnswers.text = Editable.Factory.getInstance().newEditable(
                questionToReplace.second.second.joinToString(separator = "; ")
            )
        }

        binding.buttonAddQuestion.setOnClickListener {
            val question = binding.editTextQuestionName.text.toString()
            val rightAnswers = binding.editTextQuestionRightAnswers.text.toString()
            val wrongAnswers = binding.editTextQuestionWrongAnswers.text.toString()

            if (question.isEmpty()) {
                binding.editTextQuestionName.error = getString(R.string.question_name_empty)
                binding.editTextQuestionName.requestFocus()
                return@setOnClickListener
            }

            if (rightAnswers.isEmpty()) {
                binding.editTextQuestionRightAnswers.error =
                    getString(R.string.question_answer_empty)
                binding.editTextQuestionRightAnswers.requestFocus()
                return@setOnClickListener
            }

            if (wrongAnswers.isEmpty()) {
                binding.editTextQuestionWrongAnswers.error =
                    getString(R.string.question_answer_empty)
                binding.editTextQuestionWrongAnswers.requestFocus()
                return@setOnClickListener
            }

            if (questionToReplace == null || onClickListener == null) {
                viewModel.addQuestion(
                    QuizQuestionDto(
                        id = 0,
                        question = question,
                        requiredAnswerType = "Text",
                        rightAnswers = rightAnswers.split(";").map { it.trim() },
                        wrongAnswers = wrongAnswers.split(";").map { it.trim() }
                    )
                )
            } else {
                onClickListener.onSavedClicked(
                    Pair(
                        question,
                        Pair(
                            rightAnswers.split(";").map { it.trim() },
                            wrongAnswers.split(";").map { it.trim() }
                        )
                    )
                )
            }
            dismiss()
        }
    }
}