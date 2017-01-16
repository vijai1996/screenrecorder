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

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;

/**
 * Created by vijai on 03-12-2016.
 */

public class SavedStateHandler extends Preference.BaseSavedState {
    public static final Parcelable.Creator<SavedStateHandler> CREATOR = new Parcelable.Creator<SavedStateHandler>() {
        public SavedStateHandler createFromParcel(Parcel in) {
            return new SavedStateHandler(in);
        }

        public SavedStateHandler[] newArray(int size) {
            return new SavedStateHandler[size];
        }
    };
    public final String selectedDir;
    public final Bundle dialogState;

    public SavedStateHandler(Parcelable superState, String selectedDir, Bundle dialogState) {
        super(superState);
        this.selectedDir = selectedDir;
        this.dialogState = dialogState;
    }

    public SavedStateHandler(Parcel source) {
        super(source);
        selectedDir = source.readString();
        dialogState = source.readBundle();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(selectedDir);
        dest.writeBundle(dialogState);
    }
}
