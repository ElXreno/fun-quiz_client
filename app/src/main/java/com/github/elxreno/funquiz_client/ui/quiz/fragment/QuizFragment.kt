package com.github.elxreno.funquiz_client.ui.quiz.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.elxreno.funquiz_client.R
import com.github.elxreno.funquiz_client.data.dto.QuizDto
import com.github.elxreno.funquiz_client.data.dto.QuizQuestionDto
import com.github.elxreno.funquiz_client.data.dto.QuizStageDto
import com.github.elxreno.funquiz_client.data.entity.QuizEntity
import com.github.elxreno.funquiz_client.databinding.FragmentQuizBinding
import com.github.elxreno.funquiz_client.ui.ClickListener
import com.github.elxreno.funquiz_client.ui.quiz.QuestionsExpandableListAdapter
import com.github.elxreno.funquiz_client.ui.quiz.QuizViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class QuizFragment : Fragment(), MenuProvider {

    private val args: QuizFragmentArgs by navArgs()

    private lateinit var binding: FragmentQuizBinding
    private val viewModel by activityViewModels<QuizViewModel>()

    private val questions = mutableListOf<QuizQuestionDto>()
    private var quiz: QuizEntity? = null

    private var quizSaved = true

    private val job = SupervisorJob()
    private val ioScope by lazy { CoroutineScope(Dispatchers.IO + job) }

    private val removeClickListener = object : ClickListener {
        override fun onPositionClicked(position: Int) {
            questions.removeAt(position)
        }

        override fun onSavedClicked(pair: Pair<String, Pair<List<String>, List<String>>>) {
            TODO("Not yet implemented")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (args.canEdit) {
            val callback: OnBackPressedCallback =
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) && !quizSaved) {
                            AlertDialog.Builder(requireContext())
                                .setTitle(R.string.quiz_title)
                                .setMessage(R.string.are_you_sure_leave_quiz_edit)
                                .setPositiveButton(R.string.yes) { _, _ ->
                                    findNavController().navigateUp()
                                }
                                .setNegativeButton(R.string.no) { _, _ -> }
                                .show()
                        } else {
                            findNavController().navigateUp()
                        }
                    }
                }

            requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        if (!args.canSeeQuestions) {
            binding.buttonAddQuestion.visibility = View.GONE
            binding.expandableListViewQuestions.visibility = View.GONE
            binding.bannerQuestions.visibility = View.GONE
        }

        if (!args.canEdit) {
            binding.editTextQuizName.isEnabled = false
            binding.switchIsPublic.isEnabled = false
            binding.buttonAddQuestion.isEnabled = false
        }

        if (args.quizId != -1) {
            viewModel.loadQuizById(args.quizId)
            viewModel.currentQuiz.observe(viewLifecycleOwner) { quizWithStages ->
                quiz = quizWithStages.quiz
                binding.editTextQuizName.text =
                    Editable.Factory.getInstance().newEditable(quizWithStages.quiz.name)
                binding.switchIsPublic.isChecked = quizWithStages.quiz.isPublic

                if (args.canEdit || args.canSeeQuestions) {
                    if (quizWithStages.quizStages?.isNotEmpty() == true) {
                        questions.clear()
                        quizWithStages?.quizStages.forEach { stage ->
                            questions.addAll(stage.quizQuestions?.map { it.toDto() } ?: emptyList())
                        }
                        updateQuestionsListView()
                    }
                }

                quizSaved = true
            }
        }

        binding.buttonAddQuestion.setOnClickListener {
            val addQuestionFragment = AddQuestionFragment()
            addQuestionFragment.show(childFragmentManager, "AddQuestionFragment")
        }

        binding.editTextQuizName.addTextChangedListener {
            quizSaved = false
        }
        binding.switchIsPublic.setOnCheckedChangeListener { _, _ ->
            quizSaved = false
        }

        viewModel.questions.observe(viewLifecycleOwner) {
            println("AddQuestionFragment returned: $it")
            if (it != null) {
                loadQuestionsFromListView()
                questions.add(it)
                updateQuestionsListView()
                viewModel.clearQuestions()
                quizSaved = false
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_quiz, menu)
        menu.findItem(R.id.action_save).isVisible = args.canEdit
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_save -> {
                if (!isAllFieldsValid())
                    return true

                loadQuestionsFromListView()
                val stage = QuizStageDto(
                    id = 0,
                    scorePerQuestion = 0,
                    quizQuestions = questions.map { question ->
                        QuizQuestionDto(
                            id = 0,
                            question = question.question,
                            requiredAnswerType = "Text",
                            rightAnswers = question.rightAnswers,
                            wrongAnswers = question.wrongAnswers,
                        )
                    }.toMutableList()
                )

                val quiz = QuizDto(
                    id = quiz?.id ?: 0,
                    name = binding.editTextQuizName.text.toString(),
                    createdBy = quiz?.createdBy ?: "",
                    isPublic = binding.switchIsPublic.isChecked,
                    quizStages = mutableListOf(stage)
                )

                ioScope.launch {
                    withContext(Dispatchers.Main) {
                        menuItem.setActionView(android.R.layout.simple_spinner_item)
                    }
                    if (quiz.id == 0) {
                        viewModel.addQuiz(quiz)
                    } else {
                        viewModel.updateQuiz(quiz)
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Quiz ${quiz.name} saved",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    quizSaved = true
                }

                true
            }

            else -> false
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    private fun updateQuestionsListView() {
        val questionsMap = questions.map { question ->
            Pair(
                question.question,
                Pair(
                    question.rightAnswers,
                    question.wrongAnswers
                )
            )
        }

        val titles = ArrayList(questionsMap.map { it.first })

        val adapter = QuestionsExpandableListAdapter(
            context = requireContext(),
            fragmentManager = childFragmentManager,
            expandableListTitle = titles,
            expandableListDetail = questionsMap,
            removeOnClickListener = removeClickListener,
            canEditQuestions = args.canEdit
        )

        binding.expandableListViewQuestions.setAdapter(
            adapter
        )
    }

    private fun loadQuestionsFromListView() {
        val q = binding.expandableListViewQuestions.expandableListAdapter
            .let { if (it is QuestionsExpandableListAdapter) it.getQuestions() else listOf() }

        questions.clear()
        questions.addAll(
            q.map { question ->
                QuizQuestionDto(
                    id = 0,
                    question = question.first,
                    requiredAnswerType = "Text",
                    rightAnswers = question.second.first.map { answer ->
                        answer
                    },
                    wrongAnswers = question.second.second.map { answer ->
                        answer
                    }
                )
            }
        )
    }

    private fun isAllFieldsValid(): Boolean {
        if (binding.editTextQuizName.text.isNullOrBlank()) {
            requireActivity().runOnUiThread {
                binding.editTextQuizName.error = getString(R.string.quiz_name_empty)
                binding.editTextQuizName.requestFocus()
            }
            return false
        }
        return true
    }
}