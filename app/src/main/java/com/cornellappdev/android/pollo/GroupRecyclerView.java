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
    //https://stackoverflow.com/questions/40584424/simple-android-recyclerview-example
    private List<Group> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    GroupRecyclerView(Context context, List<Group> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.group_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Group group = mData.get(position);
        if(group != null) {
            holder.groupName.setText(group.getName());
            if(group.isLive())
                holder.groupSubtext.setText("⚫ Live");
            else {
                long unixTime = System.currentTimeMillis() / 1000L;
                long lastUpdated = Long.parseLong(group.getUpdatedAt());
                String timeResult = "";
                int[] timeSplit = Util.splitToComponentTimes(unixTime - lastUpdated);
                for(int i=0;i<7;i++){
                    switch (i){
                        case 0: timeResult = timeSplit[i] + " years"; break;
                        case 1: timeResult = timeSplit[i] + " months"; break;
                        case 2: timeResult = timeSplit[i] + " weeks"; break;
                        case 3: timeResult = timeSplit[i] + " days"; break;
                        case 4: timeResult = timeSplit[i] + " hours"; break;
                        case 5: timeResult = timeSplit[i] + " minutes"; break;
                        case 6: timeResult = timeSplit[i] + " seconds"; break;
                    }
                    if(timeSplit[i] != 0)
                        break;
                }
                holder.groupSubtext.setText("Last live " + timeResult + " ago.");
            }
        }
        //holder.groupSubtext.setText();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView groupName;
        TextView groupSubtext;
        TextView groupMenu;

        ViewHolder(View itemView){
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

    public void addAll(List<Group> newList){
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
