package com.cornellappdev.android.pollo

import android.content.Context
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import kotlinx.android.synthetic.main.create_poll_options_list_item.view.*

class CreatePollAdapter(private val context: Context, private val options: ArrayList<String>, private var correct: Int, private var root: CreatePollFragment) :
        BaseAdapter() {

    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount() = options.size
    override fun getItem(position: Int) = options[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = inflater.inflate(R.layout.create_poll_options_list_item, parent, false)

        val rightAnswerButton = rowView!!.create_poll_options_item as CheckBox
        rightAnswerButton.isChecked = (correct == position)
        val optionPollName = rowView!!.create_poll_options_text as EditText

        if (options[position] == "Option " + (position + 65).toChar())
            optionPollName.hint = SpannableStringBuilder(options[position])
        else
            optionPollName.text = SpannableStringBuilder(options[position])

        rightAnswerButton.setOnClickListener {
            correct = if (correct == position) -1 else position
            this!!.notifyDataSetChanged()
            root.correct = correct
        }

        optionPollName.addTextChangedListener( object : TextWatcher{
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                options[position] = s.toString()
            }
        })

        return rowView
    }


}