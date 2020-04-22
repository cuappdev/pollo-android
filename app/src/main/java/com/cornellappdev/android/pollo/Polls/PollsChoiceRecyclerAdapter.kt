package com.cornellappdev.android.pollo.polls

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.android.pollo.R
import com.cornellappdev.android.pollo.inflate
import com.cornellappdev.android.pollo.models.Poll
import com.cornellappdev.android.pollo.models.PollChoice
import com.cornellappdev.android.pollo.models.PollState
import com.cornellappdev.android.pollo.models.User
import kotlinx.android.synthetic.main.poll_multiple_choice_item_row.view.*
import com.cornellappdev.android.pollo.networking.Socket
import kotlin.math.roundToInt


class PollsChoiceRecyclerAdapter(private val poll: Poll,
                                 private val googleId: String,
                                 private val role: User.Role) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var positionSelected = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflatedView = parent.inflate(R.layout.poll_multiple_choice_item_row, false)
        return ChoiceHolder(inflatedView)
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun getItemCount(): Int = poll.answerChoices.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val choiceHolder = holder as ChoiceHolder
        choiceHolder.bindPoll(poll, googleId, role)
        if (poll.state == PollState.live && role == User.Role.MEMBER) {
            choiceHolder.view.setOnClickListener { _ ->
                positionSelected = position
                val answerSelected = poll.answerChoices[position]
                choiceHolder.view.answerButton.isChecked = true
                poll.userAnswers?.set(googleId, arrayListOf(PollChoice(letter = answerSelected.letter, text = answerSelected.text)))
                notifyDataSetChanged()
                sendAnswer(position)
            }
        }
    }

    private fun sendAnswer(index: Int) {
        val answerSelected = poll.answerChoices[index];
        val pollChoice = PollChoice(letter = answerSelected.letter, text = answerSelected.text)
        Socket.sendMCAnswer(pollChoice)
    }

    class ChoiceHolder(v: View) : RecyclerView.ViewHolder(v) {

        var view: View = v
        private var poll: Poll? = null
        private var isQuestionLive = false
        private var totalNumberOfResponses = 0

        fun bindPoll(poll: Poll, googleId: String, role: User.Role) {
            this.poll = poll
            this.totalNumberOfResponses = poll.answerChoices.map { it.count ?: 0}.sum()

            if (role == User.Role.MEMBER && poll.state != PollState.shared) {
                // hides responses
                view.answerLinearLayout.visibility = View.GONE
                view.progressBarBorder.background = ContextCompat.getDrawable(view.context, R.drawable.rounded_multiple_choice_cell)
                view.progressBarWrapper.background.level = 0
            } else {
                displayResponses(poll)
                if (role == User.Role.ADMIN) {
                    view.answerButton.visibility = View.GONE
                }
            }

            // Set up answer text
            view.answerTextView.text = poll.answerChoices[adapterPosition].text
            val answerTextColor = if (role == User.Role.ADMIN || poll.state == PollState.live) R.color.black else R.color.darkGray
            view.answerTextView.setTextColor(ContextCompat.getColor(view.context, answerTextColor))

            // See if radio button is checked
            val potentialUserAnswer = poll.userAnswers?.get(googleId)
            if (potentialUserAnswer == null || potentialUserAnswer.size < 1) {
                view.answerButton.isChecked = false
            } else {
                val isSelectedAnswer = potentialUserAnswer.first().letter == poll.answerChoices[adapterPosition].letter
                view.answerButton.isChecked = isSelectedAnswer
            }

            if (!view.answerButton.isChecked) {
                view.answerButton.background = ContextCompat.getDrawable(view.context, R.drawable.unchecked_radio_button)
                return
            }

            // Set radio button background
            when (poll.state) {
                PollState.live -> {
                    view.answerButton.background = ContextCompat.getDrawable(view.context, R.drawable.checked_live_radio_button)
                }

                PollState.ended -> {
                    view.answerButton.background = ContextCompat.getDrawable(view.context, R.drawable.no_correct_radio_button)
                }

                PollState.shared -> {
                    view.answerButton.background =
                        when (poll.correctAnswer) {
                            -1 -> ContextCompat.getDrawable(view.context, R.drawable.no_correct_radio_button)
//                            potentialUserAnswer?.first() -> ContextCompat.getDrawable(view.context, R.drawable.checked_correct_radio_button)
                            else -> ContextCompat.getDrawable(view.context, R.drawable.checked_incorrect_radio_button)
//                    setupFinishedPoll()
//                    val darkGrayColor = ContextCompat.getColor(view.context, R.color.darkGray)
//                    view.answerTextView.setTextColor(darkGrayColor)
//                    view.answerCountTextView.setTextColor(darkGrayColor)
//                    val count = poll.answerChoices[adapterPosition].count ?: 0
//                    val correctAnswer = poll.correctAnswer
//                    if (correctAnswer == -1) {
//                        /* No correct answer. We need to set how much the background is filled based
//                         off the % of people that answered this. the level property goes from 0 to 10000 */
//                        view.progressBarWrapper.background.level =
//                                if (totalNumberOfResponses != 0) ((count.toDouble() / totalNumberOfResponses.toDouble()) * 10000).roundToInt() else 0
//                    } else {
//                        if (adapterPosition == correctAnswer) {
//                            view.progressBarWrapper.background.level = 10000
//                        } else {
//                            view.progressBarWrapper.background.level = 0
                        }
                }
            }
        }

        // Displays count and percentage of responses for this answer choice
        private fun displayResponses(poll: Poll) {
            view.answerLinearLayout.visibility = View.VISIBLE

            val count = poll.answerChoices[adapterPosition].count ?: 0
            view.answerCountTextView.text = count.toString()
            val answerPercentage = if (totalNumberOfResponses != 0) "(${((count.toDouble()/ totalNumberOfResponses) * 100).roundToInt()}%)" else "(0%)"
            view.answerPercentageTextView.text = answerPercentage

            setupProgressBar(poll)
        }

        // Displays progress bar
        private fun setupProgressBar(poll: Poll) {
            val correctAnswer = poll.correctAnswer
            if (poll.answerChoices[adapterPosition].letter == correctAnswer) {
                view.progressBarWrapper.background = ContextCompat.getDrawable(view.context, R.drawable.correct_multiple_choice_progress_fill)
                view.progressBarBorder.background = ContextCompat.getDrawable(view.context, R.drawable.correct_rounded_multiple_choice_cell)
            } else {
                view.progressBarWrapper.background = ContextCompat.getDrawable(view.context, R.drawable.incorrect_multiple_choice_progress_fill)
                view.progressBarBorder.background = ContextCompat.getDrawable(view.context, R.drawable.rounded_multiple_choice_cell)
            }
            /* We need to set how much the background is filled based off the % of people that answered this.
                        the level property goes from 0 to 10000 */
            val count = poll.answerChoices[adapterPosition].count ?: 0
            val level = if (totalNumberOfResponses != 0) ((count.toDouble() / totalNumberOfResponses.toDouble()) * 10000).toInt() else 0
            view.progressBarWrapper.background.level = level
        }

        companion object {
            private val POLL_CHOICE_KEY = "POLL_CHOICE_MULTIPLE"
        }
    }
}