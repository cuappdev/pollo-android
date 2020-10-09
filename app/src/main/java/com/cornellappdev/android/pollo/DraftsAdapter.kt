package com.cornellappdev.android.pollo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.core.content.ContextCompat
import com.cornellappdev.android.pollo.models.Draft
import com.cornellappdev.android.pollo.models.Group
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.SnackbarContentLayout
import kotlinx.android.synthetic.main.draft_list_item.view.*

class DraftAdapter(private val context: Context,
                   private var drafts: ArrayList<Draft>,
                   val callback: OnDraftOptionsPressedListener
) :
        BaseAdapter() {

    var delegate: DraftsDelegate? = null

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var selectedDraftItem: View? = null // draft_list_item that is currently selected

    override fun getCount() = drafts.size
    override fun getItem(position: Int) = drafts[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val viewHolder: ViewHolder
        val rowView: View

        if (convertView == null) {
            rowView = inflater.inflate(R.layout.draft_list_item, parent, false)
            viewHolder = ViewHolder(rowView)
            rowView.tag = viewHolder
        } else {
            rowView = convertView
            viewHolder = rowView.tag as ViewHolder
        }

        viewHolder.itemName.text = drafts[position].text

        rowView.setOnClickListener {
            draftSelected(rowView, drafts[position])
        }

        rowView.draftOptionsButton.setOnClickListener {
            callback.OnDraftOptionsPressed(position)
            //draftOptionsSelected(rowView, position)
        }

        return rowView
    }

    private class ViewHolder(view: View) {
        val itemName = view.findViewById(R.id.questionTextView) as TextView
    }

    override fun notifyDataSetChanged() {
        if (selectedDraftItem != null) {
            setDraftItemSelection(selectedDraftItem, SelectionAction.Deselect)
            selectedDraftItem = null
        }
        super.notifyDataSetChanged()
    }

    private fun draftSelected(view: View, draft: Draft) {
        when (selectedDraftItem) {
            null -> {
                // Selecting this item, no others selected
                setDraftItemSelection(view, SelectionAction.Select)
                selectedDraftItem = view
                delegate?.draftSelected(draft)
            }
            view -> {
                // Deselecting this item
                setDraftItemSelection(view, SelectionAction.Deselect)
                selectedDraftItem = null
                delegate?.draftDeselected()
            }
            else -> {
                // Selecting a new item, one already selected
                setDraftItemSelection(selectedDraftItem, SelectionAction.Deselect)
                setDraftItemSelection(view, SelectionAction.Select)
                selectedDraftItem = view
                delegate?.draftSelected(draft)
            }
        }
    }

    private fun setDraftItemSelection(view: View?, action: SelectionAction) {
        val color = when (action) {
            SelectionAction.Select -> {
                view?.setBackgroundResource(R.drawable.rounded_drafts_background_selected)
                ContextCompat.getColor(context, R.color.black)
            }
            SelectionAction.Deselect -> {
                view?.setBackgroundResource(R.drawable.rounded_drafts_background_deselected)
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
        if (selectedDraftItem != null) {
            // Reset selection
            setDraftItemSelection(selectedDraftItem, SelectionAction.Deselect)
            //clear draft text and options
            delegate?.draftDeselected()
        }
        delegate?.draftDeleted(position)

    }

    interface DraftsDelegate {
        fun draftSelected(draft: Draft) // Populates draft creation fields appropriately
        fun draftDeselected()   // Clears draft creation fields
        fun draftDeleted(position: Int)

    }

    interface OnDraftOptionsPressedListener {
        fun OnDraftOptionsPressed(position: Int)
    }
}