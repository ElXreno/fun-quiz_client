package com.github.elxreno.funquiz_client.ui.quiz

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.github.elxreno.funquiz_client.R
import com.github.elxreno.funquiz_client.data.entity.QuizWithStages
import com.github.elxreno.funquiz_client.databinding.RecyclerviewQuizBinding
import com.github.elxreno.funquiz_client.ui.ClickListener
import com.github.elxreno.funquiz_client.ui.pass_test.PassTestActivity

class QuizAdapter(
    private val context: Context,
    private val onClickListener: ClickListener,
    private val removeOnClickListener: ClickListener,
    private val showOnlyPublic: Boolean,
    private val moderatorMode: Boolean,
    private val canPassTest: Boolean
) :
    RecyclerView.Adapter<QuizAdapter.ViewHolder>(), Filterable {

    private var quizzes = mutableListOf<QuizWithStages>()

    private var quizzesFiltered = mutableListOf<QuizWithStages>()

    inner class ViewHolder(val binding: RecyclerviewQuizBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecyclerviewQuizBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return quizzesFiltered.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.textViewName.text = String.format(
            context.getString(R.string.combiner),
            context.getString(R.string.quiz_name),
            quizzesFiltered[position].quiz.name
        )
        holder.binding.textViewCount.text = String.format(
            context.getString(R.string.combiner),
            context.getString(R.string.quiz_question_count),
            quizzesFiltered[position].quizStages?.sumOf { it.quizQuestions?.size ?: 0 }
        )
        holder.binding.cardViewQuiz.setOnClickListener {
            onClickListener.onPositionClicked(position)
        }
        if (!showOnlyPublic) {
            val id = quizzesFiltered[position].quiz.id
            holder.binding.imageButtonRemove.setOnClickListener {
                removeOnClickListener.onPositionClicked(id)
            }
        } else {
            holder.binding.imageButtonRemove.visibility = View.GONE
        }

        if (canPassTest) {
            holder.binding.buttonTest.setOnClickListener {
                if (quizzesFiltered[position].quizStages?.isEmpty() == true ||
                    quizzesFiltered[position].quizStages?.sumOf { it.quizQuestions?.size ?: 0 } == 0
                ) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.quiz_empty_questions),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                val intent = Intent(context, PassTestActivity::class.java)
                intent.putExtra("quizId", quizzesFiltered[position].quiz.id)
                context.startActivity(intent)
            }
        } else {
            holder.binding.buttonTest.visibility = View.GONE
        }

        if (moderatorMode) {
            holder.binding.buttonCheck.setOnClickListener {
                Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show()
            }
        } else {
            holder.binding.buttonCheck.visibility = View.GONE
        }
    }

    fun updateQuizzes(quizWithStagesList: List<QuizWithStages>?) {
        val iterator = quizzesFiltered.iterator()
        while (iterator.hasNext()) {
            val quiz = iterator.next()
            if (quizWithStagesList?.contains(quiz) == false) {
                val position = quizzesFiltered.indexOf(quiz)
                iterator.remove()
                notifyItemRemoved(position)
            }
        }

        quizWithStagesList?.forEach { quiz ->
            if (!quizzesFiltered.contains(quiz)) {
                quizzesFiltered.add(quiz)
                notifyItemInserted(quizzesFiltered.indexOf(quiz))
            }
        }

        quizWithStagesList?.forEach { quiz ->
            if (
                quizzesFiltered.contains(quiz)
                && quizzesFiltered.indexOf(quiz) != quizWithStagesList.indexOf(quiz)
            ) {
                val index = quizzesFiltered.indexOf(quiz)
                quizzesFiltered[index] = quiz
                notifyItemChanged(index)
            }
        }

        quizzesFiltered.sortBy { it.quiz.id }

        quizzes = quizWithStagesList?.toMutableList() ?: mutableListOf()
    }

    fun getQuizByPosition(position: Int): QuizWithStages {
        return quizzesFiltered[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                quizzesFiltered = if (charSearch.isEmpty()) {
                    quizzes
                } else {
                    val resultList = mutableListOf<QuizWithStages>()
                    for (row in quizzes) {
                        if (row.quiz.name.lowercase().contains(charSearch.lowercase())) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = quizzesFiltered
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                quizzesFiltered = results?.values as MutableList<QuizWithStages>
                notifyDataSetChanged()
            }
        }
    }

}