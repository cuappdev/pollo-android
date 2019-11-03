package com.cornellappdev.android.pollo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_create_poll.view.*

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

        adapter = CreatePollAdapter(context!!, options)
        options = arrayListOf("Option A", "Option B")
        adapter!!.notifyDataSetChanged()
        rootView.poll_options.adapter = adapter
        options = arrayListOf()

        val linLayout = rootView.create_poll_option as LinearLayout
        val option = linLayout.add_poll_option_text as EditText
        val addOption = linLayout.add_poll_option_button as ImageButton

        addOption.setOnClickListener {
            options.add(option.text.toString())
            if (options.size < 2) options.add("Option B")
            adapter!!.notifyDataSetChanged()
        }

        return rootView
    }
}