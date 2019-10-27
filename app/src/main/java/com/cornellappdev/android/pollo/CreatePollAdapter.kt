package com.cornellappdev.android.pollo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.annotation.LayoutRes
import kotlinx.android.synthetic.main.create_poll_options_list_item.view.*

class CreatePollAdapter(private val context: Context, private val options: ArrayList<String>) :
        BaseAdapter() {

    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount() = options.size
    override fun getItem(position: Int) = options[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = inflater.inflate(R.layout.create_poll_options_list_item, parent, false)

        val rightAnswerButton = convertView!!.create_poll_options_item as RadioButton
        rightAnswerButton.text = options[position]

        return rowView
    }
}