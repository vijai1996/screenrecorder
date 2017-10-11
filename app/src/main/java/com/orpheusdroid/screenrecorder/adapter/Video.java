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

import android.graphics.Bitmap;

import java.io.File;
import java.util.Date;

/**
 * Created by vijai on 07-11-2016.
 */

public class Video implements Comparable<Video> {
    private String FileName;
    private File file;
    private Bitmap thumbnail;
    private Date lastModified;
    private boolean isSection = false;

    public Video(boolean isSection, Date lastModified) {
        this.isSection = isSection;
        this.lastModified = lastModified;
    }

    public Video(String fileName, File file, Bitmap thumbnail, Date lastModified) {
        FileName = fileName;
        this.file = file;
        this.thumbnail = thumbnail;
        this.lastModified = lastModified;
    }

    public String getFileName() {
        return FileName;
    }

    public File getFile() {
        return file;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public boolean isSection() {
        return isSection;
    }

    @Override
    public int compareTo(Video video) {
        return getLastModified().compareTo(video.getLastModified());
    }
}
