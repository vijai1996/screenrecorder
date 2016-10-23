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
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public static final String APPDIR = "screenrecorder";
    private static final int SCREEN_RECORD_REQUEST_CODE = 1002;
    private PermissionResultListener mPermissionResultListener;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mProjectionManager;
    private FloatingActionButton fab;
    private enum Status{
      RECORDING, STOPPED
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Arbitrary "Write to external storage" permission since this permission is most important for the app
        requestPermissionStorage();

        //final boolean isServiceRunning = isServiceRunning(RecorderService.class);
        //Let's add SettingsPreferenceFragment to the activity
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager
                .beginTransaction();
        SettingsPreferenceFragment mPrefsFragment = new SettingsPreferenceFragment();
        mFragmentTransaction.replace(R.id.settingsFragment, mPrefsFragment);
        mFragmentTransaction.commit();

        //Acquiring media projection service to start screen mirroring
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        //Respond to app shortcut
        if(getIntent().getAction() != null && getIntent().getAction().equals(getString(R.string.app_shortcut_action))){
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), SCREEN_RECORD_REQUEST_CODE);
            return;
        }

        fab = (FloatingActionButton) findViewById(R.id.fab);

        if (isServiceRunning(RecorderService.class)){
            Log.d(Const.TAG, "service is running" );
            changeFabIcon(Status.RECORDING);
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaProjection == null && !isServiceRunning(RecorderService.class)) {
                    //Request Screen recording permission
                    startActivityForResult(mProjectionManager.createScreenCaptureIntent(), SCREEN_RECORD_REQUEST_CODE);
                } else if(isServiceRunning(RecorderService.class)){
                    //stop recording if the service is already active and recording
                    Intent stopRecording = new Intent(MainActivity.this, RecorderService.class);
                    stopRecording.setAction(Const.SCREEN_RECORDING_STOP);
                    startService(stopRecording);
                    changeFabIcon(Status.STOPPED);
                }
            }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //Show hint toast based on recording status
                if (isServiceRunning(RecorderService.class))
                    Toast.makeText(MainActivity.this, R.string.fab_record_hint, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MainActivity.this, R.string.fab_stop_hint, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

    }

    //Method to change FAB icon based on recording status
    private void changeFabIcon(Status status){
        if (status.equals(Status.RECORDING)){
            fab.setImageResource(R.drawable.ic_notification_stop);
        } else {
            fab.setImageResource(R.drawable.fab_record);
        }
    }

    //Method to check if the service is running
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //Overriding onActivityResult to capture screen mirroring permission request result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != SCREEN_RECORD_REQUEST_CODE) {
            Log.e(Const.TAG, "Unknown request code: " + requestCode);
            return;
        }

        //The user has denied permission for screen mirroring. Let's notify the user
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this,
                    getString(R.string.screen_recording_permission_denied), Toast.LENGTH_SHORT).show();
            //Return to home screen if the app was started from app shortcut
            if (getIntent().getAction().equals(getString(R.string.app_shortcut_action)))
                this.finish();
            return;
        }

        /*If code reaches this point, congratulations! The user has granted screen mirroring permission
        * Let us set the recorderservice intent with relevant data and start service*/
        changeFabIcon(Status.RECORDING);
        Intent recorderService = new Intent(this, RecorderService.class);
        recorderService.setAction(Const.SCREEN_RECORDING_START);
        recorderService.putExtra(Const.RECORDER_INTENT_DATA, data);
        recorderService.putExtra(Const.RECORDER_INTENT_RESULT, resultCode);
        startService(recorderService);
        this.finish();
    }

    //Method to create app directory which is default directory for storing recorded videos
    private void createDir() {
        File appDir = new File(Environment.getExternalStorageDirectory() + File.separator + APPDIR);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && !appDir.isDirectory()) {
            appDir.mkdirs();
        }
    }

    /* Marshmallow style permission request.
     * We also present the user with a dialog to notify why storage permission is required */
    public void requestPermissionStorage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.storage_permission_request_title))
                    .setMessage(getString(R.string.storage_permission_request_summary))
                    .setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    Const.EXTDIR_REQUEST_CODE);
                        }
                    });

            alert.create().show();
        }
    }

    // Marshmallor style permission request for audio recording
    public void requestPermissionAudio() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    Const.AUDIO_REQUEST_CODE);
        }
    }

    // Overriding onRequestPermissionsResult method to receive results of marshmallow style permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Const.EXTDIR_REQUEST_CODE:
                if ((grantResults.length > 0) &&
                        (grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
                    Log.d(Const.TAG, "write storage Permission Denied");
                    /* Disable floating action Button in case write storage permission is denied.
                     * There is no use in recording screen when the video is unable to be saved */
                    fab.setEnabled(false);
                } else {
                    /* Since we have write storage permission now, lets create the app directory
                    * in external storage*/
                    Log.d(Const.TAG, "write storage Permission granted");
                    createDir();
                }
        }

        // Let's also pass the result data to SettingsPreferenceFragment using the callback interface
        if (mPermissionResultListener != null) {
            mPermissionResultListener.onPermissionResult(requestCode, permissions, grantResults);
        }


    }

    //Set the callback interface for permission result from SettingsPreferenceFragment
    public void setPermissionResultListener(PermissionResultListener mPermissionResultListener) {
        this.mPermissionResultListener = mPermissionResultListener;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
