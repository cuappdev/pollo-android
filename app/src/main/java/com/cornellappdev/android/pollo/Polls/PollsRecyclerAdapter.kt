package com.cornellappdev.android.pollo.polls

import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.cornellappdev.android.pollo.R
import com.cornellappdev.android.pollo.inflate
import com.cornellappdev.android.pollo.networking.Socket
import kotlinx.android.synthetic.main.poll_recyclerview_item_row.view.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.android.pollo.models.Poll
import com.cornellappdev.android.pollo.models.PollState
import com.cornellappdev.android.pollo.models.User
import java.util.*
import kotlin.collections.ArrayList

interface AnswerChoiceDelegate {
    fun sendAnswer(position: Int)
}

class PollsRecyclerAdapter(
        private var polls: ArrayList<Poll>,
        private val userId: String,
        private val role: User.Role,
        val callback: OnPollOptionsPressedListener
) : RecyclerView.Adapter<PollsRecyclerAdapter.PollHolder>(), AnswerChoiceDelegate {

    private val viewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollHolder {
        val inflatedView = parent.inflate(R.layout.poll_recyclerview_item_row, false)
        return PollHolder(inflatedView)
    }

    override fun getItemCount(): Int = polls.size

    override fun onBindViewHolder(holder: PollHolder, position: Int) {

        val poll = polls[position]
        var recyclerviewHeight=0;

        holder.view.layoutParams = (holder.view.layoutParams as RecyclerView.LayoutParams).apply {
            val displayMetrics = Resources.getSystem().displayMetrics

            /* To show the edge of the next/previous card on the screen, we'll adjust the width of our MATCH_PARENT card to make
            it just slightly smaller than the screen. That way, no matter the size of the screen, the card will fill most of
            it and show a hint of the next cards. */
            val widthSubtraction = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, displayMetrics).toInt()
            width = displayMetrics.widthPixels - widthSubtraction
            /* We always want the spot card centered. But the RecyclerView will left-align the first card and right-align the
            last card, since there's no card peeking on that size. We'll adjust the margins in those two places to pad it out
            so those cards appear centered.
            Theoretically we SHOULD be able to just use half of the amount we shrank the card by, but for some reason that's
            not quite right, so I'm adding a fudge factor developed via trial and error to make it look better. */
            val fudgeFactor = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7f, displayMetrics).toInt()
            val endAdjustment = (widthSubtraction / 2) - fudgeFactor
            marginStart = if (position == 0) endAdjustment else 16
            marginEnd = if (position == (itemCount - 1)) endAdjustment else 16

            val headerHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 140f, displayMetrics).toInt()
            val cellHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55f, displayMetrics).toInt()
            val adminControlsHeight = if (role == User.Role.ADMIN) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 85f, displayMetrics).toInt() else 0
            // 86 dp is the height of header, 53dp is height of cell
            val tmpHeight = headerHeight + cellHeight * poll.answerChoices.count() + adminControlsHeight // 53 is cell height including top margin
            height = if (tmpHeight <= 1500) tmpHeight else 1500

            recyclerviewHeight=cellHeight*poll.answerChoices.count();
        }

        holder.bindPoll(poll, this, role)
        holder.view.pollsChoiceRecyclerView.layoutParams= ( holder.view.pollsChoiceRecyclerView.layoutParams
                as ConstraintLayout.LayoutParams).apply {
            height=if(recyclerviewHeight<=600) recyclerviewHeight else 600
        }
        val childLayoutManager = LinearLayoutManager(holder.view.pollsChoiceRecyclerView.context)
        childLayoutManager.initialPrefetchItemCount = 4
        holder.view.pollsChoiceRecyclerView.apply {
            layoutManager = childLayoutManager
            adapter = PollsChoiceRecyclerAdapter(poll, userId, role)
            setRecycledViewPool(viewPool)
        }

    }

    override fun sendAnswer(position: Int) {
        Socket.sendMCAnswer(position)
    }

    inner class PollHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        var view: View = v
        private var poll: Poll? = null
        private var delegate: AnswerChoiceDelegate? = null
        private var role: User.Role? = null

        override fun onClick(v: View) {}

        fun bindPoll(poll: Poll, delegate: AnswerChoiceDelegate, role: User.Role) {
            this.poll = poll
            this.delegate = delegate
            this.role = role

            val totalNumberOfResponses = poll.answerChoices.map { pollResult ->
                pollResult.count ?: 0
            }.sum()
            view.questionMCTextView.text = poll.text

            if (role == User.Role.ADMIN) {
                view.adminPollControlsView.visibility = View.VISIBLE
                view.pollOptionsButton.visibility = View.VISIBLE
                view.poll_timer.visibility = View.VISIBLE
                view.resultsSharedLayout.visibility = View.VISIBLE
                view.adminResponsesCount.visibility = View.VISIBLE
                view.adminResponsesCount.text = "$totalNumberOfResponses Response${if (totalNumberOfResponses == 1) "" else "s"}"
                view.questionMCSubtitleText.visibility = View.GONE
                view.pollOptionsButton.setOnClickListener {
                    callback.onPollOptionsPressed(poll)
                }
            } else {
                view.adminPollControlsView.visibility = View.GONE
                view.pollOptionsButton.visibility = View.GONE
            }

            when (poll.state) {
                PollState.live -> {
                    view.questionMCSubtitleText.text = view.context.getString(R.string.poll_live)
                    if (role == User.Role.ADMIN) {
                        displayAdminLive(poll)
                    }
                }

                PollState.ended -> {
                    view.questionMCSubtitleText.text = view.context.getString(R.string.poll_closed)
                    if (role == User.Role.ADMIN) {
                        displayAdminEnded(poll)
                    }
                }

                PollState.shared -> {
                    view.questionMCSubtitleText.text = "Final Results  •  $totalNumberOfResponses Response${if (totalNumberOfResponses == 1) "" else "s"}"
                    if (role == User.Role.ADMIN) {
                        displayAdminShared()
                    }
                }
            }
        }

        // Sets up timer and end poll controls when poll is live
        private fun displayAdminLive(poll: Poll) {
            displayAdminNotShared()
            val timer = Timer("Poll Timer", false)
            timer.schedule(object : TimerTask() {
                val pollCreatedAt = if (poll.createdAt != null) poll.createdAt.toLong() * 1000 else Date().time
                val start = if (pollCreatedAt < Date().time) pollCreatedAt else Date().time
                override fun run() {
                    val timeElapsed = ((Date().time - start) / 1000)
                    val minutes = timeElapsed / 60
                    val seconds = timeElapsed % 60

                    val secondsText = if (seconds < 10) "0$seconds" else "$seconds"
                    val minutesText = if (minutes < 10) "0$minutes" else "$minutes"
                    val timerText = "$minutesText:$secondsText"
                    view.post {
                        view.poll_timer.text = timerText
                    }
                }
            }, 0, 1000)
            view.end_poll_button.setOnClickListener {
                Socket.serverEnd()
                timer.cancel()
            }
            view.end_poll_button.text = view.context.getString(R.string.end_poll)
        }

        private fun displayAdminEnded(poll: Poll) {
            displayAdminNotShared()
            view.poll_timer.visibility = View.GONE
            view.end_poll_button.text = view.context.getString(R.string.share_results)
            view.end_poll_button.setOnClickListener {
                Socket.shareResults(poll)
                displayAdminShared()
            }
        }

        private fun displayAdminNotShared() {
            view.end_poll_button.isEnabled = true
            view.end_poll_button.background = ContextCompat.getDrawable(view.context, R.drawable.rounded_container_outline)
            view.end_poll_button.setTextColor(ContextCompat.getColor(view.context, R.color.actualWhite))
            view.resultsSharedIcon.setImageResource(R.drawable.results_not_shared)
            view.resultsSharedText.text = view.context.getString(R.string.admin_results_not_shared)
        }

        private fun displayAdminShared() {
            view.poll_timer.visibility = View.GONE
            view.end_poll_button.isEnabled = false
            view.end_poll_button.text = view.context.getString(R.string.results_shared)
            view.end_poll_button.background = ContextCompat.getDrawable(view.context, R.drawable.rounded_cool_grey_container_outline)
            view.end_poll_button.setTextColor(ContextCompat.getColor(view.context, R.color.cool_grey))
            view.resultsSharedIcon.setImageResource(R.drawable.results_shared)
            view.resultsSharedText.text = view.context.getString(R.string.admin_results_shared)
        }
    }

    interface OnPollOptionsPressedListener {
        fun onPollOptionsPressed(poll: Poll)
    }
}
