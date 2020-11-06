package com.cornellappdev.android.pollo

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.cornellappdev.android.pollo.models.*
import com.cornellappdev.android.pollo.models.PollResult
import com.cornellappdev.android.pollo.networking.*
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.create_poll_onboarding.view.*
import kotlinx.android.synthetic.main.create_poll_options_list_item.view.*
import kotlinx.android.synthetic.main.fragment_create_poll.*
import kotlinx.android.synthetic.main.fragment_create_poll.view.*
import kotlinx.android.synthetic.main.fragment_create_poll.view.groupNameTextView
import kotlinx.android.synthetic.main.manage_group_view.*
import kotlinx.android.synthetic.main.manage_group_view.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("ValidFragment")
class CreatePollFragment : Fragment(), SavedPollAdapter.SavedPollDelegate, SavedPollAdapter.OnSavedPollOptionsPressedListener, CreatePollAdapter.OnPollChoicesDeleteListener {
    var options: ArrayList<String> = arrayListOf()
    var optionsContent: ArrayList<String> = arrayListOf()
    var createPollAdapter: CreatePollAdapter? = null
    private var delegate: CreatePollDelegate? = null
    private var isPopupActive: Boolean = false
    var savedPolls: ArrayList<SavedPoll> = arrayListOf()
    var savedPollAdapter: SavedPollAdapter? = null
    var selectedSavedPoll: SavedPoll? = null
    var currOnboardScreen: Int = -1

    private val preferencesHelper: PreferencesHelper by lazy {
        PreferencesHelper(requireContext())
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_create_poll, container, false)

        options = arrayListOf()
        createPollAdapter = CreatePollAdapter(requireContext(), options, -1, this)
        rootView.poll_options.adapter = createPollAdapter
        resetOptions()

        savedPolls = arrayListOf()
        savedPollAdapter = SavedPollAdapter(requireContext(), savedPolls, this)
        savedPollAdapter?.delegate = this
        rootView.savedPoll.savedPollListView.adapter = savedPollAdapter
        getSavedPolls()

        val addOption = rootView.add_poll_option_button as Button
        val savePoll = rootView.save_poll as Button
        val startPoll = rootView.start_poll as Button

        addOption.setOnClickListener {
            addOptionToList()
            createPollAdapter?.deletable = options.size >= 3
            add_poll_option_button.visibility = if (options.size > 25) View.GONE else View.VISIBLE
            resetPollHeight()

        }

        savePoll.setOnClickListener {
            savePoll()
            resetSavedPollHeight()
            resetPollHeight()

        }

        startPoll.setOnClickListener {
            startPoll(createPollAdapter!!.getCorrectness())
        }

        if (preferencesHelper.displayOnboarding) {
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
        createPollAdapter?.notifyDataSetChanged()
    }

    private fun resetPollHeight() {
        poll_options.layoutParams = (poll_options.layoutParams as RelativeLayout.LayoutParams).apply {
            val displayMetrics = Resources.getSystem().displayMetrics
            val cellHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 58f, displayMetrics).toInt()

            height = cellHeight * poll_options.count - 8 //extra padding in the end

        }
        poll_options.requestLayout()
        midView.requestLayout()
    }

    private fun resetOptions() {
        // ASCII Math, 0 is 'A', going up from there.
        options.clear()
        options.add("Option " + (options.size + 65).toChar())
        options.add("Option " + (options.size + 65).toChar())
        createPollAdapter?.notifyDataSetChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSavedPollsHeader()
        // Setup options menu for saved polls
        groupMenuOptionsView.renameGroup.visibility = View.GONE

        groupMenuOptionsView.closeButton.setOnClickListener { dismissPopup() }

        poll_question.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val questionLength = s?.length
                if (questionLength == 0) word_count.visibility = View.INVISIBLE
                else {
                    word_count.visibility = View.VISIBLE
                    word_count.text = "$questionLength/120"
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        resetPollHeight()
    }


    /**
     * Starts poll and returns to group
     */
    private fun startPoll(correct: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val pollText = if (poll_question.text.toString().isBlank()) getString(R.string.untitled_poll) else poll_question.text.toString()

            val answerChoices = Poll((System.currentTimeMillis() / 1000).toString(), null, null, pollText,
                    ArrayList(), correct, mutableMapOf(), PollState.live)
            for (x in 0 until options.size) {
                answerChoices.answerChoices.add(PollResult(x, options[x], 0))
            }

            val imm = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().applicationWindowToken, 0)

            (activity as PollsDateActivity).startNewPoll(answerChoices)
        }
    }

    // Saved Polls
    private fun resetSavedPollHeight() {
        savedPollListView.layoutParams = (savedPollListView.layoutParams as LinearLayout.LayoutParams).apply {
            val displayMetrics = Resources.getSystem().displayMetrics
            val cellHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 114f, displayMetrics).toInt()

            height = cellHeight * savedPolls.size - 12 //extra padding in the end
            savedPollListView.requestLayout()
            midView.requestLayout()

        }
    }

    private fun savePoll() {
        val text = if (poll_question.text.toString().isBlank()) getString(R.string.untitled_poll) else poll_question.text.toString()
        val pollOptions = arrayListOf<String>()
        pollOptions.addAll(options)
        val savedPoll: SavedPoll

        when (selectedSavedPoll) {
            null -> {
                savedPoll = SavedPoll(text = text, options = pollOptions)
                savedPolls.add(0, savedPoll)
                createSavedPoll(savedPoll)
                savedPollAdapter?.notifyDataSetChanged()
                setSavedPollsHeader()
            }
            else -> {
                savedPoll = selectedSavedPoll!!
                savedPoll.text = text
                savedPoll.options = pollOptions
                updateSavedPoll(savedPoll)
                selectedSavedPoll = null

            }
        }
        poll_question.text.clear()
        resetOptions()
        word_count.visibility = View.INVISIBLE

        val imm = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().applicationWindowToken, 0)
    }

    private fun setSavedPollsHeader() {
        if (savedPolls.size == 0) {
            savedPollHeader?.visibility = View.GONE
        } else {
            savedPollHeader?.visibility = View.VISIBLE
            savedPollHeader?.text = "Saved Polls (${savedPolls.size})"
        }
    }

    private fun getSavedPolls() {
        val getSavedPollsEndpoint = Endpoint.getAllSavedPolls()
        val typeTokenSavedPoll = object : TypeToken<ApiResponse<ArrayList<SavedPoll>>>() {}.type
        CoroutineScope(Dispatchers.IO).launch {
            val getSavedPollResponse = Request.makeRequest<ApiResponse<ArrayList<SavedPoll>>>(
                    getSavedPollsEndpoint.okHttpRequest(),
                    typeTokenSavedPoll
            )

            if (getSavedPollResponse?.success == true) {
                withContext(Dispatchers.Main) {
                    savedPolls.addAll(getSavedPollResponse.data)
                    savedPollAdapter?.notifyDataSetChanged()
                    setSavedPollsHeader()
                    resetSavedPollHeight()

                }
                return@launch
            } else {
                Toast.makeText(requireContext(), "Loading Saved Polls Failed", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    private fun createSavedPoll(savedPoll: SavedPoll) {
        val createSavedPollEndpoint = Endpoint.createSavedPoll(savedPoll)
        val typeTokenSavedPoll = object : TypeToken<ApiResponse<SavedPoll>>() {}.type
        CoroutineScope(Dispatchers.IO).launch {
            val createSavedPollResponse = Request.makeRequest<ApiResponse<SavedPoll>>(
                    createSavedPollEndpoint.okHttpRequest(),
                    typeTokenSavedPoll
            )

            if (createSavedPollResponse?.success == true) {
                savedPolls[0] = createSavedPollResponse.data
                return@launch
            } else {
                Toast.makeText(requireContext(), "Saving Poll Failed", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    private fun updateSavedPoll(savedPoll: SavedPoll) {
        val updateSavedPollEndpoint = Endpoint.updateSavedPoll(savedPoll)
        val typeTokenSavedPoll = object : TypeToken<ApiResponse<SavedPoll>>() {}.type
        CoroutineScope(Dispatchers.IO).launch {
            val createSavedPollResponse = Request.makeRequest<ApiResponse<SavedPoll>>(
                    updateSavedPollEndpoint.okHttpRequest(),
                    typeTokenSavedPoll
            )

            if (createSavedPollResponse?.success == true) {
                for (i in 0 until savedPolls.size) {
                    if (savedPolls[i].id == createSavedPollResponse.data.id) {
                        savedPolls.removeAt(i)
                        savedPolls.add(0, savedPoll)
                        withContext(Dispatchers.Main) {
                            savedPollAdapter?.notifyDataSetChanged()
                        }
                        break
                    }
                }
                return@launch
            } else {
                Toast.makeText(requireContext(), "Saving Poll Failed", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    override fun savedPollSelected(savedPoll: SavedPoll) {
        selectedSavedPoll = savedPoll
        poll_question.setText(savedPoll.text)
        createPollAdapter!!.resetCorrectness()
        options.clear()
        options.addAll(savedPoll.options)
        createPollAdapter?.deletable = options.size >= 3
        createPollAdapter?.notifyDataSetChanged()
        add_poll_option_button.visibility = if (options.size > 25) View.GONE else View.VISIBLE
        resetPollHeight()


    }

    override fun savedPollDeselected() {
        selectedSavedPoll = null
        poll_question.text.clear()
        createPollAdapter!!.resetCorrectness()
        resetOptions()
        resetPollHeight()
        word_count.visibility = View.INVISIBLE

    }

    override fun savedPollDeleted(position: Int) {
        when (savedPolls[position].id) {
            null -> {
                // No id --> doesn't exist on backend, if we're here, we're probably in an error state already
                savedPolls.removeAt(position)
                savedPollAdapter?.notifyDataSetChanged()
                setSavedPollsHeader()
                resetSavedPollHeight()
                resetPollHeight()
            }

            else -> {
                val deleteSavedPollEndpoint = Endpoint.deleteSavedPoll(savedPolls[position].id!!)
                CoroutineScope(Dispatchers.IO).launch {
                    val success = Request.makeRequest(deleteSavedPollEndpoint.okHttpRequest())

                    if (success) {
                        withContext(Dispatchers.Main) {
                            savedPolls.removeAt(position)
                            savedPollAdapter?.notifyDataSetChanged()
                            setSavedPollsHeader()
                            resetSavedPollHeight()
                            resetPollHeight()
                        }
                        return@launch
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete saved poll", Toast.LENGTH_LONG)
                                .show()
                    }
                }
            }
        }
    }

    // ONBOARDING

    private fun setupOnboard(view: View) {
        currOnboardScreen = 0

        view.onboardingView.visibility = View.VISIBLE
        view.headerView.elevation = 0f
        view.footerView.elevation = 0f

        // Converts included XML elements into outlines
        view.start_poll_outline.save_poll.visibility = View.INVISIBLE
        view.save_poll_outline.start_poll.visibility = View.INVISIBLE
        view.start_poll_outline2.save_poll.visibility = View.INVISIBLE
        outlinePollOption(view.option_a_outline, "Option A")
        outlinePollOption(view.option_b_outline, "Option B")
        outlinePollOption(view.autofill_a_outline, "A")
        outlinePollOption(view.autofill_b_outline, "B")
        outlineBubble(view.bubble_outline1)
        outlineBubble(view.bubble_outline2)
    }

    private fun outlinePollOption(view: View, text: String) {
        outlineBubble(view)
        view.setBackgroundResource(R.drawable.rounded_container_outline)
        view.create_poll_options_text.setHintTextColor(Color.WHITE)
        view.create_poll_options_text.hint = text
    }

    private fun outlineBubble(view: View) {
        view.setBackgroundColor(Color.TRANSPARENT)
        view.create_poll_options_text.setHintTextColor(Color.TRANSPARENT)
        view.create_poll_options_item.buttonTintList = ColorStateList.valueOf(Color.WHITE)
    }

    /**
     * Moves through onboarding screens
     */
    private fun displayOnboard(view: ConstraintLayout) {
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

    fun dismissPopup() {
        if (!isPopupActive) return
        isPopupActive = false
        setDim(false)
        val animate = TranslateAnimation(0f, 0f, 0f, groupMenuOptionsView.height.toFloat())
        animate.duration = 300
        animate.fillAfter = true
        groupMenuOptionsView.startAnimation(animate)
        groupMenuOptionsView.visibility = View.GONE
        groupMenuOptionsView.removeGroup.visibility = View.GONE

        if (animate.hasEnded()) {
            headerView.elevation = 4f
            footerView.elevation = 4f

        }
    }


    override fun onSavedPollOptionsPressed(position: Int) {
        if (isPopupActive) return
        isPopupActive = true
        setDim(true)
        groupMenuOptionsView.removeGroup.visibility = View.VISIBLE
        groupMenuOptionsView.groupNameTextView.text = getString(R.string.save_poll_options)
        groupMenuOptionsView.removeGroup.removeGroupImage.setImageResource(R.drawable.ic_trash_can)
        groupMenuOptionsView.removeGroup.removeGroupText.setText(R.string.delete_poll)
        headerView.elevation = 0f
        footerView.elevation = 0f
        groupMenuOptionsView.visibility = View.VISIBLE

        val animate = TranslateAnimation(0f, 0f, groupMenuOptionsView.height.toFloat(), 0f)
        animate.duration = 300
        animate.fillAfter = true
        groupMenuOptionsView.startAnimation(animate)
        groupMenuOptionsView.removeGroup.setOnClickListener {
            // Reset selection
            savedPollAdapter!!.resetSelection(position)
            dismissPopup()

        }
    }

    private fun setDim(shouldDim: Boolean) {
        delegate?.setDim(shouldDim, this)
        // Don't want to be able to open poll views when dimmed

        setSelfDim(shouldDim)
    }

    fun setSelfDim(shouldDim: Boolean) {
        // Don't want to be able to open poll views when dimmed
        dimView_savedPoll.isClickable = shouldDim

        val alphaValue = if (shouldDim) 0.5f else 0.0f
        val dimAnimation = ObjectAnimator.ofFloat(dimView_savedPoll, "alpha", alphaValue)
        dimAnimation.duration = 500
        dimAnimation.start()
    }

    interface CreatePollDelegate {
        /**
         * Should dim any parts of the screen outside of `CreatePollFragment`
         */
        fun setDim(shouldDim: Boolean, createPollFragment: CreatePollFragment)

    }

    override fun onPollChoicesDelete(position: Int) {
        options.removeAt(position)
        for (int in position until options.size) {
            val default = "Option " + (int + 66).toChar()
            if (options[int] == default)
                options[int] = "Option " + (int + 65).toChar()
        }

        val correct = createPollAdapter?.getCorrectness()
        if (correct == position) createPollAdapter?.resetCorrectness()
        if (correct!! > position) createPollAdapter?.decreaseCorrectness()
        createPollAdapter?.deletable = options.size >= 3
        createPollAdapter?.notifyDataSetChanged()
        resetPollHeight()
    }
}



