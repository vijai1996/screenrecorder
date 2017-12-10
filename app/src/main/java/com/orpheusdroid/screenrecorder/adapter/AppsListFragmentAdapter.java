/*
 * Copyright (c) 2017. Vijai Chandra Prasad R.
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

package com.orpheusdroid.screenrecorder.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.orpheusdroid.screenrecorder.R;

import java.util.ArrayList;

/**
 * Created by vijai on 08-12-2017.
 */

public class AppsListFragmentAdapter extends RecyclerView.Adapter<AppsListFragmentAdapter.SimpleViewHolder> {
    private ArrayList<Apps> apps;
    private OnItemClicked onClick;

    // Interface to handle recycler view item click
    public interface OnItemClicked {
        void onItemClick(int position);
    }

    public AppsListFragmentAdapter(ArrayList<Apps> apps) {
        this.apps = apps;
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_apps_list_preference, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder holder, final int position) {
        Apps app = apps.get(holder.getAdapterPosition());
        holder.textView.setText("" + app.getAppName());
        holder.appIcon.setImageDrawable(app.getAppIcon());

        // Show a visible tick mark for the selected app
        if (app.isSelectedApp())
            holder.selectedApp.setVisibility(View.VISIBLE);
        else
            holder.selectedApp.setVisibility(View.INVISIBLE);
        holder.app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick.onItemClick(holder.getAdapterPosition());
            }
        });
    }

    // A static view holder class to hold the view items used by the recycler view
    static class SimpleViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView appIcon;
        ImageView selectedApp;
        RelativeLayout app;

        SimpleViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.appName);
            appIcon = itemView.findViewById(R.id.appIcon);
            selectedApp = itemView.findViewById(R.id.appChecked);
            app = itemView.findViewById(R.id.app);
        }
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public void setOnClick(OnItemClicked onClick) {
        this.onClick = onClick;
    }
}
