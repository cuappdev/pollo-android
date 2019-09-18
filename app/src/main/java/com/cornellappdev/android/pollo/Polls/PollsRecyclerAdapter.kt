package com.cornellappdev.android.pollo.polls

import android.content.res.Resources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import com.cornellappdev.android.pollo.networking.PollsResponse
import com.cornellappdev.android.pollo.R
import com.cornellappdev.android.pollo.inflate
import kotlinx.android.synthetic.main.poll_recyclerview_item_row.view.*

class PollsRecyclerAdapter(private val polls: ArrayList<PollsResponse>) : androidx.recyclerview.widget.RecyclerView.Adapter<PollsRecyclerAdapter.PollHolder>() {

    private val viewPool = androidx.recyclerview.widget.RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollHolder {
        val inflatedView = parent.inflate(R.layout.poll_recyclerview_item_row, false)
        return PollHolder(inflatedView)
    }

    override fun getItemCount(): Int = polls.size

    override fun onBindViewHolder(holder: PollHolder, position: Int) {

        holder.view.layoutParams = (holder.view.layoutParams as androidx.recyclerview.widget.RecyclerView.LayoutParams).apply {
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
        }

        val poll = polls[position]
        val questionType = QuestionType.create(poll.type) ?: QuestionType.MULTIPLE_CHOICE
        holder.bindPoll(poll, questionType)

        val childLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(holder.view.pollsChoiceRecyclerView.context)
        childLayoutManager.initialPrefetchItemCount = 4
        holder.view.pollsChoiceRecyclerView.apply {
            layoutManager = childLayoutManager
            adapter = PollsChoiceRecyclerAdapter(poll.results, poll.correctAnswer, poll.shared, questionType)
            setRecycledViewPool(viewPool)
        }

    }

    class PollHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v), View.OnClickListener {

        var view: View = v
        private var poll: PollsResponse? = null
        private var questionType: QuestionType? = null

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            println("CLICKED BIG POLL")
        }

        fun bindPoll(poll: PollsResponse, questionType: QuestionType) {
            this.poll = poll
            this.questionType = questionType

            val totalNumberOfResponses = poll.results.map { (_, pollResult) ->
                pollResult.count
            }.sum()
            view.questionMCTextView.text = poll.text

            when (questionType) {
                QuestionType.FREE_RESPONSE -> view.questionMCSubtitleText.text = "Poll Closed"
                QuestionType.MULTIPLE_CHOICE -> {
                    view.questionMCSubtitleText.text = "Final Results  •  $totalNumberOfResponses Vote${if (totalNumberOfResponses == 1) "" else "s"}"
                }
            }
        }

        companion object {
            private val POLL_KEY = "POLL"
        }
    }
}