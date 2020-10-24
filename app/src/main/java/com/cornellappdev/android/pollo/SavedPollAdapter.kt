package com.cornellappdev.android.pollo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.cornellappdev.android.pollo.models.SavedPoll
import kotlinx.android.synthetic.main.savedpoll_list_item.view.*

class SavedPollAdapter(
        private val context: Context,
        private var savedPoll: ArrayList<SavedPoll>,
        val callback: OnSavedPollOptionsPressedListener
) :
        BaseAdapter() {

    var delegate: SavedPollDelegate? = null

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var selectedPollItem: View? = null // savedpoll_list_item that is currently selected

    override fun getCount() = savedPoll.size
    override fun getItem(position: Int) = savedPoll[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val viewHolder: ViewHolder
        val rowView: View

        if (convertView == null) {
            rowView = inflater.inflate(R.layout.savedpoll_list_item, parent, false)
            viewHolder = ViewHolder(rowView)
            rowView.tag = viewHolder
        } else {
            rowView = convertView
            viewHolder = rowView.tag as ViewHolder
        }

        viewHolder.itemName.text = savedPoll[position].text

        rowView.setOnClickListener {
            savedPollSelected(rowView, savedPoll[position])
        }

        rowView.savedPollOptionsButton.setOnClickListener {
            callback.onSavedPollOptionsPressed(position)
        }

        return rowView
    }

    private class ViewHolder(view: View) {
        val itemName = view.findViewById(R.id.questionTextView) as TextView
    }

    override fun notifyDataSetChanged() {
        if (selectedPollItem != null) {
            setSavedPollItemSelection(selectedPollItem, SelectionAction.Deselect)
            selectedPollItem = null
        }
        super.notifyDataSetChanged()
    }

    private fun savedPollSelected(view: View, savedPoll: SavedPoll) {
        when (selectedPollItem) {
            null -> {
                // Selecting this item, no others selected
                setSavedPollItemSelection(view, SelectionAction.Select)
                selectedPollItem = view
                delegate?.savedPollSelected(savedPoll)
            }
            view -> {
                // Deselecting this item
                setSavedPollItemSelection(view, SelectionAction.Deselect)
                selectedPollItem = null
                delegate?.savedPollDeselected()
            }
            else -> {
                // Selecting a new item, one already selected
                setSavedPollItemSelection(selectedPollItem, SelectionAction.Deselect)
                setSavedPollItemSelection(view, SelectionAction.Select)
                selectedPollItem = view
                delegate?.savedPollSelected(savedPoll)
            }
        }
    }

    private fun setSavedPollItemSelection(view: View?, action: SelectionAction) {
        val color = when (action) {
            SelectionAction.Select -> {
                view?.setBackgroundResource(R.drawable.rounded_savedpoll_background_selected)
                ContextCompat.getColor(context, R.color.black)
            }
            SelectionAction.Deselect -> {
                view?.setBackgroundResource(R.drawable.rounded_savedpoll_background_deselected)
                ContextCompat.getColor(context, R.color.mediumGray)
            }
        }

        view?.findViewById<TextView>(R.id.questionTextView)?.setTextColor(color)
        view?.findViewById<TextView>(R.id.questionTypeTextView)?.setTextColor(color)
    }


    private enum class SelectionAction {
        Select, Deselect
    }


    fun resetSelection(position: Int) {
        if (selectedPollItem != null) {
            // Reset selection
            setSavedPollItemSelection(selectedPollItem, SelectionAction.Deselect)
            //clear saved poll text and options
            delegate?.savedPollDeselected()
        }
        delegate?.savedPollDeleted(position)

    }

    interface SavedPollDelegate {
        fun savedPollSelected(savedPoll: SavedPoll) // Populates saved poll creation fields appropriately
        fun savedPollDeselected()   // Clears saved poll creation fields
        fun savedPollDeleted(position: Int)

    }

    interface OnSavedPollOptionsPressedListener {
        fun onSavedPollOptionsPressed(position: Int)
    }
}