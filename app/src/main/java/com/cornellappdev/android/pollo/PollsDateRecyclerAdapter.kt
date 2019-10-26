package com.cornellappdev.android.pollo

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.android.pollo.networking.GetSortedPollsResponse
import com.cornellappdev.android.pollo.networking.Socket
import com.cornellappdev.android.pollo.polls.PollsActivity
import kotlinx.android.synthetic.main.polls_date_recyclerview_item_row.view.*

class PollsDateRecyclerAdapter(private var polls: ArrayList<GetSortedPollsResponse>,
                               private val code: String,
                               private val name: String,
                               var userCount: Int) :
        RecyclerView.Adapter<PollsDateRecyclerAdapter.PollsHolder>() {

    fun updatePolls(polls: ArrayList<GetSortedPollsResponse>) {
        val oldSize = this.polls.size
        this.polls = polls
        if (oldSize == polls.size) notifyDataSetChanged() else notifyItemInserted(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollsDateRecyclerAdapter.PollsHolder {
        val inflatedView = parent.inflate(R.layout.polls_date_recyclerview_item_row, false)
        return PollsHolder(inflatedView)
    }

    override fun getItemCount() = polls.size

    override fun onBindViewHolder(holder: PollsDateRecyclerAdapter.PollsHolder, position: Int) {
        val itemPoll = polls[position]
        holder.bindPoll(itemPoll, code, name, userCount)
    }

    class PollsHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        private var view = view
        private var poll: GetSortedPollsResponse? = null
        private var code: String? = null
        private var name: String? = null
        private var userCount = 0

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val pollsDateActivity = Intent(view.context, PollsActivity::class.java)
            pollsDateActivity.putExtra("POLLS", poll?.polls)
            pollsDateActivity.putExtra("POLLS_DATE", poll?.date)
            pollsDateActivity.putExtra("GROUP_NAME", name)
            pollsDateActivity.putExtra("GROUP_CODE", code)
            pollsDateActivity.putExtra("USER_COUNT", userCount)
            view.context.startActivity(pollsDateActivity)
        }

        fun bindPoll(poll: GetSortedPollsResponse, code: String, name: String, userCount: Int) {
            this.poll = poll
            this.code = code
            this.name = name
            this.userCount = userCount
            view.pollDateTextView.text = poll.date.removeRange(poll.date.length - 5, poll.date.length)
            view.numQuestionsTextView.text = "${poll.polls.size} Question${if (poll.polls.size == 1) "" else "s"}"
            view.liveIndicator.visibility = if (poll.isLive) View.VISIBLE else View.INVISIBLE
        }

        companion object {
            // a key for easier reference to the particular item being used to launch your RecyclerView
            private const val POLL_KEY = "POLL"
        }
    }
}