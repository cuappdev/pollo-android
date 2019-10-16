package com.cornellappdev.android.pollo

import android.content.Intent
import androidx.core.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import com.cornellappdev.android.pollo.models.ApiResponse
import com.cornellappdev.android.pollo.models.Group
import com.cornellappdev.android.pollo.networking.*
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.group_list_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupRecyclerAdapter(private val groups: ArrayList<Group>, val callback: GroupFragment.OnMoreButtonPressedListener?) : androidx.recyclerview.widget.RecyclerView.Adapter<GroupRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupRecyclerAdapter.ViewHolder {
        val inflatedView = parent.inflate(R.layout.group_list_item, false)
        return ViewHolder(inflatedView)
    }

    override fun getItemCount() = groups.size

    override fun onBindViewHolder(holder: GroupRecyclerAdapter.ViewHolder, position: Int) {
        val group = groups[position]
        holder.bind(group)
    }


    inner class ViewHolder internal constructor(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view), View.OnClickListener {

        private var view = view
        private var group: Group? = null
        private var isLoading = false

        init {
            view.setOnClickListener(this)

            view.groupDetailsButton.setOnClickListener {
                callback?.onMoreButtonPressed(group)
            }
        }

        override fun onClick(view: View) {
            if (isLoading) return

            isLoading = true

            view.groupDetailsButton.visibility = View.GONE
            view.groupLoadingIndicator.visibility = View.VISIBLE


            CoroutineScope(Dispatchers.Main).launch {
                val endpoint = Endpoint.joinGroupWithCode(group?.code ?: "")
                val typeTokenGroupNode = object : TypeToken<ApiResponse<Group>>() {}.type
                val typeTokenSortedPolls = object : TypeToken<ApiResponse<ArrayList<GetSortedPollsResponse>>>() {}.type
                val groupNodeResponse = withContext(Dispatchers.Default) { Request.makeRequest<ApiResponse<Group>>(endpoint.okHttpRequest(), typeTokenGroupNode) }

                val allPollsEndpoint = Endpoint.getSortedPolls(groupNodeResponse!!.data.id)
                val sortedPolls = withContext(Dispatchers.Default) { Request.makeRequest<ApiResponse<ArrayList<GetSortedPollsResponse>>>(allPollsEndpoint.okHttpRequest(), typeTokenSortedPolls) }

                val context = view.context
                val pollsDateActivity = Intent(context, PollsDateActivity::class.java)
                pollsDateActivity.putExtra("SORTED_POLLS", sortedPolls!!.data)
                pollsDateActivity.putExtra("GROUP_NODE", group)
                context.startActivity(pollsDateActivity)

                view.groupDetailsButton.visibility = View.VISIBLE
                view.groupLoadingIndicator.visibility = View.GONE
                isLoading = false
            }
        }

        fun bind(group: Group) {
            this.group = group
            view.groupNameTextView.text = group.name

            if (group.isLive == true) {
                view.groupLiveTextView.text = "• Live Now"
                view.groupLiveTextView.setTextColor(ContextCompat.getColor(view.context, R.color.liveNow))
            } else {
                val unixTime = System.currentTimeMillis() / 1000L
                val lastUpdated = java.lang.Long.parseLong(group.updatedAt)
                var timeResult = " "
                val timeSplit = Util.splitToComponentTimes(unixTime - lastUpdated)
                for (i in 0..6) {
                    timeResult = timeSplit[i].toString() + " " + TIME_LABELS[i]
                    if (timeSplit[i] != 0) break
                }

                if (timeResult[0] == '1') {
                    timeResult = timeResult.removeRange(timeResult.length - 1, timeResult.length)
                }
                view.groupLiveTextView.text = "${group.code}  •  Last live $timeResult ago"
            }
        }
    }

    fun addAll(newList: ArrayList<Group>) {
        groups.clear()
        groups.addAll(newList)
    }

    internal fun getItem(id: Int): Group {
        return groups[id]
    }

    companion object {
        internal val TIME_LABELS = arrayOf("years", "months", "weeks", "days", "hours", "minutes", "seconds")
    }
}
