package com.cornellappdev.android.pollo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cornellappdev.android.pollo.models.ApiResponse
import com.cornellappdev.android.pollo.models.Group
import com.cornellappdev.android.pollo.models.Poll
import com.cornellappdev.android.pollo.models.User
import com.cornellappdev.android.pollo.networking.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_polls_date.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class PollsDateActivity : AppCompatActivity(), SocketDelegate, View.OnClickListener {
    private lateinit var adapter: PollsDateRecyclerAdapter
    private lateinit var group: Group
    private lateinit  var linearLayoutManager: LinearLayoutManager
    private lateinit var role: User.Role
    private var currentUserCount = 0

    private val dateFormatter = SimpleDateFormat("MMMM dd yyyy", Locale.US)
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

        val account = GoogleSignIn.getLastSignedInAccount(this)

        CoroutineScope(Dispatchers.IO).launch {
            val joinGroupEndpoint = Endpoint.joinGroupWithCode(group.code)
            val groupTypeToken = object : TypeToken<ApiResponse<Group>>() {}.type
            val groupResponse = Request.makeRequest<ApiResponse<Group>>(joinGroupEndpoint.okHttpRequest(), groupTypeToken)
            withContext(Dispatchers.Main) {
                Socket.connect(groupResponse?.data?.id ?: "", User.currentSession.accessToken)
                Socket.add(this@PollsDateActivity)
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            val joinGroupEndpoint = Endpoint.joinGroupWithCode(group.code)
            val groupTypeToken = object : TypeToken<ApiResponse<Group>>() {}.type
            val groupResponse = Request.makeRequest<ApiResponse<Group>>(joinGroupEndpoint.okHttpRequest(), groupTypeToken)
            withContext(Dispatchers.Main) {
                Socket.connect(groupResponse?.data?.id ?: "0", User.currentSession.accessToken)
                Socket.add(this@PollsDateActivity)
            }
        }

        // Use LinearLayoutManager because we just want one cell per row
        linearLayoutManager = LinearLayoutManager(this)
        pollsDateRecyclerView.layoutManager = linearLayoutManager
        adapter = PollsDateRecyclerAdapter(sortedPolls, group.code, group.name, currentUserCount)
        pollsDateRecyclerView.adapter = adapter

        groupNameTextView.text = group.name
        codeTextView.text = "Code: ${group.code}"
    }

    fun goBack(view: View) {
        finish()
        Socket.disconnect()
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
            if (role == User.Role.ADMIN) adminFooter.visibility = View.VISIBLE
        }
    }

    fun groupByDate(sortedPolls: ArrayList<GetSortedPollsResponse>): ArrayList<GetSortedPollsResponse> {
        var dateToPolls = HashMap<String, ArrayList<Poll>>()
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

    // Socket Delegate

    override fun onPollStart(poll: Poll) {
        val currentDate = dateFormatter.format(Date())
        val datesForPolls = sortedPolls.map { it.date }

        // Avoid index out of bounds exceptions
        if (sortedPolls.isEmpty()) return

        for (p in sortedPolls[0].polls){
            // Don't add duplicate polls
            if (p.id == poll.id) return
        }

        if(datesForPolls.contains(currentDate)) {
            sortedPolls[0].isLive = true
            sortedPolls[0].polls.add(poll)
        } else {
            val newPollDate = GetSortedPollsResponse(currentDate, arrayListOf(poll), isLive = true)
            sortedPolls.add(newPollDate)
            sortedPolls.sortByDescending { dateFormatter.parse(it.date) }
        }
        runOnUiThread { adapter.updatePolls(sortedPolls) }

        val pollResponse = poll
    }

    override fun onPollEnd(poll: Poll) {
        // Replace the old live poll with the new, now ended, poll
        val pollsForToday = sortedPolls[0].polls.map { currPoll ->
            if (poll.id == currPoll.id) poll else currPoll
        }

        sortedPolls[0].polls = ArrayList(pollsForToday)
        sortedPolls[0].isLive = false

        runOnUiThread { adapter.updatePolls(sortedPolls) }
    }

    override fun onPollResult(poll: Poll) { }

    override fun freeResponseUpdates(poll: Poll) { }

    override fun onPollDelete(pollID: String) { }

    override fun onPollDeleteLive() { }

    override fun freeResponseSubmissionSuccessful() { }

    override fun freeResponseSubmissionFailed(pollFilter: Socket.PollFilter) {
        println(pollFilter.filter)
    }

    fun openNewPollFragment(view: View) {
        supportFragmentManager.beginTransaction()
            .add(R.id.create_poll_fragment, CreatePollFragment()).commit()
    }

//    fun createNewPoll(view: View) {
//        val endpoint = Endpoint.startPoll(text, answerChoices, correctAnswer, type)
//        CoroutineScope(Dispatchers.IO).launch {
//
//        }
//    }
}
