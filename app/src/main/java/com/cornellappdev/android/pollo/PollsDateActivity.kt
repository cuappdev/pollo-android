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

class PollsDateActivity : AppCompatActivity(), SocketDelegate {
    private lateinit var adapter: PollsDateRecyclerAdapter
    private lateinit var group: Group
    private lateinit  var linearLayoutManager: LinearLayoutManager
    private var currentUserCount = 0

    private val dateFormatter = SimpleDateFormat("MMMM dd yyyy", Locale.US)
    private var sortedPolls = ArrayList<GetSortedPollsResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_polls_date)
        sortedPolls = groupByDate(intent.getParcelableArrayListExtra<GetSortedPollsResponse>("SORTED_POLLS"))
        group = intent.getParcelableExtra("GROUP_NODE")

        val account = GoogleSignIn.getLastSignedInAccount(this)

        CoroutineScope(Dispatchers.IO).launch {
            val joinGroupEndpoint = Endpoint.joinGroupWithCode(group.code)
            val groupTypeToken = object : TypeToken<ApiResponse<Group>>() {}.type
            val groupResponse = Request.makeRequest<ApiResponse<Group>>(joinGroupEndpoint.okHttpRequest(), groupTypeToken)
            withContext(Dispatchers.Main) {
                Socket.connect(groupResponse?.data?.id ?: 0, User.currentSession.accessToken)
                Socket.add(this@PollsDateActivity)
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            val joinGroupEndpoint = Endpoint.joinGroupWithCode(group.code)
            val groupTypeToken = object : TypeToken<ApiResponse<Group>>() {}.type
            val groupResponse = Request.makeRequest<ApiResponse<Group>>(joinGroupEndpoint.okHttpRequest(), groupTypeToken)
            withContext(Dispatchers.Main) {
                Socket.connect(groupResponse?.data?.id ?: 0, User.currentSession.accessToken)
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

    override fun onPollDelete(pollID: Int) { }

    override fun onPollDeleteLive() { }

    override fun freeResponseSubmissionSuccessful() { }

    override fun freeResponseSubmissionFailed(pollFilter: Socket.PollFilter) {
        println(pollFilter.filter)
    }

    fun createNewPoll(view: View) {
        supportFragmentManager.beginTransaction()
            .add(R.id.create_poll_fragment, CreatePollFragment()).commit()
    }
}
