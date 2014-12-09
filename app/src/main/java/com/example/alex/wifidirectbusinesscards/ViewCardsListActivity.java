package com.example.alex.wifidirectbusinesscards;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import ezvcard.Ezvcard;
import ezvcard.VCard;


public class ViewCardsListActivity extends Activity {
    private LinkedList<String> mData;
    private String FILE_NAME;
    private String INCOMING_ACTIVITY = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_cards_list);

        FILE_NAME = getIntent().getStringExtra("FILE_NAME");
        INCOMING_ACTIVITY = getIntent().getStringExtra("INCOMING_ACTIVITY");

        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        mRecyclerView.setLayoutManager(layoutManager);

        mData = populateTheListofCards();
        final RecyclerViewAdapter adapter = new RecyclerViewAdapter(mData);
        mRecyclerView.setAdapter(adapter);


        //Add Item decoration
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        mRecyclerView.addItemDecoration(itemDecoration);

        final RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        mRecyclerView.setItemAnimator(itemAnimator);


        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent cardIntent = new Intent(getApplicationContext(), ViewCard.class);
                        cardIntent.putExtra("FILE_NAME", FILE_NAME);
                        cardIntent.putExtra("POSITION", position+"");
                        if (INCOMING_ACTIVITY.equals("MainActivity"))
                        {
                            Log.e("ViewCardsListActivity Position", position + " " + cardIntent.getStringExtra("POSITION")+"");
                            startActivity(cardIntent);
                        }
                        else
                        {
                            Log.e("ViewCards", "Made it here");
                            setResult(RESULT_OK, cardIntent);
                            Log.e("ViewCards", "result is set");
                            finish();
                            Log.e("ViewCards", "Should have left");
                        }
                    }
                }));

    }

    public LinkedList<String> populateTheListofCards()
    {
        List<VCard> temp = new LinkedList<VCard>();
        LinkedList<String> stringsList = new LinkedList<String>();
        try {
            temp = Ezvcard.parse(new File(getFilesDir(),FILE_NAME)).all();
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