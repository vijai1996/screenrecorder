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
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.orpheusdroid.screenrecorder.adapter.Video;
import com.orpheusdroid.screenrecorder.adapter.VideoRecyclerAdapter;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vijai on 06-11-2016.
 */

public class VideosListFragment extends Fragment implements PermissionResultListener {
    private RecyclerView videoRV;
    private TextView message;
    private SharedPreferences prefs;
    private ArrayList<Video> videosList = new ArrayList<>();

    public VideosListFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_videos, container, false);
        message = (TextView) view.findViewById(R.id.message_tv);
        videoRV = (RecyclerView) view.findViewById(R.id.videos_rv);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                File[] files = getVideos(directory.listFiles());
                //Read the videos and extract details from it in async.
                // This is essential if the directory contains huge number of videos
                new GetVideosAsync().execute(files);
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
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        videoRV.setLayoutManager(layoutManager);
        VideoRecyclerAdapter adapter = new VideoRecyclerAdapter(getActivity(), videos);
        videoRV.setAdapter(adapter);
    }

    //Permission result callback method
    @Override
    public void onPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Const.EXTDIR_REQUEST_CODE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.d(Const.TAG, "Storage permission granted.");
                    new GetVideosAsync().execute();
                } else {
                    Log.d(Const.TAG, "Storage permission denied.");
                    videoRV.setVisibility(View.GONE);
                    message.setText(R.string.video_list_permission_denied_message);
                }
                break;
        }
    }

    //Clear the videos ArrayList once the save directory is changed which forces reloading of videos from new directory
    public void removeVideosList(){
        videosList.clear();
        Log.d(Const.TAG, "Reached video fragment");
    }

    //Class to retrieve video details in async
    //TODO: Translations
    class GetVideosAsync extends AsyncTask<File[], Integer, ArrayList<Video>> {
        ProgressDialog progress;
        File[] files;
        ContentResolver resolver;

        GetVideosAsync() {
            resolver = getActivity().getApplicationContext().getContentResolver();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Create a progress dialog to notify the user of current progress
            progress = new ProgressDialog(getActivity());
            progress.setTitle("Please wait");
            progress.setMessage("The videos are being loaded");
            progress.setCancelable(false);
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.show();
        }

        @Override
        protected void onPostExecute(ArrayList<Video> videos) {
            //If the directory has no videos, remove recyclerview from rootview and show empty message.
            // Else set recyclerview and remove message textview
            if (videos.isEmpty()){
                videoRV.setVisibility(View.GONE);
                message.setVisibility(View.VISIBLE);
            } else {
                setRecyclerView(videos);
                videoRV.setVisibility(View.VISIBLE);
                message.setVisibility(View.GONE);
            }
            //Cancel the progress dialog
            progress.cancel();
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
            progress.setMax(files.length - 2);
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (!file.isDirectory() && isVideoFile(file.getPath())) {
                    videosList.add(new Video(file.getName(),
                            Uri.fromFile(file),
                            getBitmap(file)));
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
