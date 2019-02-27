package com.cornellappdev.android.pollo

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.cornellappdev.android.pollo.Networking.GetSortedPollsResponse
import kotlinx.android.synthetic.main.polls_date_recyclerview_item_row.*
import kotlinx.android.synthetic.main.polls_date_recyclerview_item_row.view.*

class PollsDateRecyclerAdapter(private val polls: ArrayList<GetSortedPollsResponse>): RecyclerView.Adapter<PollsDateRecyclerAdapter.PollsHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollsDateRecyclerAdapter.PollsHolder {
        val inflatedView = parent.inflate(R.layout.polls_date_recyclerview_item_row, false)
        return PollsHolder(inflatedView)
    }

    override fun getItemCount() = polls.size

    override fun onBindViewHolder(holder: PollsDateRecyclerAdapter.PollsHolder, position: Int) {
        val itemPoll = polls[position]
        holder.bindPoll(itemPoll)
    }

    class PollsHolder(v: View): RecyclerView.ViewHolder(v), View.OnClickListener {

        private var view = v
        private var poll: GetSortedPollsResponse? = null

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            println("CLICKKKKK")
        }

        fun bindPoll(poll: GetSortedPollsResponse) {
            this.poll = poll
            view.pollDateTextView.text = poll.date
            view.numQuestionsTextView.text = "${poll.polls.size} Questions"
        }

        companion object {
            // a key for easier reference to the particular item being used to launch your RecyclerView
            private val POLL_KEY = "POLL"
        }
    }
}
