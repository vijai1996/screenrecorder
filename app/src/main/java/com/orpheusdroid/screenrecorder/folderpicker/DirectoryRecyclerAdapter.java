/*
 * Copyright (c) 2016. Vijai Chandra Prasad R.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses
 */

package com.orpheusdroid.screenrecorder.folderpicker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orpheusdroid.screenrecorder.Const;
import com.orpheusdroid.screenrecorder.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by vijai on 01-12-2016.
 */

class DirectoryRecyclerAdapter extends RecyclerView.Adapter<DirectoryRecyclerAdapter.ItemViewHolder> {
    private Context context;
    private ArrayList<File> directories;
    private static OnDirectoryClickedListerner onDirectoryClickedListerner;

    interface OnDirectoryClickedListerner {
        void OnDirectoryClicked(File directory);
    }

    DirectoryRecyclerAdapter(Context context, OnDirectoryClickedListerner listerner, ArrayList<File> directories){
        this.context = context;
        onDirectoryClickedListerner = listerner;
        this.directories = directories;
    }

    @Override
    public DirectoryRecyclerAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_directory_chooser, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DirectoryRecyclerAdapter.ItemViewHolder holder, int position) {
        holder.dir.setText(directories.get(position).getName());
        holder.dir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(Const.TAG, "Item clicked: " + directories.get(holder.getAdapterPosition()));
                onDirectoryClickedListerner.OnDirectoryClicked(directories.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return directories.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView dir;
        LinearLayout dir_view;
        public ItemViewHolder(View itemView) {
            super(itemView);
            dir = (TextView) itemView.findViewById(R.id.directory);
            dir_view = (LinearLayout) itemView.findViewById(R.id.directory_view);
        }
    }
}
