package com.example.alex.wifidirectbusinesscards;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Alex on 11/30/2014.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ListItemViewHolder>
{
    private LinkedList<String> articlesList;

    public RecyclerViewAdapter(LinkedList<String> list)
    {
        if (list == null)
        {
            throw new IllegalArgumentException("The list of articles cannot be null");
        }
        else
            articlesList = list;
    }
    public void addItems(LinkedList<String> newList)
    {
        for(String a: newList)
        {
            articlesList.add(a);
        }
        //Collections.sort(articlesList);
        notifyDataSetChanged();
    }
    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(android.R.layout.simple_list_item_1, viewGroup, false);
        return new ListItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder listItemViewHolder, int i) {
        String article = articlesList.get(i);
        listItemViewHolder.title.setText(article);

    }

    @Override
    public int getItemCount() {
        return articlesList.size();
    }

    public static final class ListItemViewHolder extends RecyclerView.ViewHolder
    {
        TextView title;
        TextView description;
        ImageView thumbnail;
        public ListItemViewHolder(View v)
        {
            super(v);
            title = (TextView) v.findViewById(android.R.id.text1);

        }
    }
}

