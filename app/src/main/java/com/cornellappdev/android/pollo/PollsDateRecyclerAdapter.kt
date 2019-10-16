package com.cornellappdev.android.pollo

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.cornellappdev.android.pollo.networking.GetSortedPollsResponse
import com.cornellappdev.android.pollo.polls.PollsActivity
import kotlinx.android.synthetic.main.polls_date_recyclerview_item_row.view.*

class PollsDateRecyclerAdapter(private val polls: ArrayList<GetSortedPollsResponse>, private val code: String, private val name: String) :
        RecyclerView.Adapter<PollsDateRecyclerAdapter.PollsHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollsHolder {
        val inflatedView = parent.inflate(R.layout.polls_date_recyclerview_item_row, false)
        return PollsHolder(inflatedView)
    }

    override fun getItemCount() = polls.size

    override fun onBindViewHolder(holder: PollsHolder, position: Int) {
        val itemPoll = polls[position]
        holder.bindPoll(itemPoll, code, name)
    }

    class PollsHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        private var view = view
        private var poll: GetSortedPollsResponse? = null
        private var code: String? = null
        private var name: String? = null

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val pollsDateActivity = Intent(view.context, PollsActivity::class.java)
            pollsDateActivity.putExtra("POLLS", poll?.polls)
            pollsDateActivity.putExtra("GROUP_NAME", name)
            pollsDateActivity.putExtra("GROUP_CODE", code)
            view.context.startActivity(pollsDateActivity)
        }

        fun bindPoll(poll: GetSortedPollsResponse, code: String, name: String) {
            this.poll = poll
            this.code = code
            this.name = name
            view.pollDateTextView.text = poll.date.removeRange(poll.date.length - 5, poll.date.length)
            view.numQuestionsTextView.text = "${poll.polls.size} Question${if (poll.polls.size == 1) "" else "s"}"
            view.liveIndicator.visibility = View.VISIBLE
        }

        companion object {
            // a key for easier reference to the particular item being used to launch your RecyclerView
            private const val POLL_KEY = "POLL"
        }
    }
}
