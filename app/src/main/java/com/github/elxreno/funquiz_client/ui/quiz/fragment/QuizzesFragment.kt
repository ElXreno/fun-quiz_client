package com.github.elxreno.funquiz_client.ui.quiz.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.github.elxreno.funquiz_client.R
import com.github.elxreno.funquiz_client.data.repository.QuizRepository
import com.github.elxreno.funquiz_client.data.service.QuizService
import com.github.elxreno.funquiz_client.databinding.FragmentQuizzesBinding
import com.github.elxreno.funquiz_client.ui.ClickListener
import com.github.elxreno.funquiz_client.ui.quiz.QuizAdapter
import com.github.elxreno.funquiz_client.ui.quiz.QuizViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class QuizzesFragment(
    private val adminMode: Boolean = false,
    private val showOnlyPublic: Boolean = false,
    private val moderatorMode: Boolean = false,
    private val canPassTest: Boolean = true
) : Fragment(), MenuProvider {

    companion object {
        fun newInstance(
            adminMode: Boolean = false,
            showOnlyPublic: Boolean = false,
            moderatorMode: Boolean = false,
            canPassTest: Boolean = true
        ) = QuizzesFragment(adminMode, showOnlyPublic, moderatorMode, canPassTest)
    }

    private lateinit var binding: FragmentQuizzesBinding
    private lateinit var adapter: QuizAdapter

    @Inject
    lateinit var quizService: QuizService

    @Inject
    lateinit var quizRepository: QuizRepository

    private val viewModel by viewModels<QuizViewModel>()

    private val job = SupervisorJob()
    private val ioScope by lazy { CoroutineScope(job + Dispatchers.IO) }

    private lateinit var csv: String
    private val registerForActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                if (::csv.isInitialized || csv.isNotEmpty()) {
                    result.data?.data?.let { uri ->
                        requireContext().contentResolver.openOutputStream(uri, "w")?.use {
                            it.write(csv.toByteArray(Charsets.UTF_16))
                            Toast.makeText(
                                requireContext(),
                                R.string.export_success,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        R.string.export_no_data,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQuizzesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val onClickListener = object : ClickListener {
            override fun onPositionClicked(position: Int) {
                val quizId = adapter.getQuizByPosition(position).quiz.id
                findNavController().navigate(
                    QuizDashboardFragmentDirections.actionQuizzesDashboardFragmentToQuizFragment(
                        quizId =  quizId,
                        canEdit = adminMode,
                        canSeeQuestions = moderatorMode
                    )
                )
            }

            override fun onSavedClicked(pair: Pair<String, Pair<List<String>, List<String>>>) {
                TODO("Not yet implemented")
            }
        }

        val removeOnClickListener = object : ClickListener {
            override fun onPositionClicked(position: Int) {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.quiz_remove))
                    .setMessage(getString(R.string.quiz_remove_message))
                    .setPositiveButton(getString(R.string.remove)) { _, _ ->
                        ioScope.launch {
                            quizRepository.deleteQuiz(position)
                        }
                    }
                    .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                    .show()
            }

            override fun onSavedClicked(pair: Pair<String, Pair<List<String>, List<String>>>) {
                TODO("Not yet implemented")
            }
        }

        adapter = QuizAdapter(
            context = requireContext(),
            onClickListener = onClickListener,
            removeOnClickListener = removeOnClickListener,
            showOnlyPublic = showOnlyPublic,
            moderatorMode = moderatorMode,
            canPassTest = canPassTest
        )

        binding.recyclerViewQuiz.adapter = adapter

        if (showOnlyPublic) {
            viewModel.publicQuizzes.observe(viewLifecycleOwner) {
                println("New public quizzes fetched: $it")
                adapter.updateQuizzes(it)
            }
            binding.addFloatingActionButton.visibility = View.GONE
        } else {
            viewModel.quizzes.observe(viewLifecycleOwner) {
                println("New quizzes fetched: $it")
                adapter.updateQuizzes(it)
            }
        }

        binding.addFloatingActionButton.setOnClickListener {
            viewModel.clearCurrentQuiz()
            findNavController().navigate(
                QuizDashboardFragmentDirections.actionQuizzesDashboardFragmentToQuizFragment(
                    canEdit = adminMode,
                    canSeeQuestions = moderatorMode
                )
            )
        }

        viewModel.loadQuizzes(showOnlyPublic)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_quizzes, menu)

        (menu.findItem(R.id.action_search).actionView as SearchView).apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filter.filter(newText)
                    return true
                }
            })
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_export -> {
                val csv = viewModel.exportToCsv()
                this.csv = csv
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "text/csv"
                intent.putExtra(Intent.EXTRA_TITLE, "quizzes.csv")
                registerForActivityResult.launch(intent)
                true
            }
            else -> false
        }
    }
}