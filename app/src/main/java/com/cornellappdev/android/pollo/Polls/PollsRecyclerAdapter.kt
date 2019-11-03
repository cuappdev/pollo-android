package com.cornellappdev.android.pollo.polls

import android.content.Context
import android.content.res.Resources
import android.text.Layout
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import com.cornellappdev.android.pollo.R
import com.cornellappdev.android.pollo.inflate
import com.cornellappdev.android.pollo.networking.Socket
import kotlinx.android.synthetic.main.poll_recyclerview_item_row.view.*
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.android.pollo.models.Poll
import com.cornellappdev.android.pollo.models.PollChoice
import com.cornellappdev.android.pollo.models.PollState
import com.cornellappdev.android.pollo.models.PollType


interface FreeResponseDelegate {
    fun sendAnswer(position: Int, answer: String)
}

class PollsRecyclerAdapter(private var polls: ArrayList<Poll>,
                           private val googleId: String) : RecyclerView.Adapter<PollsRecyclerAdapter.PollHolder>(), FreeResponseDelegate {

    private val viewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollHolder {
        val inflatedView = parent.inflate(R.layout.poll_recyclerview_item_row, false)
        return PollHolder(inflatedView)
    }

    override fun getItemCount(): Int = polls.size

    override fun onBindViewHolder(holder: PollHolder, position: Int) {

        val poll = polls[position]

        holder.view.layoutParams = (holder.view.layoutParams as RecyclerView.LayoutParams).apply {
            val displayMetrics = Resources.getSystem().displayMetrics

            /* To show the edge of the next/previous card on the screen, we'll adjust the width of our MATCH_PARENT card to make
            it just slightly smaller than the screen. That way, no matter the size of the screen, the card will fill most of
            it and show a hint of the next cards. */
            val widthSubtraction = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, displayMetrics).toInt()
            width = displayMetrics.widthPixels - widthSubtraction
            /* We always want the spot card centered. But the RecyclerView will left-align the first card and right-align the
            last card, since there's no card peeking on that size. We'll adjust the margins in those two places to pad it out
            so those cards appear centered.
            Theoretically we SHOULD be able to just use half of the amount we shrank the card by, but for some reason that's
            not quite right, so I'm adding a fudge factor developed via trial and error to make it look better. */
            val fudgeFactor = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7f, displayMetrics).toInt()
            val endAdjustment = (widthSubtraction / 2) - fudgeFactor
            marginStart = if (position == 0) endAdjustment else 16
            marginEnd = if (position == (itemCount - 1)) endAdjustment else 16

            val headerHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, displayMetrics).toInt()
            val cellHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55f, displayMetrics).toInt()
            //86 dp is the heght of header, 53dp is height of cell
            val tmpHeight = headerHeight + cellHeight*poll.answerChoices.count() //53 is cell height including top margin

            height = if (tmpHeight <= 1250 && poll.type == PollType.multipleChoice) tmpHeight else 1250

            //height = 750
            //height = 1250
        }

        val questionType = poll.type
        holder.bindPoll(poll, questionType, this)

        val childLayoutManager = LinearLayoutManager(holder.view.pollsChoiceRecyclerView.context)
        childLayoutManager.initialPrefetchItemCount = 4
        holder.view.pollsChoiceRecyclerView.apply {
            layoutManager = childLayoutManager
            adapter = PollsChoiceRecyclerAdapter(poll, googleId)
            setRecycledViewPool(viewPool)
        }
    }

    override fun sendAnswer(position: Int, answer: String) {
        val poll = polls[position]
        val pollChoice = PollChoice(letter = null, text = answer)
        Socket.sendMCAnswer(pollChoice)
    }

    override fun sendAnswer(position: Int, answer: String) {
        val poll = polls[position]
        val pollChoice = PollChoice(letter = null, text = answer)
        Socket.sendMCAnswer(pollChoice)
    }

    class PollHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        var view: View = v
        private var poll: Poll? = null
        private var pollType: PollType? = null
        private var delegate: FreeResponseDelegate? = null

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            println("CLICKED BIG POLL")
        }

        fun bindPoll(poll: Poll, questionType: PollType, delegate: FreeResponseDelegate) {
            this.poll = poll
            this.pollType = questionType
            this.delegate = delegate

            val totalNumberOfResponses = poll.answerChoices?.map { pollResult ->
                pollResult.count ?: 0
            }?.sum()
            view.questionMCTextView.text = poll.text

            when (questionType) {
                PollType.freeResponse -> {
                    when(poll.state) {
                        PollState.ended -> {
                            view.questionMCSubtitleText.text = "Poll Closed"
                        }
                        PollState.live -> {
                            view.questionHeaderView.visibility = View.GONE
                            view.questionFRHeaderView.visibility = View.VISIBLE

                            view.questionFRTextView.text = poll.text
                            val newHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 132f, view.resources.displayMetrics);
                            view.questionFRHeaderView.layoutParams.height = newHeight.toInt()
                            val layoutParams = view.pollsChoiceRecyclerView.layoutParams as ConstraintLayout.LayoutParams
                            layoutParams.topToBottom = view.questionFRHeaderView.id
                            view.pollsChoiceRecyclerView.requestLayout()

//                            view.questionFREditText.setOnKeyListener { v, keyCode, event ->
//
//                                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
//                                    val freeResponse = v.questionFREditText.text
//                                    if (!freeResponse.isEmpty()) {
//                                        delegate?.sendAnswer(adapterPosition, freeResponse.toString())
//                                    }
//
//                                    // Clear focus and hide keyboard
//                                    v.questionFREditText.clearFocus()
//                                    val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                                    imm.hideSoftInputFromWindow(v.windowToken, 0)
//                                    true
//                                }
//                                false
//                            }
                        }
                    }




                }
                PollType.multipleChoice -> {
                    when (poll.state) {
                        PollState.live -> {
                            view.questionMCSubtitleText.text = "Live"
                        }

                        PollState.ended -> {
                            view.questionMCSubtitleText.text = "Poll Closed"
                        }

                        PollState.shared -> {
                            view.questionMCSubtitleText.text = "Final Results  •  $totalNumberOfResponses Vote${if (totalNumberOfResponses == 1) "" else "s"}"
                        }
                    }
                }
            }
        }

        companion object {
            private val POLL_KEY = "POLL"
        }
    }
}