package com.cornellappdev.android.pollo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Window
import com.cornellappdev.android.pollo.Models.Group
import com.cornellappdev.android.pollo.Networking.GetSortedPollsResponse
import kotlinx.android.synthetic.main.activity_polls_date.*

class PollsDateActivity : AppCompatActivity() {

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: PollsDateRecyclerAdapter
    private lateinit var group: Group

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
    }

    override fun onStart() {
        super.onStart()
    }
}
