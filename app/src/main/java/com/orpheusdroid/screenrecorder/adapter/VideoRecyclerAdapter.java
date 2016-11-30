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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by vijai on 07-11-2016.
 */

//Custom Recycler view adapter for video list fragment
public class VideoRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Video> videos;
    private Context context;
    private static final int VIEW_SECTION = 0;
    private static final int VIEW_ITEM = 1;

    public VideoRecyclerAdapter(Context context, ArrayList<Video> android) {
        this.videos = android;
        this.context = context;
    }

    //Find if the view is a section type or video type
    @Override
    public int getItemViewType(int position) {
        return isSection(position) ? VIEW_SECTION : VIEW_ITEM;
    }

    //Method to determine the type
    public boolean isSection(int position){
        return videos.get(position).isSection();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_SECTION:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_video_section, viewGroup, false);
                return new SectionViewHolder(view);
            case VIEW_ITEM:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_video, viewGroup, false);
                return new ItemViewHolder(view);
            default:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_video, viewGroup, false);
                return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_ITEM:
                final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
                //Set video file name
                itemViewHolder.tv_fileName.setText(videos.get(position).getFileName());
                //If thumbnail has failed for some reason, set empty image resource to imageview
                if (videos.get(position).getThumbnail() != null) {
                    itemViewHolder.iv_thumbnail.setImageBitmap(videos.get(position).getThumbnail());
                } else
                    itemViewHolder.iv_thumbnail.setImageResource(0);

                //Show user a chooser to play the video with
                itemViewHolder.videoCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("Videos List", "video position clicked: " + itemViewHolder.getAdapterPosition());
                        Intent openVideoIntent = new Intent(Intent.ACTION_VIEW);
                        openVideoIntent.setData(FileProvider.getUriForFile(context,
                                context.getApplicationContext().getPackageName() +
                                        ".provider", new File(videos.get(itemViewHolder.getAdapterPosition()).getFile().getPath())))
                                .setType("video/*");
                        context.startActivity(openVideoIntent);
                    }
                });

                //Show chooser to the user to select an app to share the video with
                itemViewHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("Videos List", "share position clicked: " + itemViewHolder.getAdapterPosition());
                        Intent Shareintent = new Intent(Intent.ACTION_SEND)
                                .setDataAndType(videos.get(itemViewHolder.getAdapterPosition()).getFile(), "video/*");
                        context.startActivity(Intent.createChooser(Shareintent,
                                context.getString(R.string.share_intent_notification_title)));
                    }
                });
                break;
            case VIEW_SECTION:
                SectionViewHolder sectionViewHolder = (SectionViewHolder) holder;
                sectionViewHolder.section.setText(generateSectionTitle(videos.get(position).getLastModified()));
                break;
        }
    }

    //Generate title for the section depending on the recording date
    private String generateSectionTitle(Date date){
        Calendar sDate = toCalendar(new Date().getTime());
        Calendar eDate = toCalendar(date.getTime());

        // Get the represented date in milliseconds
        long milis1 = sDate.getTimeInMillis();
        long milis2 = eDate.getTimeInMillis();

        // Calculate difference in milliseconds
        int dayDiff = (int)Math.abs((milis2 - milis1) / (24 * 60 * 60 * 1000));

        int yearDiff = sDate.get(Calendar.YEAR) - eDate.get(Calendar.YEAR);
        Log.d("ScreenRecorder", "yeardiff: " + yearDiff);

        if (yearDiff == 0) {
            switch (dayDiff) {
                case 0:
                    return "Today";
                case 1:
                    return "Yesterday";
                default:
                    SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMM", Locale.getDefault());
                    return format.format(date);
            }
        } else {
            SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMM YYYY", Locale.getDefault());
            return format.format(date);
        }
    }

    //Generate Calendar object and return it
    private Calendar toCalendar(long timestamp)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    //ViewHolder class for video items
    private final class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_fileName;
        private ImageView iv_thumbnail;
        private RelativeLayout videoCard;
        private ImageButton shareBtn;

        ItemViewHolder(View view) {
            super(view);
            tv_fileName = (TextView) view.findViewById(R.id.fileName);
            iv_thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            iv_thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            videoCard = (RelativeLayout) view.findViewById(R.id.videoCard);
            shareBtn = (ImageButton) view.findViewById(R.id.share);
        }
    }

    //ViewHolder class for sections
    private final class SectionViewHolder extends RecyclerView.ViewHolder {
        private TextView section;

        SectionViewHolder(View view) {
            super(view);
            section = (TextView) view.findViewById(R.id.sectionID);
        }
    }
}
