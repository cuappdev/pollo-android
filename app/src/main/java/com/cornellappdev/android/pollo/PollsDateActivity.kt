package com.cornellappdev.android.pollo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import com.cornellappdev.android.pollo.models.Group
import com.cornellappdev.android.pollo.models.User
import com.cornellappdev.android.pollo.networking.GetSortedPollsResponse
import com.cornellappdev.android.pollo.networking.PollsResponse
import com.cornellappdev.android.pollo.networking.Socket
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.android.synthetic.main.activity_polls_date.*
import kotlinx.android.synthetic.main.activity_polls_date.noPollsView
import kotlinx.android.synthetic.main.fragment_main.*
import java.text.SimpleDateFormat
import java.util.*

class PollsDateActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var adapter: PollsDateRecyclerAdapter
    private lateinit var group: Group
    private lateinit var linearLayoutManager: androidx.recyclerview.widget.LinearLayoutManager
    private lateinit var role: User.Role
    private lateinit var socket: Socket

    private var sortedPolls = ArrayList<GetSortedPollsResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_polls_date)
        sortedPolls = groupByDate(intent.getParcelableArrayListExtra<GetSortedPollsResponse>("SORTED_POLLS"))
        group = intent.getParcelableExtra("GROUP_NODE")

        backButton.setOnClickListener(this)

        // Customize page for role
        role = intent.getSerializableExtra("USER_ROLE") as User.Role

        when (role) {
            User.Role.ADMIN -> {
                noPollsTitle.setText(R.string.no_polls_created_title)
                noPollsSubtext.setText(R.string.no_polls_created_subtext)
            }
            User.Role.MEMBER -> {
                adminFooter.visibility = View.GONE
                newPollImageButton.visibility = View.GONE
            }
        }

        toggleEmptyState()

        // Use LinearLayoutManager because we just want one cell per row
        linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
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

    override fun onClick(view: View) {
        if (view.id == R.id.backButton)
            goBack(view)
    }

    private fun toggleEmptyState() {
        if (sortedPolls.count() == 0) {
            noPollsView.visibility = View.VISIBLE
            adminFooter.visibility = View.GONE
        } else {
            noPollsView.visibility = View.GONE
            if (role == User.Role.ADMIN) {
                adminFooter.visibility = View.VISIBLE
            }
        }
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
