package com.cornellappdev.android.pollo.polls

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.android.pollo.R
import com.cornellappdev.android.pollo.inflate
import com.cornellappdev.android.pollo.models.Poll
import com.cornellappdev.android.pollo.models.PollChoice
import com.cornellappdev.android.pollo.models.PollState
import com.cornellappdev.android.pollo.models.PollType
import kotlinx.android.synthetic.main.poll_free_response_item_row.view.*
import kotlinx.android.synthetic.main.poll_multiple_choice_item_row.view.*
import com.cornellappdev.android.pollo.networking.Socket
import kotlin.math.roundToInt


class PollsChoiceRecyclerAdapter(private val poll: Poll,
                                 private val googleId: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var positionSelected = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (poll.type) {
            PollType.multipleChoice -> {
                val inflatedView = parent.inflate(R.layout.poll_multiple_choice_item_row, false)
                ChoiceHolder(inflatedView)
            }

            PollType.freeResponse -> {
                val inflatedView = parent.inflate(R.layout.poll_free_response_item_row, false)
                FreeResponseHolder(inflatedView)
            }

            null -> {
                val inflatedView = parent.inflate(R.layout.poll_multiple_choice_item_row, false)
                ChoiceHolder(inflatedView)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (poll.type) {
            PollType.multipleChoice -> 0
            PollType.freeResponse -> 1
            null -> 0
        }
    }

    override fun getItemCount(): Int = poll.answerChoices.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(poll.type) {
            PollType.multipleChoice -> {
                val choiceHolder = holder as ChoiceHolder
                choiceHolder.bindPoll(poll, googleId)
                if (poll.state == PollState.live) {
                    choiceHolder.view.setOnClickListener { view ->
                        positionSelected = position
                        val answerSelected = poll.answerChoices[position]
                        poll.userAnswers?.set(googleId, arrayListOf(PollChoice(letter = answerSelected.letter, text = answerSelected.text)))
                        notifyDataSetChanged()
                        sendAnswer(position)
                    }
                }
            }

            // Below is legacy code for Free Response submissions by Austin Astorga. This is not used in Pollo v1.
            PollType.freeResponse -> {
                val choiceHolder = holder as FreeResponseHolder
                choiceHolder.bindPoll(poll, googleId)
                choiceHolder.view.upvoteImageView.setOnClickListener { view ->
                    // TODO: Clicked on Upvote Action
                }
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

        fun bindPoll(poll: Poll, googleId: String) {
            this.poll = poll
            this.totalNumberOfResponses = poll.answerChoices.map { it.count ?: 0}.sum()

            when (poll.state) {
                PollState.live -> {
                    view.answerTextView.text = poll.answerChoices[adapterPosition].text
                    view.answerTextView.setTextColor(ContextCompat.getColor(view.context, R.color.black))
                    view.answerCountTextView.visibility = View.INVISIBLE

                    val potentialUserAnswer = poll.userAnswers?.get(googleId)

                    if (potentialUserAnswer == null || potentialUserAnswer.size < 1) {
                        view.progressBarWrapper.background.level = 0
                    } else {
                        val level = if (potentialUserAnswer.first().letter == poll.answerChoices[adapterPosition].letter) 10000 else 0
                        view.progressBarWrapper.background.level = level
                    }
                }

                PollState.ended -> {
                    setupFinishedPoll()
                    view.answerCountTextView.text = ""
                    val potentialUserAnswer = poll.userAnswers?.get(googleId)
                    if (potentialUserAnswer == null || potentialUserAnswer.size < 1) {
                        view.progressBarWrapper.background.level = 0
                        view.answerTextView.setTextColor(ContextCompat.getColor(view.context, R.color.multipleChoiceIncorrectAnswerColor))
                    } else {
                        val isSelectedAnswer = potentialUserAnswer.first().letter == poll.answerChoices[adapterPosition].letter
                        val level = if (isSelectedAnswer) 10000 else 0
                        view.progressBarWrapper.background.level = level
                        if (isSelectedAnswer) {
                            view.answerTextView.setTextColor(ContextCompat.getColor(view.context, R.color.actualWhite))
                        } else {
                            view.answerTextView.setTextColor(ContextCompat.getColor(view.context, R.color.multipleChoiceIncorrectAnswerColor))
                        }
                    }
                }

                PollState.shared -> {
                    setupFinishedPoll()
                    val darkGrayColor = ContextCompat.getColor(view.context, R.color.darkGray)
                    view.answerTextView.setTextColor(darkGrayColor)
                    view.answerCountTextView.setTextColor(darkGrayColor)
                    val count = poll.answerChoices[adapterPosition].count ?: 0
                    view.answerCountTextView.visibility = View.VISIBLE
                    view.answerCountTextView.text = if (totalNumberOfResponses != 0) "${(count / totalNumberOfResponses) * 100}%" else "0%"
                    val correctAnswer = poll.correctAnswer
                    if(correctAnswer == "") {
                        /* We need to set how much the background is filled based off the % of people that answered this.
                        the level property goes from 0 to 10000 */
                        view.progressBarWrapper.background.level =
                                if (totalNumberOfResponses != 0) ((count.toDouble() / totalNumberOfResponses.toDouble()) * 10000).roundToInt() else 0
                    } else {
                        if (poll.answerChoices[adapterPosition].letter == correctAnswer) {
                            view.progressBarWrapper.background.level = 10000
                        } else {
                            view.progressBarWrapper.background.level = 0
                        }
                    }
                }
            }
        }

        private fun setupFinishedPoll() {
            val currPoll = poll ?: return
            view.answerTextView.text = currPoll.answerChoices[adapterPosition].text

        }

        companion object {
            private val POLL_CHOICE_KEY = "POLL_CHOICE_MULTIPLE"
        }
    }

    // Below is legacy code for Free Response submissions by Austin Astorga. This is not used in Pollo v1.
    class FreeResponseHolder(v: View) : RecyclerView.ViewHolder(v) {

        var view: View = v
        private var poll: Poll? = null

        fun bindPoll(poll: Poll, googleId: String) {
            this.poll = poll

            when(poll.state) {
                PollState.live -> {
                    view.optionTextView.text = poll.answerChoices[adapterPosition].text
                }

                PollState.ended -> {

                }

                PollState.shared -> {
                    view.optionTextView.text = poll.answerChoices[adapterPosition].text
                    view.numberOfUpvotesTextView.text = "${poll.answerChoices[adapterPosition].count ?: 0}"
                }
            }
        }

        companion object {
            private val POLL_CHOICE_KEY = "POLL_CHOICE_RESPONSE"
        }
    }
}