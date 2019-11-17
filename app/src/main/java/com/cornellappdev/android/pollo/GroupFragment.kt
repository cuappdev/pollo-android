package com.cornellappdev.android.pollo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import com.cornellappdev.android.pollo.models.ApiResponse
import com.cornellappdev.android.pollo.models.Group
import com.cornellappdev.android.pollo.models.User
import com.cornellappdev.android.pollo.networking.*
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.manage_group_view.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

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

        when (role) {
            User.Role.MEMBER -> {
                groupMenuOptionsView.editGroupName.visibility = View.GONE
                groupMenuOptionsView.removeGroup.removeGroupImage.rotation = 180f
                groupMenuOptionsView.removeGroup.removeGroupImage.setImageResource(R.drawable.leave_group_red)
                groupMenuOptionsView.removeGroup.removeGroupText.setText(R.string.leave_group)
            }
            User.Role.ADMIN -> {
                groupMenuOptionsView.editGroupName.visibility = View.VISIBLE
                groupMenuOptionsView.removeGroup.removeGroupImage.setImageResource(R.drawable.ic_trash_can)
                groupMenuOptionsView.removeGroup.removeGroupText.setText(R.string.delete_group)
            }
        }

        // Setup options menu for groups
        groupMenuOptionsView.closeButton.setOnClickListener {
            dismissPopup()
        }

        groupMenuOptionsView.editGroupName.setOnClickListener {
            // TODO(#40)
        }

        groupMenuOptionsView.removeGroup.setOnClickListener {
            removeGroup()
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        // TODO: setup delegation to MainActivity
    }

    public fun refreshGroups() {
        CoroutineScope(Dispatchers.IO).launch {
            if (role == null) return@launch
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
        groups.add(group)
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

    override fun onMoreButtonPressed(group: Group?) {
//        manageDim(true)
        // TODO: setup delegation an call delegate method of `MainActivity` to dim view
        groupSelected = group
        groupMenuOptionsView.groupNameTextView.text = group?.name ?: "Pollo Group"
        groupMenuOptionsView.visibility = View.VISIBLE
        val animate = TranslateAnimation(0f, 0f, groupMenuOptionsView.height.toFloat(), 0f)
        animate.duration = 300
        animate.fillAfter = true
        groupMenuOptionsView.startAnimation(animate)
    }

    private fun dismissPopup() {
//        manageDim(false)
//        dimView.isClickable = false
//        dimView.isFocusable = false
        // TODO: setup delegation an call delegate method of `MainActivity` to undim view
        val animate = TranslateAnimation(0f, 0f, 0f, groupMenuOptionsView.height.toFloat())
        animate.duration = 300
        animate.fillAfter = true
        groupMenuOptionsView.startAnimation(animate)
        groupMenuOptionsView.visibility = View.INVISIBLE
        groupSelected = null
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
}
