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

/**
 * Created by vijai on 12-10-2016.
 */

// POJO class for bunch of statics used across the app
class Const {
    static final int EXTDIR_REQUEST_CODE = 1000;
    static final int AUDIO_REQUEST_CODE = 1001;
    static final int SYSTEM_WINDOWS_CODE = 1002;
    static final String SCREEN_RECORDING_START = "com.orpheusdroid.screenrecorder.services.action.startrecording";
    static final String SCREEN_RECORDING_PAUSE = "com.orpheusdroid.screenrecorder.services.action.pauserecording";
    static final String SCREEN_RECORDING_RESUME = "com.orpheusdroid.screenrecorder.services.action.resumerecording";
    static final String SCREEN_RECORDING_STOP = "com.orpheusdroid.screenrecorder.services.action.stoprecording";
    static final int SCREEN_RECORDER_NOTIFICATION_ID = 5001;
    static final int SCREEN_RECORDER_SHARE_NOTIFICATION_ID = 5002;
    static final String RECORDER_INTENT_DATA = "recorder_intent_data";
    static final String RECORDER_INTENT_RESULT = "recorder_intent_result";
    static final String TAG = "SCREENRECORDER";

    public enum RecordingState {
        RECORDING, PAUSED, STOPPED
    }
}
