package com.cornellappdev.android.pollo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cornellappdev.android.pollo.Models.Group;

import java.util.List;

public class GroupRecyclerView extends RecyclerView.Adapter<GroupRecyclerView.ViewHolder> {
    private List<Group> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    final static String[] TIME_LABELS = {"years", "months", "weeks", "days",
            "hours", "minutes", "seconds"};

    GroupRecyclerView(Context context, List<Group> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = mInflater.inflate(R.layout.group_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Group group = mData.get(position);
        if (group == null)
            return;
        holder.groupName.setText(group.getName());
        if (group.isLive())
            holder.groupSubtext.setText("⚫ Live");
        else {
            long unixTime = System.currentTimeMillis() / 1000L;
            long lastUpdated = Long.parseLong(group.getUpdatedAt());
            String timeResult = "";
            int[] timeSplit = Util.splitToComponentTimes(unixTime - lastUpdated);
            for (int i = 0; i < 7; i++) {
                timeResult = timeSplit[i] + " " + TIME_LABELS[i];
                if (timeSplit[i] != 0)
                    break;
            }
            holder.groupSubtext.setText("Last live " + timeResult + " ago");
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView groupName;
        final TextView groupSubtext;
        final TextView groupMenu;

        ViewHolder(View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.textView_group);
            groupSubtext = itemView.findViewById(R.id.textView_group_live);
            groupMenu = itemView.findViewById(R.id.textView_group_menu);
            itemView.setOnClickListener(this);
            groupMenu.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

        }
    }

    public void addAll(List<Group> newList) {
        mData.clear();
        mData.addAll(newList);
    }

    Group getItem(int id) {
        return mData.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
