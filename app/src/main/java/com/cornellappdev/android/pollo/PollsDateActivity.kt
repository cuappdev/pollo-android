package com.cornellappdev.android.pollo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.cornellappdev.android.pollo.models.Group
import com.cornellappdev.android.pollo.networking.GetSortedPollsResponse
import com.cornellappdev.android.pollo.networking.PollsResponse
import com.cornellappdev.android.pollo.networking.Socket
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.android.synthetic.main.activity_polls_date.*
import java.text.SimpleDateFormat
import java.util.*

class PollsDateActivity : AppCompatActivity() {

    private lateinit var adapter: PollsDateRecyclerAdapter
    private lateinit var group: Group
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var socket: Socket

    private var sortedPolls = ArrayList<GetSortedPollsResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_polls_date)
        sortedPolls = groupByDate(intent.getParcelableArrayListExtra<GetSortedPollsResponse>("SORTED_POLLS"))
        group = intent.getParcelableExtra("GROUP_NODE")

        // Use LinearLayoutManager because we just want one cell per row
        linearLayoutManager = LinearLayoutManager(this)
        pollsDateRecyclerView.layoutManager = linearLayoutManager
        adapter = PollsDateRecyclerAdapter(sortedPolls, group.name, group.code)
        pollsDateRecyclerView.adapter = adapter

        groupNameTextView.text = group.name
        codeTextView.text = "Code: ${group.code}"

        val account = GoogleSignIn.getLastSignedInAccount(this)
        socket = Socket(id = group.id, googleUserID = account?.id ?: "")
    }

    fun goBack(view: View) {
        finish()
    }

    fun groupByDate(sortedPolls: ArrayList<GetSortedPollsResponse>): ArrayList<GetSortedPollsResponse> {
        val dateFormatter = SimpleDateFormat("MMMM dd yyyy", Locale.US)
        var dateToPolls = HashMap<String, ArrayList<PollsResponse>>()
        sortedPolls.forEach { poll ->
            val dateForPoll = Date(poll.date.toLong() * 1000)
            val dateAsString = dateFormatter.format(dateForPoll)

            if (dateToPolls.containsKey(dateAsString)) {
                val currListOfPolls = dateToPolls[dateAsString] ?: throw RuntimeException("Key does not exist")
                val updatedListOfPolls = ArrayList(currListOfPolls + poll.polls)
                dateToPolls[dateAsString] = updatedListOfPolls
            } else {
                dateToPolls[dateAsString] = poll.polls
            }
        }

        var newSortedPollsList = ArrayList<GetSortedPollsResponse>()

        for (entry in dateToPolls) {
            newSortedPollsList.add(GetSortedPollsResponse(date = entry.key, polls = entry.value))
        }

        newSortedPollsList.sortByDescending { dateFormatter.parse(it.date) }

        return newSortedPollsList
    }
}
