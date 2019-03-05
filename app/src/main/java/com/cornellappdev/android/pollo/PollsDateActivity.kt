package com.cornellappdev.android.pollo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.cornellappdev.android.pollo.Models.Group
import com.cornellappdev.android.pollo.Networking.GetSortedPollsResponse
import com.cornellappdev.android.pollo.Networking.Socket
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.android.synthetic.main.activity_polls_date.*

class PollsDateActivity : AppCompatActivity() {

    private lateinit var adapter: PollsDateRecyclerAdapter
    private lateinit var group: Group
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var socket: Socket

    private var sortedPolls = ArrayList<GetSortedPollsResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_polls_date)
        sortedPolls = intent.getParcelableArrayListExtra<GetSortedPollsResponse>("SORTED_POLLS")
        group = intent.getParcelableExtra("GROUP_NODE")

        // Use LinearLayoutManager because we just want one cell per row
        linearLayoutManager = LinearLayoutManager(this)
        pollsDateRecyclerView.layoutManager = linearLayoutManager
        adapter = PollsDateRecyclerAdapter(sortedPolls)
        pollsDateRecyclerView.adapter = adapter

        groupNameTextView.text = group.name
        codeTextView.text = "Code: ${group.code}"

        val account = GoogleSignIn.getLastSignedInAccount(this)
        socket = Socket(id = group.id, googleUserID = account?.id ?: "")
    }

    fun goBack(view: View) {
        finish()
    }
}
