package com.cornellappdev.android.pollo.polls

import android.animation.ObjectAnimator
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.animation.TranslateAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.android.pollo.R
import com.cornellappdev.android.pollo.models.Poll
import com.cornellappdev.android.pollo.models.PollState
import com.cornellappdev.android.pollo.models.User
import com.cornellappdev.android.pollo.networking.Socket
import com.cornellappdev.android.pollo.networking.SocketDelegate
import com.google.android.gms.auth.api.signin.GoogleSignIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.collections.ArrayList
import kotlinx.android.synthetic.main.activity_polls.*
import kotlinx.android.synthetic.main.manage_group_view.view.*


class PollsActivity : AppCompatActivity(), SocketDelegate, PollsRecyclerAdapter.OnPollOptionsPressedListener {

    private var polls = ArrayList<Poll>()
    private var userCount: Int = 0
    private lateinit var name: String
    private lateinit var code: String
    private lateinit var date: String
    private lateinit var role: User.Role
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: PollsRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_polls)

        val helper = PagerSnapHelper()
        helper.attachToRecyclerView(pollsRecyclerView)

        polls = intent.getParcelableArrayListExtra<Poll>("POLLS")
        name = intent.getStringExtra("GROUP_NAME")
        code = intent.getStringExtra("GROUP_CODE")
        date = intent.getStringExtra("POLLS_DATE")
        userCount = intent.getIntExtra("USER_COUNT", 0)
        role = intent.getSerializableExtra("USER_ROLE") as User.Role
        Socket.add(this)

        groupNameTextView.text = name
        codeTextView.text = "Code: $code"
        currentPollView.text = "1 / ${polls.size}"

        val googleId = GoogleSignIn.getLastSignedInAccount(this)?.id ?: ""

        linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        pollsRecyclerView.layoutManager = linearLayoutManager
        adapter = PollsRecyclerAdapter(polls, googleId, role, this)
        pollsRecyclerView.adapter = adapter

        if (polls[polls.size - 1].state == PollState.live) {
            linearLayoutManager.scrollToPosition(polls.size - 1)
            currentPollView.text = "${polls.size} / ${polls.size}"
        }

        pollsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (linearLayoutManager.findFirstCompletelyVisibleItemPosition() >= 0) {
                    currentPollView.text = "${linearLayoutManager.findFirstCompletelyVisibleItemPosition() + 1} / ${polls.size}"
                }
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        Socket.delete(this)
    }

    private fun renderPoll(poll: Poll) {
        val index = polls.indexOfFirst { it.id == poll.id }
        val indexOfCurrPoll = if (index == -1) polls.size - 1 else index
        polls[indexOfCurrPoll] = poll
        runOnUiThread {
            adapter.notifyItemChanged(indexOfCurrPoll)
        }
    }

    // Socket Delegate
    override fun onPollStart(poll: Poll) {
        val firstCalendar = Calendar.getInstance()
        val secondCalendar = Calendar.getInstance()
        val parser = SimpleDateFormat("MMMM d yyyy")
        firstCalendar.time = parser.parse(date)
        secondCalendar.time = Date()

        val datesSameDay = firstCalendar.get(Calendar.DAY_OF_YEAR) == secondCalendar.get(Calendar.DAY_OF_YEAR) &&
                firstCalendar.get(Calendar.YEAR) == secondCalendar.get(Calendar.YEAR)

        if (!datesSameDay) return // No need to handle a new poll if it is not the same day
        if (polls.isNotEmpty() && poll.id == polls[polls.size - 1].id) return // No need to handle a new poll if it already exists

        polls.add(poll)
        runOnUiThread {
            adapter.notifyItemChanged(polls.size - 1)
            linearLayoutManager.scrollToPosition(polls.size - 1)
            currentPollView.text = "${linearLayoutManager.findFirstCompletelyVisibleItemPosition() + 1} / ${polls.size}"
        }
    }

    override fun onPollEnd(poll: Poll) {
        renderPoll(poll)
    }

    override fun onPollResult(poll: Poll) {
        renderPoll(poll)
    }

    override fun freeResponseUpdates(poll: Poll) {
        println(poll.answerChoices)
        renderPoll(poll)
    }

    override fun onPollDelete(pollID: String) {
        var removePollID = -1
        for (poll in polls){
            if (poll.id == pollID) {
                removePollID = polls.indexOf(poll)
                polls.remove(poll)
                break
            }
        }

        if (removePollID == -1) return

        if (polls.size == 0) {
            finish()
            return
        }

        runOnUiThread {
            adapter.notifyItemRemoved(removePollID)
            adapter.notifyDataSetChanged()
            if (removePollID == polls.size) {
                linearLayoutManager.scrollToPosition(polls.size - 1)
                currentPollView.text = "${polls.size} / ${polls.size}"
            } else{
                currentPollView.text = "${linearLayoutManager.findFirstCompletelyVisibleItemPosition() + 1} / ${polls.size}"
            }
        }
    }

    override fun onPollDeleteLive() {
        polls.removeAt(polls.lastIndex)
        runOnUiThread {
            adapter.notifyItemRemoved(polls.size)
            adapter.notifyDataSetChanged()
            linearLayoutManager.scrollToPosition(polls.size - 1)
            currentPollView.text = "${polls.size} / ${polls.size}"
        }
        if (polls.size == 0) {
            finish()
        }
    }


    override fun onPollStartAdmin(poll: Poll) {
        val firstCalendar = Calendar.getInstance()
        val secondCalendar = Calendar.getInstance()
        val parser = SimpleDateFormat("MMMM d yyyy")
        firstCalendar.time = parser.parse(date)
        secondCalendar.time = Date()

        val datesSameDay = firstCalendar.get(Calendar.DAY_OF_YEAR) == secondCalendar.get(Calendar.DAY_OF_YEAR) &&
                firstCalendar.get(Calendar.YEAR) == secondCalendar.get(Calendar.YEAR)

        if (!datesSameDay) return // No need to handle a new poll if it is not the same day
        if (poll.id == polls[polls.size - 1].id) return // No need to handle a new poll if it already exists

        polls.add(poll)
        runOnUiThread {
            adapter.notifyItemChanged(polls.size - 1)
            linearLayoutManager.scrollToPosition(polls.size - 1)
            currentPollView.text = "${linearLayoutManager.findFirstCompletelyVisibleItemPosition() + 1} / ${polls.size}"
        }
    }

    override fun onPollEndAdmin(poll: Poll) {
        renderPoll(poll)
    }

    override fun onPollUpdateAdmin(poll: Poll) {
        renderPoll(poll)
    }

    // Below is legacy code for Free Response submissions by Austin Astorga. This is not used in Pollo v1.
    override fun freeResponseSubmissionSuccessful() {
        val viewHolder = pollsRecyclerView.findViewHolderForAdapterPosition(polls.size - 1) as PollsRecyclerAdapter.PollHolder
        runOnUiThread {
            // viewHolder.view.questionFREditText.setText("")
        }
    }

    override fun freeResponseSubmissionFailed(pollFilter: Socket.PollFilter) {
        val viewHolder = pollsRecyclerView.findViewHolderForAdapterPosition(polls.size - 1) as PollsRecyclerAdapter.PollHolder
        val spannableText = SpannableString(pollFilter.text ?: "")
        (pollFilter.filter ?: ArrayList()).forEach { wordToFilter ->
            var currEndIndex = 0
            val regexForFilteredWord = "(?i)$wordToFilter".toRegex()
            regexForFilteredWord.findAll(spannableText).forEach { match ->
                val startIndex = spannableText.indexOf(match.value, startIndex = currEndIndex)
                currEndIndex = startIndex + match.value.length
                spannableText.setSpan(ForegroundColorSpan(resources.getColor(R.color.red)), startIndex, currEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        runOnUiThread {
            // viewHolder.view.questionFREditText.setText(spannableText)
        }
    }

    fun goBack(view: View) {
        finish()
    }

    private fun setDim(shouldDim: Boolean) {
        dimView.isClickable = shouldDim
        if (shouldDim) {
            dimView.setOnClickListener {
                closePollOptions()
            }
        }

        val newAlpha = if (shouldDim) 0.5f else 0.0f
        val dimAnimation = ObjectAnimator.ofFloat(dimView, "alpha", newAlpha)
        dimAnimation.duration = 500
        dimAnimation.start()
    }

    private fun closePollOptions() {
        setDim(false)
        val animate = TranslateAnimation(0f, 0f, 0f, pollOptionsView.height.toFloat())
        animate.duration = 300
        animate.fillAfter = true
        pollOptionsView.startAnimation(animate)
        pollOptionsView.visibility = View.INVISIBLE
    }

    override fun onPollOptionsPressed(poll: Poll) {
        setDim(true)
        pollOptionsView.isClickable = true

        pollOptionsView.renameGroup.visibility = View.GONE
        pollOptionsView.removeGroup.visibility = View.VISIBLE
        pollOptionsView.removeGroupText.text = applicationContext.getString(R.string.delete)
        pollOptionsView.removeGroup.removeGroupImage.setImageResource(R.drawable.ic_trash_can)
        pollOptionsView.groupNameTextView.text = "Question: ${linearLayoutManager.findFirstCompletelyVisibleItemPosition() + 1} / ${polls.size}"
        pollOptionsView.visibility = View.VISIBLE
        val animate = TranslateAnimation(0f, 0f, pollOptionsView.height.toFloat(), 0f)
        animate.duration = 300
        animate.fillAfter = true
        pollOptionsView.startAnimation(animate)

        pollOptionsView.removeGroup.setOnClickListener {
            when (poll.state) {
                PollState.live ->  {
                    Socket.deleteLivePoll()
                    onPollDeleteLive()
                }
                else -> {
                    Socket.deleteSavedPoll(poll)
                    onPollDelete(poll.id ?: "")
                }
            }
            closePollOptions()
        }

        pollOptionsView.closeButton.setOnClickListener {
            closePollOptions()
        }
    }
}