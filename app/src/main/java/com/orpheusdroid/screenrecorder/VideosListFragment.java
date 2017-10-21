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

package com.orpheusdroid.screenrecorder;

import android.Manifest;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.orpheusdroid.screenrecorder.adapter.Video;
import com.orpheusdroid.screenrecorder.adapter.VideoRecyclerAdapter;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by vijai on 06-11-2016.
 */

public class VideosListFragment extends Fragment implements PermissionResultListener {
    private RecyclerView videoRV;
    private TextView message;
    private SharedPreferences prefs;
    private ArrayList<Video> videosList = new ArrayList<>();
    private ProgressBar progress;

    public VideosListFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_videos, container, false);
        message = view.findViewById(R.id.message_tv);
        videoRV = view.findViewById(R.id.videos_rv);
        progress = view.findViewById(R.id.video_progress);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    //Load videos from the directory only when the fragment is visible to the screen
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Log.d(Const.TAG, "Videos fragment is visible load the videos");
            checkPermission();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem refresh = menu.add("Refresh");
        refresh.setIcon(R.drawable.ic_refresh_white_24dp);
        refresh.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        refresh.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                videosList.clear();
                checkPermission();
                Log.d(Const.TAG, "Refreshing");
                return false;
            }
        });
    }

    //Check if we have permission to read the external storage. The fragment is useless without this
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setPermissionResultListener(this);
                ((MainActivity) getActivity()).requestPermissionStorage();
            }
        } else {
            //We have required permission now and lets populate the video from the selected
            // directory if the arraylist holding videos is empty
            if (videosList.isEmpty()) {
                File directory = new File(prefs.getString(getString(R.string.savelocation_key),
                        Environment.getExternalStorageDirectory()
                                + File.separator + "screenrecorder"));
                //Remove directory pointers and other files from the list
                if (!directory.exists()){
                    MainActivity.createDir();
                    Log.d(Const.TAG, "Directory missing! Creating dir");
                }
                ArrayList<File> filesList = new ArrayList<File>();
                if (directory.isDirectory() && directory.exists()) {
                    filesList.addAll(Arrays.asList(getVideos(directory.listFiles())));
                }
                //Read the videos and extract details from it in async.
                // This is essential if the directory contains huge number of videos

                progress.setIndeterminate(false);
                progress.setMax(filesList.size()-2);
                new GetVideosAsync().execute(filesList.toArray(new File[filesList.size()]));
            }
        }
    }

    //Method to strip off folders and other file types from the files list
    private File[] getVideos(File[] files) {
        List<File> newFiles = new ArrayList<>();
        for (File file : files) {
            if (!file.isDirectory() && isVideoFile(file.getPath()))
                newFiles.add(file);
        }
        return newFiles.toArray(new File[newFiles.size()]);
    }

    //Method to check if the file's meme type is video
    private static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    //Init recyclerview once the videos list is ready
    private void setRecyclerView(ArrayList<Video> videos) {
        videoRV.setHasFixedSize(true);
        final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        videoRV.setLayoutManager(layoutManager);
        final VideoRecyclerAdapter adapter = new VideoRecyclerAdapter(getActivity(), videos, this);
        videoRV.setAdapter(adapter);
        //Set the span to 1 (width to match the screen) if the view type is section
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.isSection(position) ? layoutManager.getSpanCount() : 1;
            }
        });
    }

    //Permission result callback method
    @Override
    public void onPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Const.EXTDIR_REQUEST_CODE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.d(Const.TAG, "Storage permission granted.");
                    //Performing storage task immediately after granting permission sometimes causes
                    //permission not taking effect.
                    checkPermission();
                } else {
                    Log.d(Const.TAG, "Storage permission denied.");
                    videoRV.setVisibility(View.GONE);
                    message.setText(R.string.video_list_permission_denied_message);
                }
                break;
        }
    }

    //Clear the videos ArrayList once the save directory is changed which forces reloading of videos from new directory
    public void removeVideosList() {
        videosList.clear();
        Log.d(Const.TAG, "Reached video fragment");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(Const.TAG, "Refresh data after edit!");
        removeVideosList();
        checkPermission();
    }

    //Class to retrieve video details in async
    //TODO: Translations
    class GetVideosAsync extends AsyncTask<File[], Integer, ArrayList<Video>> {
        //ProgressDialog progress;
        File[] files;
        ContentResolver resolver;

        GetVideosAsync() {
            resolver = getActivity().getApplicationContext().getContentResolver();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Show Progress bar
            progress.setProgress(0);
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(ArrayList<Video> videos) {
            //If the directory has no videos, remove recyclerview from rootview and show empty message.
            // Else set recyclerview and remove message textview
            if (videos.isEmpty()) {
                videoRV.setVisibility(View.GONE);
                message.setVisibility(View.VISIBLE);
            } else {
                //Sort the videos in a descending order
                Collections.sort(videos, Collections.<Video>reverseOrder());
                setRecyclerView(addSections(videos));
                videoRV.setVisibility(View.VISIBLE);
                message.setVisibility(View.GONE);
            }
            //Cancel the progress dialog
            progress.setVisibility(View.GONE);
        }

        //Add sections depending on the date the video is recorded to array list
        private ArrayList<Video> addSections(ArrayList<Video> videos) {
            ArrayList<Video> videosWithSections = new ArrayList<>();
            Date currentSection = new Date();
            Log.d(Const.TAG, "Original Length: " + videos.size());
            for (int i = 0; i < videos.size(); i++) {
                Video video = videos.get(i);
                //Add the first section arbitrarily
                if (i==0){
                    videosWithSections.add(new Video(true, video.getLastModified()));
                    videosWithSections.add(video);
                    currentSection = video.getLastModified();
                    continue;
                }
                if (addNewSection(currentSection, video.getLastModified())){
                    videosWithSections.add(new Video(true, video.getLastModified()));
                    currentSection = video.getLastModified();
                }
                videosWithSections.add(video);
            }
            Log.d(Const.TAG, "Length with sections: " + videosWithSections.size());
            return videosWithSections;
        }

        //Check if a new Section is to be added by comparing the difference of the section date
        // and the video's last modified date
        private boolean addNewSection(Date current, Date next)
        {
            Calendar currentSectionDate = toCalendar(current.getTime());
            Calendar nextVideoDate = toCalendar(next.getTime());

            // Get the represented date in milliseconds
            long milis1 = currentSectionDate.getTimeInMillis();
            long milis2 = nextVideoDate.getTimeInMillis();

            // Calculate difference in milliseconds
            int dayDiff = (int)Math.abs((milis2 - milis1) / (24 * 60 * 60 * 1000));
            Log.d(Const.TAG, "Date diff is: " + (dayDiff));
            return dayDiff > 0;
        }

        //Generate and return new Calendar object
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
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //Update progress dialog every time a video is finished processing
            progress.setProgress(values[0]);
            Log.d(Const.TAG, "Progress is :" + values[0]);
        }

        @Override
        protected ArrayList<Video> doInBackground(File[]... arg) {
            //Get video file name, Uri and video thumbnail from mediastore
            files = arg[0];
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (!file.isDirectory() && isVideoFile(file.getPath())) {
                    videosList.add(new Video(file.getName(),
                            file,
                            getBitmap(file),
                            new Date(file.lastModified())));
                    //Update progress dialog
                    publishProgress(i);
                }
            }
            return videosList;
        }

        //Method to get thumbnail from mediastore for video file
        Bitmap getBitmap(File file) {
            String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA};
            Cursor cursor = resolver.query(MediaStore.Video.Media.getContentUri("external"),
                    projection,
                    MediaStore.Images.Media.DATA + "=? ",
                    new String[]{file.getPath()}, null);

            if (cursor != null && cursor.moveToNext()) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int id = cursor.getInt(idColumn);
                Bitmap thumbNail = MediaStore.Video.Thumbnails.getThumbnail(resolver, id,
                        MediaStore.Video.Thumbnails.MINI_KIND, null);
                Log.d(Const.TAG, "Retrieved thumbnail for file: " + file.getName());
                cursor.close();
                return thumbNail;
            }
            return null;
        }
    }
}
