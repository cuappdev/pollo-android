package com.cornellappdev.android.pollo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cornellappdev.android.pollo.models.Group;

import java.util.List;

public class PollRecyclerView extends RecyclerView.Adapter<PollRecyclerView.ViewHolder> {
    final static String[] TIME_LABELS = {"years", "months", "weeks", "days",
            "hours", "minutes", "seconds"};
    private List<Group> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    PollRecyclerView(Context context, List<Group> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = mInflater.inflate(R.layout.poll_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Group group = mData.get(position);
        if (group == null)
            return;
        holder.pollOption.setText(group.getName());
    }

    @Override
    public int getItemCount() {
        return mData.size();
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView pollOption;
        final TextView pollOptionMenu;

        ViewHolder(View itemView) {
            super(itemView);
            pollOption = itemView.findViewById(R.id.textView_poll_option);
            pollOptionMenu = itemView.findViewById(R.id.textView_poll_option_menu);
            pollOption.setOnClickListener(this);
            pollOptionMenu.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

        }
    }
}
