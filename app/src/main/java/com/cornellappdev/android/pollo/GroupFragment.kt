package com.cornellappdev.android.pollo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cornellappdev.android.pollo.models.ApiResponse
import com.cornellappdev.android.pollo.models.Group
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

        CoroutineScope(Dispatchers.IO).launch {
            val getGroupsEndpoint = Endpoint.getAllGroups("member")
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
                if (noGroupsView != null) {
                    noGroupsView.visibility = if (groups.isNotEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val rootView = inflater!!.inflate(R.layout.fragment_main, container, false)

        val groupRecyclerView = rootView.findViewById<RecyclerView>(R.id.group_list_recyclerView)
        groupRecyclerView.layoutManager = LinearLayoutManager(rootView.context)
        currentAdapter = GroupRecyclerAdapter(groups, callback)
        groupRecyclerView.adapter = currentAdapter

        if (noGroupsView != null) {
            noGroupsView.visibility = if (groups.isNotEmpty()) View.GONE else View.VISIBLE
        }

        return rootView
    }

    fun removeGroup(id: String) {
        groups = ArrayList(groups.filter { it.id != id })
        currentAdapter?.addAll(groups)
        currentAdapter?.notifyDataSetChanged()
        noGroupsView.visibility = if (groups.isNotEmpty()) View.GONE else View.VISIBLE
    }

    fun addGroup(group: Group) {
        groups.add(group)
        currentAdapter?.addAll(groups)
        currentAdapter?.notifyDataSetChanged()
        noGroupsView.visibility = if (groups.isNotEmpty()) View.GONE else View.VISIBLE
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        fragmentInteractionListener?.onFragmentInteraction(uri)
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

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(sectionNumber: Int, callback: OnMoreButtonPressedListener): GroupFragment {
            val fragment = GroupFragment(callback)
            val args = Bundle()
            fragment.sectionNumber = sectionNumber
            args.putInt(ARG_SECTION_NUMBER, sectionNumber)
            fragment.arguments = args
            return fragment
        }
    }
}