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

package com.orpheusdroid.screenrecorder.folderpicker;

/**
 * Created by vijai on 24-05-2017.
 */

public class Storages {
    private String path;
    private StorageType type;
    public Storages(String path, StorageType type) {
        this.path = path;
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public StorageType getType() {
        return type;
    }

    public enum StorageType {Internal, External}
}
