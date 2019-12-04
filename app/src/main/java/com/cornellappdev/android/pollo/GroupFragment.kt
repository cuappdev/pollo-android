package com.cornellappdev.android.pollo

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.android.pollo.models.ApiResponse
import com.cornellappdev.android.pollo.models.Group
import com.cornellappdev.android.pollo.models.GroupCode
import com.cornellappdev.android.pollo.models.User
import com.cornellappdev.android.pollo.networking.*
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.manage_group_view.*
import kotlinx.android.synthetic.main.manage_group_view.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [GroupFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [GroupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@SuppressLint("ValidFragment")
class GroupFragment : Fragment(), GroupRecyclerAdapter.OnMoreButtonPressedListener {

    private var sectionNumber: Int = 0
    private var currentAdapter: GroupRecyclerAdapter? = null
    private val fragmentInteractionListener: OnFragmentInteractionListener? = null
    private var delegate: GroupFragmentDelegate? = null

    private var isPopupActive: Boolean = false
    private var role: User.Role? = null
    private var groups = ArrayList<Group>()
    private var groupSelected: Group? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.fragment_main, container, false)
        role = arguments?.getSerializable(GROUP_ROLE) as User.Role

        val groupRecyclerView = rootView.findViewById<RecyclerView>(R.id.group_list_recyclerView)
        groupRecyclerView.layoutManager = LinearLayoutManager(rootView.context)

        if (role != null) {
            currentAdapter = GroupRecyclerAdapter(groups, this, role!!)
            groupRecyclerView.adapter = currentAdapter
        }

        refreshGroups()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setNoGroups()
        dimView.bringToFront()

        when (role) {
            User.Role.MEMBER -> {
                groupMenuOptionsView.renameGroup.visibility = View.GONE
                groupMenuOptionsView.removeGroup.removeGroupImage.rotation = 180f
                groupMenuOptionsView.removeGroup.removeGroupImage.setImageResource(R.drawable.leave_group_red)
                groupMenuOptionsView.removeGroup.removeGroupText.setText(R.string.leave_group)
            }
            User.Role.ADMIN -> {
                groupMenuOptionsView.renameGroup.visibility = View.VISIBLE
                groupMenuOptionsView.removeGroup.removeGroupImage.setImageResource(R.drawable.ic_trash_can)
                groupMenuOptionsView.removeGroup.removeGroupText.setText(R.string.delete_group)
            }
        }

        // Setup options menu for groups
        groupMenuOptionsView.closeButton.setOnClickListener {
            dismissPopup()
        }

        groupMenuOptionsView.renameGroup.setOnClickListener {
            beginRenameGroup()
        }

        groupMenuOptionsView.renameGroupDetail.saveGroupName.setOnClickListener {
            endRenameGroup() // passing a boolean?
            // TODO: dismiss popup?
        }

        groupMenuOptionsView.removeGroup.setOnClickListener {
            removeGroup()
        }

        // Setup for joining/creating groups
        addGroupEditText.addTextChangedListener(addGroupTextWatcher)
        addGroupEditText.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus -> addGroupEditText.isCursorVisible = hasFocus }
        addGroupEditText.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_NEXT, EditorInfo.IME_ACTION_SEARCH -> {
                    addGroupEditText.isCursorVisible = false
                    true
                }
                else -> {
                    addGroupEditText.isCursorVisible = true
                    false
                }
            }
        }

        when (role) {
            User.Role.MEMBER -> {
                addGroupEditText.setHint(R.string.join_group_hint)
                addGroupEditText.setTextColor(Color.WHITE)
                addGroupEditText.filters = addGroupEditText.filters +
                        InputFilter.AllCaps() +
                        InputFilter.LengthFilter(6)

                addGroupButton.setText(R.string.join_button_text)
                addGroupButton.setOnClickListener {
                    joinGroup(addGroupEditText.text.toString())
                    addGroupEditText.setText("")
                }

                addGroupBar.setBackgroundResource(R.color.black)
            }

            User.Role.ADMIN -> {
                addGroupEditText.setHint(R.string.create_group_hint)
                addGroupEditText.setTextColor(Color.BLACK)

                addGroupButton.setText(R.string.create_button_text)
                addGroupButton.setOnClickListener {
                    createGroup(addGroupEditText.text.toString())
                    addGroupEditText.setText("")
                }

                addGroupBar.setBackgroundResource(R.color.lightGray)
            }
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is GroupFragmentDelegate) {
            delegate = context
        } else {
            Log.d("Incorrect context", "GroupFragment context must be a GroupFragmentDelegate")
        }
    }

    fun refreshGroups() {
        if (role == null) return
        CoroutineScope(Dispatchers.IO).launch {
            val getGroupsEndpoint = Endpoint.getAllGroups(role!!.name.toLowerCase())
            val typeTokenGroups = object : TypeToken<ApiResponse<ArrayList<Group>>>() {}.type
            val getGroupsResponse = Request.makeRequest<ApiResponse<ArrayList<Group>>>(getGroupsEndpoint.okHttpRequest(), typeTokenGroups)

            withContext(Dispatchers.Main.immediate) {
                if (getGroupsResponse?.success == false || getGroupsResponse?.data == null) {
                    withContext(Dispatchers.Main) {
                        AlertDialog.Builder(context)
                                .setTitle("Could Not Fetch Groups")
                                .setMessage("Please try again later.")
                                .setNeutralButton(android.R.string.ok, null)
                                .show()
                    }
                    return@withContext
                }

                groups = getGroupsResponse.data
                groups.apply {
                    sort()
                    reverse()
                }

                currentAdapter?.addAll(groups)
                currentAdapter?.notifyDataSetChanged()
                setNoGroups()
            }
        }
    }

    private fun removeGroup() {
        val groupId = groupSelected?.id ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val typeToken = object : TypeToken<ApiResponse<String>>() {}.type

            var response: ApiResponse<String>? = null

            when (role) {
                User.Role.MEMBER -> {
                    val leaveGroupEndpoint = Endpoint.leaveGroup(groupId)
                    response = Request.makeRequest<ApiResponse<String>>(leaveGroupEndpoint.okHttpRequest(), typeToken)
                }
                User.Role.ADMIN -> {
                    val deleteGroupEndpoint = Endpoint.deleteGroup(groupId)
                    response = Request.makeRequest<ApiResponse<String>>(deleteGroupEndpoint.okHttpRequest(), typeToken)
                }
                null -> return@launch
            }

            if (response?.success ?: return@launch) {
                withContext(Dispatchers.Main) {
                    dismissPopup()
                    groups = ArrayList(groups.filter { it.id != groupId })
                    currentAdapter?.addAll(groups)
                    currentAdapter?.notifyDataSetChanged()
                    setNoGroups()
                }
            }
        }
    }


    fun addGroup(group: Group) {
        groups.add(0, group)
        currentAdapter?.addAll(groups)
        currentAdapter?.notifyDataSetChanged()
        setNoGroups()
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        fragmentInteractionListener?.onFragmentInteraction(uri)
    }

    /**
     * Checks the number of groups and toggles the no groups view (empty state) if there are
     * no groups. Should be called every time `groups` is modified.
     */
    private fun setNoGroups() {
        if (noGroupsView != null) {
            noGroupsView.visibility = if (groups.isNotEmpty()) View.GONE else View.VISIBLE

            when (role) {
                User.Role.MEMBER -> {
                    noGroupsEmoji.text = getString(R.string.no_groups_joined_emoji)
                    noGroupsTitle.text = getString(R.string.no_groups_joined_title)
                    noGroupsSubtext.text = getString(R.string.no_groups_joined_subtext)
                }
                User.Role.ADMIN -> {
                    noGroupsEmoji.text = getString(R.string.no_groups_created_emoji)
                    noGroupsTitle.text = getString(R.string.no_groups_created_title)
                    noGroupsSubtext.text = getString(R.string.no_groups_created_subtext)
                }
            }
        }
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    /// Methods for managing the Group options menu

    override fun onMoreButtonPressed(group: Group?) {
        if (isPopupActive) return
        isPopupActive = true
        setDim(true)
        groupSelected = group
        groupMenuOptionsView.groupNameTextView.text = group?.name ?: "Pollo Group"
        groupMenuOptionsView.visibility = View.VISIBLE
        val animate = TranslateAnimation(0f, 0f, groupMenuOptionsView.height.toFloat(), 0f)
        animate.duration = 300
        animate.fillAfter = true
        groupMenuOptionsView.startAnimation(animate)
    }

    fun dismissPopup() {
        if (!isPopupActive) return
        isPopupActive = false
        setDim(false)
        val animate = TranslateAnimation(0f, 0f, 0f, groupMenuOptionsView.height.toFloat())
        animate.duration = 300
        animate.fillAfter = true
        groupMenuOptionsView.startAnimation(animate)
        groupMenuOptionsView.visibility = View.INVISIBLE
        groupSelected = null
    }

    /**
     * Dims the `GroupFragment` and calls the delegates dim method according to `shouldDim`
     */
     private fun setDim(shouldDim: Boolean) {
        delegate?.setDim(shouldDim, this)

        // Don't want to be able to open group views when dimmed
        setSelfDim(shouldDim)
    }

    fun setSelfDim(shouldDim: Boolean) {
        // Don't want to be able to open group views when dimmed
        dimView.isClickable = shouldDim

        val alphaValue = if (shouldDim) 0.5f else 0.0f
        val dimAnimation = ObjectAnimator.ofFloat(dimView, "alpha", alphaValue)
        dimAnimation.duration = 500
        dimAnimation.start()
    }

    /// Group joining/creation methods and values

    /**
     * Handles enabling the `addGroupButton` and the associated color change (green --> active)
     */
    private val addGroupTextWatcher = object : TextWatcher {

        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            val canProceed = when (role) {
                User.Role.MEMBER -> s?.length == 6
                User.Role.ADMIN -> s?.length != 0
                null -> return
            }

            addGroupButton.isEnabled = canProceed
            val correctBackground = if (canProceed) R.drawable.rounded_join_button_filled else R.drawable.rounded_join_button
            addGroupButton.setBackgroundResource(correctBackground)
        }
    }

    /**
     * Joins the group and launches a new `PollsDateActivity` associated with it
     */
    private fun joinGroup(code: String) {
        val endpoint = Endpoint.joinGroupWithCode(code)
        CoroutineScope(Dispatchers.IO).launch {
            val typeTokenGroupNode = object : TypeToken<ApiResponse<Group>>() {}.type
            val typeTokenSortedPolls = object : TypeToken<ApiResponse<ArrayList<GetSortedPollsResponse>>>() {}.type
            val groupResponse = Request.makeRequest<ApiResponse<Group>>(endpoint.okHttpRequest(), typeTokenGroupNode)

            if (groupResponse?.success == false || groupResponse?.data == null) {
                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(context)
                            .setTitle("Code Not Valid")
                            .setMessage("Failed to join session with code $code.\nTry again!")
                            .setNeutralButton(android.R.string.ok, null)
                            .show()
                }
                return@launch
            }

            withContext(Dispatchers.Main) {
                addGroup(groupResponse.data)
            }

            val allPollsEndpoint = Endpoint.getSortedPolls(groupResponse.data.id)
            val sortedPolls = Request.makeRequest<ApiResponse<ArrayList<GetSortedPollsResponse>>>(allPollsEndpoint.okHttpRequest(), typeTokenSortedPolls)

            if (sortedPolls?.success == false || sortedPolls?.data == null) return@launch

            role?.let { delegate?.startGroupActivity(it, groupResponse.data, sortedPolls.data) }
                    ?: return@launch
        }
    }

    /**
     * Joins the group and launches a new `PollsDateActivity` associated with it
     */
    private fun createGroup(name: String) {
        val typeTokenGroupCode = object : TypeToken<ApiResponse<GroupCode>>() {}.type
        val generateCodeEndpoint = Endpoint.generateCode()

        CoroutineScope(Dispatchers.IO).launch {
            val result = Request.makeRequest<ApiResponse<GroupCode>>(generateCodeEndpoint.okHttpRequest(), typeTokenGroupCode)

            if (result?.success == true) {
                val code = result.data.code

                val joinSessionEndpoint = Endpoint.startSession(code, name)
                val typeTokenGroupNode = object : TypeToken<ApiResponse<Group>>() {}.type
                val groupResponse = Request.makeRequest<ApiResponse<Group>>(joinSessionEndpoint.okHttpRequest(), typeTokenGroupNode)

                if (groupResponse?.success == false || groupResponse?.data == null) return@launch


                withContext(Dispatchers.Main) {
                    addGroup(groupResponse.data)
                }

                role?.let { delegate?.startGroupActivity(it, groupResponse.data, ArrayList()) }
                        ?: return@launch
            } else {
                Log.e("failure", "backend response failed to generate code")
                return@launch
            }
        }
    }

    /**
     * Shows keyboard and renaming `EditText` in the group options menu
     */
    private fun beginRenameGroup() {


        groupMenuOptionsView.renameGroupDetail.visibility = View.VISIBLE
        renameGroup.visibility = View.GONE
        removeGroup.visibility = View.GONE

        groupMenuOptionsView.groupNameTextView.text = "Edit Name"
        groupMenuOptionsView.renameGroupDetail.renameGroupEditText.hint = groupSelected?.name
        groupMenuOptionsView.renameGroupDetail.renameGroupEditText.setSelection(0)


//        var alertDialog = AlertDialog.Builder(context)
//
//        alertDialog.apply {
//            setTitle("Edit Name")
//
//            val editText = EditText(context)
//
//            editText.apply {
//                hint = groupSelected?.name
//
//                setPadding(40,0,40,0)
//
//                setBackgroundResource(R.drawable.rounded_container)
//            }
//
//            setView(editText)
//
//            setPositiveButton("Save") { d, _ ->
//                val newName = editText.text.toString()
//                newName.removePrefix(" ")
//                if (newName == "") return@setPositiveButton
//
//                println("set new name")
//                d.cancel()
//                dismissPopup()
//            }
//
//            setNegativeButton("Cancel") { d, _ ->
//                d.cancel()
//                dismissPopup()
//            }
//
//        }
//
//        alertDialog.show()
    }

    /**
     * Resets the group options menu to its default state (edit/delete group options for admins) and
     * renames group (locally and to beckend). Does not dismiss the group options menu.
     */
    private fun endRenameGroup(){

        // TODO
    }

    companion object {

        private val ARG_SECTION_NUMBER = "section_number"
        private val GROUP_ROLE = "group_role"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(sectionNumber: Int, userRole: User.Role): GroupFragment {
            val fragment = GroupFragment()
            val args = Bundle()
            fragment.sectionNumber = sectionNumber
            args.putInt(ARG_SECTION_NUMBER, sectionNumber)
            args.putSerializable(GROUP_ROLE, userRole)
            fragment.arguments = args
            return fragment
        }
    }

    interface GroupFragmentDelegate {
        /**
         * Should dim any parts of the screen outside of `GroupFragment`
         */
        fun setDim(shouldDim: Boolean, groupFragment: GroupFragment)

        /**
         * Launches a `PollsDateActivity` for the given group and parameters
         */
        fun startGroupActivity(role: User.Role, group: Group, polls: ArrayList<GetSortedPollsResponse>)
    }
}
