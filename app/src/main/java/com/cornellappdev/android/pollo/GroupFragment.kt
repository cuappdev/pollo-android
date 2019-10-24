package com.cornellappdev.android.pollo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cornellappdev.android.pollo.models.ApiResponse
import com.cornellappdev.android.pollo.models.Group
import com.cornellappdev.android.pollo.models.User
import com.cornellappdev.android.pollo.networking.Endpoint
import com.cornellappdev.android.pollo.networking.Request
import com.cornellappdev.android.pollo.networking.getAllGroups
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_main.*
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
class GroupFragment(val callback: OnMoreButtonPressedListener) : Fragment() {

    private var sectionNumber: Int = 0
    private var currentAdapter: GroupRecyclerAdapter? = null
    private val fragmentInteractionListener: OnFragmentInteractionListener? = null

    private var groups = ArrayList<Group>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshGroups()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val rootView = inflater!!.inflate(R.layout.fragment_main, container, false)
        val role = arguments?.getSerializable(GroupFragment.GROUP_ROLE) as User.Role
        val groupRecyclerView = rootView.findViewById<RecyclerView>(R.id.group_list_recyclerView)
        groupRecyclerView.layoutManager = LinearLayoutManager(rootView.context)
        currentAdapter = GroupRecyclerAdapter(groups, callback, role)
        groupRecyclerView.adapter = currentAdapter

        setNoGroups()

        return rootView
    }

    public fun refreshGroups() {
        CoroutineScope(Dispatchers.IO).launch {
            val groupRole = arguments?.getSerializable(GroupFragment.GROUP_ROLE) as User.Role? ?: return@launch
            val getGroupsEndpoint = Endpoint.getAllGroups(groupRole.name.toLowerCase())
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

    fun removeGroup(id: Int) {
        groups = ArrayList(groups.filter { it.id != id })
        currentAdapter?.addAll(groups)
        currentAdapter?.notifyDataSetChanged()
        setNoGroups()
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

            when (arguments?.getSerializable(GroupFragment.GROUP_ROLE) as User.Role) {
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


    interface OnMoreButtonPressedListener {
        fun onMoreButtonPressed(group: Group?)
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }


    companion object {

        private val ARG_SECTION_NUMBER = "section_number"
        private val GROUP_ROLE = "group_role"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(sectionNumber: Int, callback: OnMoreButtonPressedListener, userRole: User.Role): GroupFragment {
            val fragment = GroupFragment(callback)
            val args = Bundle()
            fragment.sectionNumber = sectionNumber
            args.putInt(ARG_SECTION_NUMBER, sectionNumber)
            args.putSerializable(GROUP_ROLE, userRole)
            fragment.arguments = args
            return fragment
        }
    }
}
