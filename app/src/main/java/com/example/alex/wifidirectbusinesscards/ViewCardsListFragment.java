package com.example.alex.wifidirectbusinesscards;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import ezvcard.Ezvcard;
import ezvcard.VCard;


public class ViewCardsListFragment extends Fragment {
    private LinkedList<String> mData = new LinkedList<String>();
    private String FILE_NAME;
    private String INCOMING_ACTIVITY = null;

    private OnViewCardsFragmentInteractionListener mListener;

    public ViewCardsListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            FILE_NAME = getArguments().getString("FILE_NAME");
            INCOMING_ACTIVITY = getArguments().getString("INCOMING_ACTIVITY");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_view_cards_list, container, false);
        final RecyclerView mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(v.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        mRecyclerView.setLayoutManager(layoutManager);

        mData = populateTheListofCards();
        final RecyclerViewAdapter adapter = new RecyclerViewAdapter(mData);
        mRecyclerView.setAdapter(adapter);


        //Add Item decoration
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(v.getContext(), DividerItemDecoration.VERTICAL_LIST);
        mRecyclerView.addItemDecoration(itemDecoration);

        final RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        mRecyclerView.setItemAnimator(itemAnimator);


        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(v.getContext(),
                new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Log.d("VCLF", position+"");
                        mListener.onCardSelected(FILE_NAME, position, INCOMING_ACTIVITY);
                    }
                }));

        return v;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnViewCardsFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnViewCardsFragmentInteractionListener {
        public void onCardSelected(String file, int position, String incomingLocation);
    }

    public LinkedList<String> populateTheListofCards()
    {
        List<VCard> temp = new LinkedList<VCard>();
        LinkedList<String> stringsList = new LinkedList<String>();
        try {
            temp = Ezvcard.parse(new File(((MainActivity)mListener).getFilesDir(), "vcards.xml")).all();
            Log.e("ViewCardListActivity: SIZE: ", temp.size()+"");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        for (VCard vC: temp)
        {
            stringsList.add(vC.getFormattedName().getValue());
        }
        return stringsList;

    }
    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
        private OnItemClickListener mListener;

        GestureDetector mGestureDetector;

        public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
            mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
        }
        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(childView, view.getChildPosition(childView));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
        }
    }


}

