package com.cornellappdev.android.pollo.polls

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.view.View
import com.cornellappdev.android.pollo.networking.PollsResponse
import com.cornellappdev.android.pollo.R
import kotlinx.android.synthetic.main.activity_polls.*

class PollsActivity : AppCompatActivity() {

    private var polls = ArrayList<PollsResponse>()
    private lateinit var name: String
    private lateinit var code: String
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: PollsRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_polls)

        val helper = PagerSnapHelper()
        helper.attachToRecyclerView(pollsRecyclerView)

        polls = intent.getParcelableArrayListExtra<PollsResponse>("POLLS")
        name = intent.getStringExtra("GROUP_NAME")
        code = intent.getStringExtra("GROUP_CODE")

        groupNameTextView.text = name
        codeTextView.text = "Code: $code"

        linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        pollsRecyclerView.layoutManager = linearLayoutManager
        adapter = PollsRecyclerAdapter(polls)
        pollsRecyclerView.adapter = adapter


    }

    fun goBack(view: View) {
        finish()
    }
}
