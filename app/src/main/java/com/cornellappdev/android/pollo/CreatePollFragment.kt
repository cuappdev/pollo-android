package com.cornellappdev.android.pollo

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.cornellappdev.android.pollo.models.Poll
import com.cornellappdev.android.pollo.models.PollResult
import com.cornellappdev.android.pollo.models.PollState
import kotlinx.android.synthetic.main.create_poll_onboarding.view.*
import kotlinx.android.synthetic.main.create_poll_options_list_item.view.*
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
    var currOnboardScreen: Int = -1

    private val preferencesHelper: PreferencesHelper by lazy {
        PreferencesHelper(context!!)
    }

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

        if (!preferencesHelper.displayOnboarding) {
            val onboardingView = rootView.onboardingView as ConstraintLayout
            setupOnboard(rootView)
            onboardingView.setOnClickListener {
                displayOnboard(onboardingView)
            }
            preferencesHelper.displayOnboarding = false
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
            val correctAnswer = if (correct == -1) null else (correct + 65).toChar().toString()
            var answerChoices = Poll((System.currentTimeMillis() / 1000).toString(), null, null, poll_question.text.toString(),
                    ArrayList(), correctAnswer, mutableMapOf(), PollState.live)
            for (x in 0 until options.size) {
                answerChoices.answerChoices.add(PollResult((x + 65).toChar().toString(), options[x], x))
            }

            (activity as PollsDateActivity).startNewPoll(answerChoices)
        }
    }

    private fun setupOnboard(view : View) {
        currOnboardScreen = 0

        view.onboardingView.visibility = View.VISIBLE
        view.headerView.elevation = 0f
        view.footerView.elevation = 0f

        // Converts included XML elements into outlines
        view.start_poll_outline.save_draft.visibility = View.INVISIBLE
        view.save_draft_outline.start_poll.visibility = View.INVISIBLE
        view.start_poll_outline2.save_draft.visibility = View.INVISIBLE
        outlinePollOption(view.option_a_outline, "Option A")
        outlinePollOption(view.option_b_outline, "Option B")
        outlinePollOption(view.autofill_a_outline, "A")
        outlinePollOption(view.autofill_b_outline, "B")
        outlineBubble(view.bubble_outline1)
        outlineBubble(view.bubble_outline2)
    }

    private fun outlinePollOption(view : View, text : String) {
        outlineBubble(view)
        view.setBackgroundResource(R.drawable.rounded_container_outline)
        view.create_poll_options_text.setHintTextColor(Color.WHITE)
        view.create_poll_options_text.hint = text
    }

    private fun outlineBubble(view : View) {
        view.setBackgroundColor(Color.TRANSPARENT)
        view.create_poll_options_text.setHintTextColor(Color.TRANSPARENT)
        view.create_poll_options_item.buttonTintList = ColorStateList.valueOf(Color.WHITE)
    }

    /**
     * Moves through onboarding screens
     */
    private fun displayOnboard(view : ConstraintLayout) {
        view.getChildAt(currOnboardScreen).visibility = View.GONE
        currOnboardScreen++
        if (currOnboardScreen < view.childCount) {
            view.getChildAt(currOnboardScreen).visibility = View.VISIBLE
        } else {
            onboardingView.visibility = View.GONE
            headerView.elevation = 4f
            footerView.elevation = 4f
        }
    }
}