/*
 * Copyright (c) 2016-2018. Vijai Chandra Prasad R.
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

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

/**
 * Todo: Add class description here
 *
 * @author Vijai Chandra Prasad .R
 */

@TargetApi(24)
public class QuickRecordTile extends TileService {
    private boolean isTileActive;

    @Override
    public void onStartListening() {
        super.onStartListening();
        isTileActive = isServiceRunning(RecorderService.class);
        changeTileState();
    }

    @Override
    public void onClick() {
        Tile tile = getQsTile();
        isTileActive = !(tile.getState() == Tile.STATE_ACTIVE);
        changeTileState();
        if (isTileActive) {
            startActivity(new Intent(this, MainActivity.class).setAction(getString(R.string.app_shortcut_action)));
        } else {
            startService(new Intent(this, RecorderService.class).setAction(Const.SCREEN_RECORDING_STOP));
        }
        isTileActive = !isTileActive;
    }

    private void changeTileState() {
        Tile tile = super.getQsTile();
        int activeState = isTileActive ?
                Tile.STATE_ACTIVE : Tile.STATE_INACTIVE;

        if (!isTileActive)
            tile.setLabel(getString(R.string.quick_settings_tile_start_title));
        else
            tile.setLabel(getString(R.string.quick_settings_tile_stop_title));

        tile.setState(activeState);
        tile.updateTile();

    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
