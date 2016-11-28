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

package com.orpheusdroid.screenrecorder.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.orpheusdroid.screenrecorder.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by vijai on 07-11-2016.
 */

//Custom Recycler view adapter for video list fragment
public class VideoRecyclerAdapter extends RecyclerView.Adapter<VideoRecyclerAdapter.ViewHolder> {
    private ArrayList<Video> videos;
    private Context context;

    public VideoRecyclerAdapter(Context context, ArrayList<Video> android) {
        this.videos = android;
        this.context = context;
    }

    @Override
    public VideoRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_video, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final VideoRecyclerAdapter.ViewHolder viewHolder, int position) {
        //Set video file name
        viewHolder.tv_fileName.setText(videos.get(position).getFileName());
        //If thumbnail has failed for some reason, set empty image resource to imageview
        if (videos.get(position).getThumbnail() != null) {
            viewHolder.iv_thumbnail.setImageBitmap(videos.get(position).getThumbnail());
        }
        else
            viewHolder.iv_thumbnail.setImageResource(0);

        //Show user a chooser to play the video with
        viewHolder.videoCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Videos List", "video position clicked: " + viewHolder.getAdapterPosition());
                Intent openVideoIntent = new Intent(Intent.ACTION_VIEW);
                openVideoIntent.setData(FileProvider.getUriForFile(context,
                        context.getApplicationContext().getPackageName() +
                                ".provider", new File(videos.get(viewHolder.getAdapterPosition()).getFile().getPath())))
                .setType("video/*");
                context.startActivity(openVideoIntent);
            }
        });

        //Show chooser to the user to select an app to share the video with
        viewHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Videos List", "share position clicked: " + viewHolder.getAdapterPosition());
                Intent Shareintent = new Intent(Intent.ACTION_SEND)
                        .setDataAndType(videos.get(viewHolder.getAdapterPosition()).getFile(), "video/*");
                context.startActivity(Intent.createChooser(Shareintent,
                        context.getString(R.string.share_intent_notification_title)));
            }
        });
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_fileName;
        private ImageView iv_thumbnail;
        private RelativeLayout videoCard;
        private ImageButton shareBtn;

        ViewHolder(View view) {
            super(view);
            tv_fileName = (TextView) view.findViewById(R.id.fileName);
            iv_thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            iv_thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            videoCard = (RelativeLayout) view.findViewById(R.id.videoCard);
            shareBtn = (ImageButton) view.findViewById(R.id.share);
        }
    }
}
