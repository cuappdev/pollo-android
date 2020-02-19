package com.cornellappdev.android.pollo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.cornellappdev.android.pollo.models.Poll
import com.cornellappdev.android.pollo.models.PollResult
import com.cornellappdev.android.pollo.models.PollState
import kotlinx.android.synthetic.main.fragment_create_poll.*
import kotlinx.android.synthetic.main.fragment_create_poll.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("ValidFragment")
class CreatePollFragment : Fragment() {
    var options: ArrayList<String> = arrayListOf()
    var adapter: CreatePollAdapter? = null
    var correct: Int = -1

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_create_poll, container, false)

        options = arrayListOf()
        adapter = CreatePollAdapter(context!!, options, correct, this)
        rootView.poll_options.adapter = adapter
        addOptionToList()
        addOptionToList()

        val addOption = rootView.add_poll_option_button as Button
        val saveDraft = rootView.save_draft as Button
        val startPoll = rootView.start_poll as Button

        addOption.setOnClickListener {
            addOptionToList()
        }

        startPoll.setOnClickListener {
            startPoll(correct)
        }

        return rootView
    }

    private fun addOptionToList() {
        // ASCII Math, 0 is 'A', going up from there.
        options.add("Option " + (options.size + 65).toChar())
        adapter!!.notifyDataSetChanged()
    }

    /**
     * Starts poll and returns to group
     */
    private fun startPoll(correct: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val correctAnwser = if (correct == -1) null else (correct + 65).toChar().toString()
            var answerChoices = Poll((System.currentTimeMillis() / 1000).toString(), null, null, poll_question.text.toString(),
                    ArrayList(), correctAnwser, mutableMapOf(), PollState.live)
            for (x in 0 until options.size) {
                answerChoices.answerChoices.add(PollResult((x + 65).toChar().toString(), options[x], x))
            }

            (activity as PollsDateActivity).startNewPoll(answerChoices)
        }
    }
}