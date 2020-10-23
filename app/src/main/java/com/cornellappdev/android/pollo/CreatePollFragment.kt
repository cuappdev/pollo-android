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
class CreatePollFragment : Fragment(), DraftAdapter.DraftsDelegate, DraftAdapter.OnDraftOptionsPressedListener, CreatePollAdapter.OnPollChoicesDeleteListener {
    var options: ArrayList<String> = arrayListOf()
    var optionsContent: ArrayList<String> = arrayListOf()
    var createPollAdapter: CreatePollAdapter? = null
    private var delegate: CreatePollDelegate? = null
    private var isPopupActive: Boolean = false
    var drafts: ArrayList<Draft> = arrayListOf()
    var draftAdapter: DraftAdapter? = null
    var selectedDraft: Draft? = null
    var correct: Int = -1
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
        createPollAdapter = CreatePollAdapter(requireContext(), options, correct, this, this)
        rootView.poll_options.adapter = createPollAdapter
        resetOptions()
        rootView.poll_options.adapter = createPollAdapter

        drafts = arrayListOf()
        draftAdapter = DraftAdapter(requireContext(), drafts, this)
        draftAdapter?.delegate = this
        rootView.drafts.draftsListView.adapter = draftAdapter
        getDrafts()

        val addOption = rootView.add_poll_option_button as Button
        val saveDraft = rootView.save_draft as Button
        val startPoll = rootView.start_poll as Button

        addOption.setOnClickListener {
            addOptionToList()
            if (options.size > 2) createPollAdapter?.deletable = true
            resetPollHeight()

        }

        saveDraft.setOnClickListener {
            saveDraft()
            resetDraftHeight()
            resetPollHeight()

        }

        startPoll.setOnClickListener {
            startPoll(correct)
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
        setDraftsHeader()
        // Setup options menu for drafts
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
        if (selectedDraft != null) {
            for (i in 0 until drafts.size) {
                if (drafts[i].id == selectedDraft!!.id) {
                    draftDeleted(i)
                    break
                }
            }
        }

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

    // DRAFTS
    private fun resetDraftHeight() {
        draftsListView.layoutParams = (draftsListView.layoutParams as LinearLayout.LayoutParams).apply {
            val displayMetrics = Resources.getSystem().displayMetrics
            val cellHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 114f, displayMetrics).toInt()

            height = cellHeight * drafts.size - 12 //extra padding in the end
            draftsListView.requestLayout()
            midView.requestLayout()

        }
    }

    private fun saveDraft() {
        val text = if (poll_question.text.toString().isBlank()) getString(R.string.untitled_poll) else poll_question.text.toString()
        val draftOptions = arrayListOf<String>()
        draftOptions.addAll(options)
        val draft: Draft

        when (selectedDraft) {
            null -> {
                draft = Draft(text = text, options = draftOptions)
                drafts.add(0, draft)
                createDraft(draft)
                draftAdapter?.notifyDataSetChanged()
                setDraftsHeader()

            }
            else -> {
                draft = selectedDraft!!
                draft.text = text
                draft.options = draftOptions
                updateDraft(draft)
                selectedDraft = null

            }
        }
        poll_question.text.clear()
        resetOptions()
        word_count.visibility = View.INVISIBLE

        val imm = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().applicationWindowToken, 0)
    }

    private fun setDraftsHeader() {
        if (drafts.size == 0) {
            draftsHeader?.visibility = View.GONE
        } else {
            draftsHeader?.visibility = View.VISIBLE
            draftsHeader?.text = "Drafts (${drafts.size})"
        }
    }

    private fun getDrafts() {
        val getDraftsEndpoint = Endpoint.getAllDrafts()
        val typeTokenDrafts = object : TypeToken<ApiResponse<ArrayList<Draft>>>() {}.type
        CoroutineScope(Dispatchers.IO).launch {
            val getDraftsResponse = Request.makeRequest<ApiResponse<ArrayList<Draft>>>(
                    getDraftsEndpoint.okHttpRequest(),
                    typeTokenDrafts
            )

            if (getDraftsResponse?.success == true) {
                withContext(Dispatchers.Main) {
                    drafts.addAll(getDraftsResponse.data)
                    draftAdapter?.notifyDataSetChanged()
                    setDraftsHeader()
                    resetDraftHeight()

                }
                return@launch
            } else {
                Toast.makeText(requireContext(), "Loading Drafts Failed", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    private fun createDraft(draft: Draft) {
        val createDraftEndpoint = Endpoint.createDraft(draft)
        val typeTokenDraft = object : TypeToken<ApiResponse<Draft>>() {}.type
        CoroutineScope(Dispatchers.IO).launch {
            val createDraftResponse = Request.makeRequest<ApiResponse<Draft>>(
                    createDraftEndpoint.okHttpRequest(),
                    typeTokenDraft
            )

            if (createDraftResponse?.success == true) {
                drafts[0] = createDraftResponse.data
                return@launch
            } else {
                Toast.makeText(requireContext(), "Saving Draft Failed", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    private fun updateDraft(draft: Draft) {
        val updateDraftEndpoint = Endpoint.updateDraft(draft)
        val typeTokenDraft = object : TypeToken<ApiResponse<Draft>>() {}.type
        CoroutineScope(Dispatchers.IO).launch {
            val createDraftResponse = Request.makeRequest<ApiResponse<Draft>>(
                    updateDraftEndpoint.okHttpRequest(),
                    typeTokenDraft
            )

            if (createDraftResponse?.success == true) {
                for (i in 0 until drafts.size) {
                    if (drafts[i].id == createDraftResponse.data.id) {
                        drafts.removeAt(i)
                        drafts.add(0, draft)
                        withContext(Dispatchers.Main) {
                            draftAdapter?.notifyDataSetChanged()
                        }
                        break
                    }
                }
                return@launch
            } else {
                Toast.makeText(requireContext(), "Saving Draft Failed", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    override fun draftSelected(draft: Draft) {
        selectedDraft = draft
        poll_question.setText(draft.text)
        options.clear()
        options.addAll(draft.options)
        createPollAdapter?.notifyDataSetChanged()
        resetPollHeight()

    }

    override fun draftDeselected() {
        selectedDraft = null
        poll_question.text.clear()
        resetOptions()
        resetPollHeight()
        word_count.visibility = View.INVISIBLE

    }

    override fun draftDeleted(position: Int) {
        when (drafts[position].id) {
            null -> {
                // No id --> doesn't exist on backend, if we're here, we're probably in an error state already
                drafts.removeAt(position)
                draftAdapter?.notifyDataSetChanged()
                setDraftsHeader()
                resetDraftHeight()
                resetPollHeight()
            }

            else -> {
                val deleteDraftEndpoint = Endpoint.deleteDraft(drafts[position].id!!)
                CoroutineScope(Dispatchers.IO).launch {
                    val success = Request.makeRequest(deleteDraftEndpoint.okHttpRequest())

                    if (success) {
                        withContext(Dispatchers.Main) {
                            drafts.removeAt(position)
                            draftAdapter?.notifyDataSetChanged()
                            setDraftsHeader()
                            resetDraftHeight()
                            resetPollHeight()
                        }
                        return@launch
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete draft", Toast.LENGTH_LONG)
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


    override fun onDraftOptionsPressed(position: Int) {
        if (isPopupActive) return
        isPopupActive = true
        setDim(true)
        groupMenuOptionsView.removeGroup.visibility = View.VISIBLE
        groupMenuOptionsView.groupNameTextView.text = getString(R.string.draft_options)
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
            draftAdapter!!.resetSelection(position)
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
        dimView_draft.isClickable = shouldDim

        val alphaValue = if (shouldDim) 0.5f else 0.0f
        val dimAnimation = ObjectAnimator.ofFloat(dimView_draft, "alpha", alphaValue)
        dimAnimation.duration = 500
        dimAnimation.start()
    }

    interface CreatePollDelegate {
        /**
         * Should dim any parts of the screen outside of `CreatePollFragment`
         */
        fun setDim(shouldDim: Boolean, createPollFragment: CreatePollFragment)

    }
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
        if (options.size < 3) createPollAdapter?.deletable = false

        createPollAdapter?.notifyDataSetChanged()
        resetPollHeight()
    }

}
