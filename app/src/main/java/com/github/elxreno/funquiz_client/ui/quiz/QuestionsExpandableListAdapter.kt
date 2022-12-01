package com.github.elxreno.funquiz_client.ui.quiz

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.github.elxreno.funquiz_client.R
import com.github.elxreno.funquiz_client.ui.ClickListener
import com.github.elxreno.funquiz_client.ui.dashboard.DashboardActivity
import com.github.elxreno.funquiz_client.ui.quiz.fragment.AddQuestionFragment
import com.github.elxreno.funquiz_client.ui.quiz.fragment.QuizFragment


class QuestionsExpandableListAdapter(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private var expandableListTitle: List<String>,
    private var expandableListDetail: List<Pair<String, Pair<List<String>, List<String>>>>,
    private val removeOnClickListener: ClickListener,
    private val canEditQuestions: Boolean
) : BaseExpandableListAdapter() {

    private lateinit var questionToReplace: Pair<String, Pair<List<String>, List<String>>>

    override fun getGroupCount(): Int {
        return expandableListTitle.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return 1
    }

    override fun getGroup(groupPosition: Int): Any {
        return expandableListTitle[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return expandableListDetail.elementAt(groupPosition).second.first[childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var view = convertView
        val listTitle = getGroup(groupPosition) as String
        if (view == null) {
            val layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = layoutInflater.inflate(R.layout.question_group, parent, false)
        }
        val listTitleTextView = view!!.findViewById<TextView>(R.id.listTitle)
        listTitleTextView.text = listTitle
        return view
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var view = convertView
        val rightAnswers = expandableListDetail.elementAt(groupPosition).second.first
        val wrongAnswers = expandableListDetail.elementAt(groupPosition).second.second
        if (view == null) {
            val layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = layoutInflater.inflate(R.layout.question_item, parent, false)
        }
        val answersTextView = view!!.findViewById<TextView>(R.id.expandedListItem)
        answersTextView.text = String.format(
            context.getString(R.string.quiz_title_answers),
            rightAnswers.joinToString("; "),
            wrongAnswers.joinToString("; ")
        )
        if (canEditQuestions) {
            answersTextView.setOnClickListener {
                questionToReplace = expandableListDetail.elementAt(groupPosition)

                val addQuestionFragment = AddQuestionFragment(
                    questionToReplace = questionToReplace,
                    onClickListener = object : ClickListener {
                        override fun onPositionClicked(position: Int) {
                            TODO("Not yet implemented")
                        }

                        override fun onSavedClicked(pair: Pair<String, Pair<List<String>, List<String>>>) {
                            expandableListTitle = expandableListTitle.map {
                                if (it == questionToReplace.first) {
                                    pair.first
                                } else {
                                    it
                                }
                            }
                            expandableListDetail = expandableListDetail.map {
                                if (it == questionToReplace) {
                                    pair
                                } else {
                                    it
                                }
                            }
                            notifyDataSetChanged()
                        }
                    }
                )
                addQuestionFragment.show(
                    fragmentManager,
                    "AddQuestionFragment"
                )
            }
            answersTextView.setOnLongClickListener {
                val dialog = AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.remove))
                    .setMessage(context.getString(R.string.remove_question))
                    .setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                        val _expandableListTitle = expandableListTitle.toMutableList()
                        val _expandableListDetail = expandableListDetail.toMutableList()
                        _expandableListTitle.removeAt(groupPosition)
                        _expandableListDetail.removeAt(groupPosition)
                        removeOnClickListener.onPositionClicked(groupPosition)

                        expandableListTitle = _expandableListTitle
                        expandableListDetail = _expandableListDetail
                        notifyDataSetChanged()
                    }
                    .setNegativeButton(context.getString(R.string.no)) { _, _ -> }
                    .create()
                dialog.show()
                true
            }
        }

        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    fun getQuestions(): List<Pair<String, Pair<List<String>, List<String>>>> {
        return expandableListDetail
    }
}