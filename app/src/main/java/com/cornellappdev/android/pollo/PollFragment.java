package com.cornellappdev.android.pollo;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cornellappdev.android.pollo.Models.Group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PollFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PollFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PollFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private int sectionNumber;
    private GroupRecyclerView currentAdapter;
    private OnFragmentInteractionListener mListener;

    public PollFragment() {
        // Required empty public constructor
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PollFragment newInstance(int sectionNumber) {
        final PollFragment fragment = new PollFragment();
        final Bundle args = new Bundle();
        fragment.sectionNumber = sectionNumber;
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_poll_group, container, false);
        final RecyclerView pollRecyclerView = rootView.findViewById(R.id.poll_list_recyclerView);
        pollRecyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));

        currentAdapter = new GroupRecyclerView(getContext(), new ArrayList<Group>());
        pollRecyclerView.setAdapter(currentAdapter);
        new RetrieveGroupsTask().execute(new Util().new Triple(rootView, currentAdapter, this.sectionNumber));
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    class RetrieveGroupsTask extends AsyncTask<Util.Triple, Void, List<Group>> {

        View rootView;
        GroupRecyclerView currentAdapter;

        @Override
        protected List<Group> doInBackground(Util.Triple... data) {
            final Util.Triple dataTriple = data[0];
            rootView = (View) dataTriple.getX();
            currentAdapter = (GroupRecyclerView) dataTriple.getY();
            List<Group> groups = new ArrayList<>();
            try {
                groups = ((int) dataTriple.getZ()) == 1 ? NetworkUtils.getAllGroupsAsMember(getContext())
                        : NetworkUtils.getAllGroupsAsAdmin(getContext());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return groups;
        }

        @Override
        protected void onPostExecute(List<Group> groups) {
            super.onPostExecute(groups);

            currentAdapter.addAll(groups);
            currentAdapter.notifyDataSetChanged();
        }
    }
}