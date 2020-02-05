package com.cornellappdev.android.pollo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.cornellappdev.android.pollo.models.ApiResponse
import com.cornellappdev.android.pollo.models.Group
import com.cornellappdev.android.pollo.networking.*
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_create_poll.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("ValidFragment")
class CreatePollFragment: Fragment() {
    var options: ArrayList<String> = arrayListOf()
    var adapter: CreatePollAdapter? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater?.inflate(R.layout.fragment_create_poll, container, false)

        options = arrayListOf()
        adapter = CreatePollAdapter(context!!, options, -1)
        rootView.poll_options.adapter = adapter
        addOptionToList()
        addOptionToList()

        val addOption = rootView.add_poll_option_button as Button
        val saveDraft = rootView.save_draft as Button
        val startPoll = rootView.start_poll as Button

        addOption.setOnClickListener {
            addOptionToList()
        }

        startPoll.setOnClickListener {}

        return rootView
    }

    private fun addOptionToList(){
        options.add("Option " + (options.size + 65).toChar())
        adapter!!.notifyDataSetChanged()
    }

    /**
     * Starts poll and returns to group
     */
    private fun startPoll(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val code = result.data.code

            val startPollEndpoint = Endpoint.startPoll("start", name)
            val typeTokenGroupNode = object : TypeToken<ApiResponse<Group>>() {}.type
            val groupResponse = Request.makeRequest<ApiResponse<Group>>(startPollEndpoint.okHttpRequest(), typeTokenGroupNode)

            if (groupResponse?.success == false || groupResponse?.data == null) return@launch
        }
    }
}