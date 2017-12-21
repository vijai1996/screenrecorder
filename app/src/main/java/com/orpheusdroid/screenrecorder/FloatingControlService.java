/*
 * Copyright (c) 2016-2017. Vijai Chandra Prasad R.
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

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.orpheusdroid.screenrecorder.Const.RecordingState;

/**
 * Created by vijai on 05-11-2016.
 */

public class FloatingControlService extends Service implements View.OnClickListener {

    private WindowManager windowManager;
    private LinearLayout floatingControls;
    private View controls;
    private ImageButton pauseIB;
    private ImageButton resumeIB;
    private IBinder binder = new ServiceBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        //Inflate the layout using LayoutInflater
        LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        floatingControls = (LinearLayout) li.inflate(R.layout.layout_floating_controls, null);
        controls = floatingControls.findViewById(R.id.controls);

        //Initialize imageButtons
        ImageButton stopIB = controls.findViewById(R.id.stop);
        pauseIB = controls.findViewById(R.id.pause);
        resumeIB = controls.findViewById(R.id.resume);
        resumeIB.setEnabled(false);

        stopIB.setOnClickListener(this);

        //Get floating control icon size from sharedpreference
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        //Pause/Resume doesnt work below SDK version 24. Remove them
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            pauseIB.setVisibility(View.GONE);
            resumeIB.setVisibility(View.GONE);
            controls.findViewById(R.id.divider1).setVisibility(View.GONE);
            controls.findViewById(R.id.divider2).setVisibility(View.GONE);
        } else {
            pauseIB.setOnClickListener(this);
            resumeIB.setOnClickListener(this);
        }

        //Set layout params to display the controls over any screen.
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                dpToPx(pref.getInt(getString(R.string.preference_floating_control_size_key), 100)),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // From API26, TYPE_PHONE depricated. Use TYPE_APPLICATION_OVERLAY for O
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            params.type = WindowManager.LayoutParams.TYPE_PHONE;

        //Initial position of the floating controls
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;

        //Add the controls view to windowmanager
        windowManager.addView(floatingControls, params);

        //Add touch listerner to floating controls view to move/close/expand the controls
        try {
            floatingControls.setOnTouchListener(new View.OnTouchListener() {
                boolean isMoving = false;
                private WindowManager.LayoutParams paramsF = params;
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            isMoving = false;
                            initialX = paramsF.x;
                            initialY = paramsF.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_UP:
                            if (!isMoving) {
                                if (controls.getVisibility() == View.INVISIBLE) {
                                    expandFloatingControls();
                                } else {
                                    collapseFloatingControls();
                                }
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            int xDiff = (int) (event.getRawX() - initialTouchX);
                            int yDiff = (int) (event.getRawY() - initialTouchY);
                            paramsF.x = initialX + xDiff;
                            paramsF.y = initialY + yDiff;
                            /* Set an offset of 10 pixels to determine controls moving. Else, normal touches
                             * could react as moving the control window around */
                            if (Math.abs(xDiff) > 10 || Math.abs(yDiff) > 10)
                                isMoving = true;
                            windowManager.updateViewLayout(floatingControls, paramsF);
                            break;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            // TODO: handle exception
        }
        return START_STICKY;
    }

    //Expand the floating window on touch
    private void expandFloatingControls() {
        controls.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        controls.measure(widthSpec, heightSpec);

        //Animate the expanding floating window
        ValueAnimator mAnimator = slideAnimator(0, controls.getMeasuredWidth());
        mAnimator.start();
    }

    private void collapseFloatingControls() {
        int finalHeight = controls.getWidth();

        ValueAnimator mAnimator = slideAnimator(finalHeight, 0);

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //Height=0, but it set visibility to INVISIBLE at the end of animation
                controls.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

        });
        mAnimator.start();
    }

    private ValueAnimator slideAnimator(int start, int end) {

        ValueAnimator animator = ValueAnimator.ofInt(start, end);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update width
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = controls.getLayoutParams();
                layoutParams.width = value;
                controls.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }

    //Onclick override to handle button clicks
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.stop:
                stopScreenSharing();
                break;
            case R.id.pause:
                pauseScreenRecording();
                break;
            case R.id.resume:
                resumeScreenRecording();
                break;
        }

        //Provide an haptic feedback on button press
        Vibrator vibrate = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        vibrate.vibrate(100);
    }

    /* Set resume intent and start the recording service
     * NOTE: A service can be started only once. Any subsequent startService only passes the intent
     * if any by calling onStartCommand */
    private void resumeScreenRecording() {
        Intent resumeIntent = new Intent(this, RecorderService.class);
        resumeIntent.setAction(Const.SCREEN_RECORDING_RESUME);
        startService(resumeIntent);
    }

    // Set pause intent and start the recording service
    private void pauseScreenRecording() {
        Intent pauseIntent = new Intent(this, RecorderService.class);
        pauseIntent.setAction(Const.SCREEN_RECORDING_PAUSE);
        startService(pauseIntent);
    }

    // Set stop intent and start the recording service
    private void stopScreenSharing() {
        Intent stopIntent = new Intent(this, RecorderService.class);
        stopIntent.setAction(Const.SCREEN_RECORDING_STOP);
        startService(stopIntent);
    }

    //Enable/disable pause/resume ImageButton depending on the current recording state
    public void setRecordingState(RecordingState state) {
        switch (state) {
            case PAUSED:
                pauseIB.setEnabled(false);
                resumeIB.setEnabled(true);
                break;
            case RECORDING:
                pauseIB.setEnabled(true);
                resumeIB.setEnabled(false);
                break;
        }
    }

    @Override
    public void onDestroy() {
        if (floatingControls != null) windowManager.removeView(floatingControls);
        super.onDestroy();
    }

    //Return ServiceBinder instance on successful binding
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(Const.TAG, "Binding successful!");
        return binder;
    }

    //Stop the service once the service is unbinded from recording service
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(Const.TAG, "Unbinding and stopping service");
        stopSelf();
        return super.onUnbind(intent);
    }

    //Method to convert dp to px
    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    //Binder class for binding to recording service
    public class ServiceBinder extends Binder {
        FloatingControlService getService() {
            return FloatingControlService.this;
        }
    }
}
