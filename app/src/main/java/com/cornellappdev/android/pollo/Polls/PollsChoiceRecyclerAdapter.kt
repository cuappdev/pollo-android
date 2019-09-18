package com.cornellappdev.android.pollo.polls

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.cornellappdev.android.pollo.networking.PollResult
import com.cornellappdev.android.pollo.R
import com.cornellappdev.android.pollo.inflate
import kotlinx.android.synthetic.main.poll_free_response_item_row.view.*
import kotlinx.android.synthetic.main.poll_multiple_choice_item_row.view.*
import kotlin.math.roundToInt

data class PollsChoiceModel(val hasCorrectAnswer: Boolean, val correctAnswer: String,
                            val shared: Boolean, val type: QuestionType, val answerChoice: String,
                            val pollResult: PollResult, val totalNumberOfResponses: Int)

class PollsChoiceRecyclerAdapter(private val pollChoices: Map<String, PollResult>, private val correctAnswer: String,
                                 private val shared: Boolean,
                                 private val type: QuestionType) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    private val pollChoiceKeys = ArrayList(pollChoices.keys)
    private val hasCorrectAnswer = correctAnswer != ""
    private val totalNumberOfResponses = pollChoices.map { (_, pollResult) ->
        pollResult.count
    }.sum()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return when (type) {
            QuestionType.MULTIPLE_CHOICE -> {
                val inflatedView = parent.inflate(R.layout.poll_multiple_choice_item_row, false)
                ChoiceHolder(inflatedView)
            }

            QuestionType.FREE_RESPONSE -> {
                val inflatedView = parent.inflate(R.layout.poll_free_response_item_row, false)
                FreeResponseHolder(inflatedView)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (type) {
            QuestionType.MULTIPLE_CHOICE -> 0
            QuestionType.FREE_RESPONSE -> 1
        }
    }

    override fun getItemCount(): Int = pollChoiceKeys.size

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, postion: Int) {
        val answerChoice = pollChoiceKeys[postion]
        val pollResult = pollChoices.getValue(answerChoice)
        val pollsChoiceModel = PollsChoiceModel(hasCorrectAnswer, correctAnswer, shared, type, answerChoice, pollResult, totalNumberOfResponses)
        when (type) {
            QuestionType.MULTIPLE_CHOICE -> {
                val choiceHolder = holder as ChoiceHolder
                choiceHolder.bindPoll(pollsChoiceModel)
            }

            QuestionType.FREE_RESPONSE -> {
                val choiceHolder = holder as FreeResponseHolder
                choiceHolder.bindPoll(pollsChoiceModel)
            }
        }

    }

    class ChoiceHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v), View.OnClickListener {

        var view: View = v
        private var pollsChoiceModel: PollsChoiceModel? = null

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            println("CLICKED CHOICE")
        }

        fun bindPoll(pollsChoiceModel: PollsChoiceModel) {
            this.pollsChoiceModel = pollsChoiceModel
            view.answerTextView.text = pollsChoiceModel.pollResult.text
            view.answerCountTextView.text = "${pollsChoiceModel.pollResult.count}"

            if (pollsChoiceModel.hasCorrectAnswer) {
                if (pollsChoiceModel.correctAnswer == pollsChoiceModel.answerChoice) {
                    view.progressBarWrapper.background.level = 10000
                    val whiteColor = view.context.resources.getColor(R.color.white)
                    view.answerTextView.setTextColor(whiteColor)
                    view.answerCountTextView.setTextColor(whiteColor)
                } else {
                    val grayColor = view.context.resources.getColor(R.color.multipleChoiceIncorrectAnswerColor)
                    view.answerTextView.setTextColor(grayColor)
                    view.answerCountTextView.setTextColor(grayColor)
                }
            } else {
                if (pollsChoiceModel.totalNumberOfResponses != 0) {
                    /* We need to set how much the background is filled based off the % of people that answered this.
                    the level property goes from 0 to 10000 */
                    view.progressBarWrapper.background.level = ((pollsChoiceModel.pollResult.count.toDouble() / pollsChoiceModel.totalNumberOfResponses.toDouble()) * 10000).roundToInt()
                }
            }
        }

        companion object {
            private val POLL_CHOICE_KEY = "POLL_CHOICE_MULTIPLE"
        }
    }

    class FreeResponseHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v), View.OnClickListener {

        var view: View = v
        private var pollsChoiceModel: PollsChoiceModel? = null

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            println("CLICKED CHOICE")
        }

        fun bindPoll(pollsChoiceModel: PollsChoiceModel) {
            this.pollsChoiceModel = pollsChoiceModel
            view.optionTextView.text = pollsChoiceModel.pollResult.text
            view.numberOfUpvotesTextView.text = "${pollsChoiceModel.pollResult.count}"
        }

        companion object {
            private val POLL_CHOICE_KEY = "POLL_CHOICE_RESPONSE"
        }
    }
}