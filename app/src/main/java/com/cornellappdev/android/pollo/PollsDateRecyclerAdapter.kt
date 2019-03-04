package com.cornellappdev.android.pollo

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.cornellappdev.android.pollo.Networking.GetSortedPollsResponse
import kotlinx.android.synthetic.main.polls_date_recyclerview_item_row.*
import kotlinx.android.synthetic.main.polls_date_recyclerview_item_row.view.*

class PollsDateRecyclerAdapter(private val polls: ArrayList<GetSortedPollsResponse>):
        RecyclerView.Adapter<PollsDateRecyclerAdapter.PollsHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollsDateRecyclerAdapter.PollsHolder {
        val inflatedView = parent.inflate(R.layout.polls_date_recyclerview_item_row, false)
        return PollsHolder(inflatedView)
    }

    override fun getItemCount() = polls.size

    override fun onBindViewHolder(holder: PollsDateRecyclerAdapter.PollsHolder, position: Int) {
        val itemPoll = polls[position]
        holder.bindPoll(itemPoll)
    }

    class PollsHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {

        private var view = view
        private var poll: GetSortedPollsResponse? = null

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            println("Clicked polls date")
        }

        fun bindPoll(poll: GetSortedPollsResponse) {
            this.poll = poll
            view.pollDateTextView.text = poll.date
            view.numQuestionsTextView.text = "${poll.polls.size} Questions"
            view.liveIndicator.visibility = View.VISIBLE
        }

        companion object {
            // a key for easier reference to the particular item being used to launch your RecyclerView
            private const val POLL_KEY = "POLL"
        }
    }
}
