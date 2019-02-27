package com.cornellappdev.android.pollo

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.cornellappdev.android.pollo.Models.ApiResponse

import com.cornellappdev.android.pollo.Models.Group
import com.cornellappdev.android.pollo.Models.Nodes.GroupNode
import com.cornellappdev.android.pollo.Models.Nodes.GroupNodeResponse
import com.cornellappdev.android.pollo.Networking.Endpoint
import com.cornellappdev.android.pollo.Networking.Request
import com.cornellappdev.android.pollo.Networking.getAllGroups
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.io.IOException
import java.util.ArrayList
import java.util.Arrays

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [GroupFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [GroupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupFragment : Fragment() {

    private var sectionNumber: Int = 0
    private var currentAdapter: GroupRecyclerAdapter? = null
    private val mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_main, container, false)
        val groupRecyclerView = rootView.findViewById<RecyclerView>(R.id.group_list_recyclerView)
        groupRecyclerView.layoutManager = LinearLayoutManager(rootView.context)
        currentAdapter = GroupRecyclerAdapter(ArrayList())
        groupRecyclerView.adapter = currentAdapter

        CoroutineScope(Dispatchers.IO).launch {
            val getGroupsEndpoint = Endpoint.getAllGroups("member")
            val typeTokenGroups = object : TypeToken<ApiResponse<ArrayList<GroupNode>>>() {}.type
            val getGroupsResponse = Request.makeRequest<ApiResponse<ArrayList<GroupNode>>>(getGroupsEndpoint.okHttpRequest(), typeTokenGroups)

            withContext(Dispatchers.Main.immediate) {
                currentAdapter!!.addAll(ArrayList(getGroupsResponse.data.map { it.node }))
                currentAdapter!!.notifyDataSetChanged()
                if (getGroupsResponse.data.size > 0)
                rootView.findViewById<View>(R.id.no_groups_layout).visibility = View.GONE
            }



        }

        // new RetrieveGroupsTask().execute(new Util().new Triple(rootView, currentAdapter, this.sectionNumber));
        return rootView
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        mListener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

//    internal inner class RetrieveGroupsTask : AsyncTask<Util.Triple<*, *, *>, Void, List<Group>>() {
//
//        var rootView: View
//        var currentAdapter: GroupRecyclerView
//
//        override fun doInBackground(vararg data: Util.Triple<*, *, *>): List<Group> {
//            val dataTriple = data[0]
//            rootView = dataTriple.getX() as View
//            currentAdapter = dataTriple.getY() as GroupRecyclerView
//            var groups: List<Group> = ArrayList()
//            try {
//                groups = if (dataTriple.getZ() as Int == 1)
//                    NetworkUtils.getAllGroupsAsMember(context)
//                else
//                    NetworkUtils.getAllGroupsAsAdmin(context)
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//
//            return groups
//        }
//
//        override fun onPostExecute(groups: List<Group>) {
//            super.onPostExecute(groups)
//
//            currentAdapter.addAll(groups)
//            currentAdapter.notifyDataSetChanged()
//            if (groups.size > 0)
//                rootView.findViewById<View>(R.id.no_groups_layout).visibility = View.GONE
//        }
//    }

    companion object {

        private val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(sectionNumber: Int): GroupFragment {
            val fragment = GroupFragment()
            val args = Bundle()
            fragment.sectionNumber = sectionNumber
            args.putInt(ARG_SECTION_NUMBER, sectionNumber)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
