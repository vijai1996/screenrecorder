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

package com.orpheusdroid.screenrecorder.adapter;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

/**
 * Created by vijai on 08-12-2017.
 */

public class Apps implements Comparable<Apps> {
    private String appName;
    private String packageName;
    private Drawable appIcon;
    private boolean isSelectedApp;

    public Apps(String appName, String packageName, Drawable appIcon) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
    }

    String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    boolean isSelectedApp() {
        return isSelectedApp;
    }

    public void setSelectedApp(boolean selectedApp) {
        isSelectedApp = selectedApp;
    }

    @Override
    public int compareTo(@NonNull Apps apps) {
        return appName.compareTo(apps.appName);
    }
}
