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

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Calendar;

public class AboutActivity extends AppCompatActivity {

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String theme = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.preference_theme_key), Const.PREFS_LIGHT_THEME);
        switch (theme){
            case Const.PREFS_DARK_THEME:
                setTheme(R.style.AppTheme_Dark);
                break;
            case Const.PREFS_BLACK_THEME:
                setTheme(R.style.AppTheme_Black);
                break;
        }

        super.onCreate(savedInstanceState);

        // Set up arrow to close the activity
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_about);

        //Let's set the copyright and app version dynamically
        TextView appVersion = findViewById(R.id.versionTxt);
        TextView iconCredit = findViewById(R.id.icon_credit_tv);
        TextView videoEditorCredit = findViewById(R.id.video_editor_lib_credit_tv);
        TextView analyticsCredit = findViewById(R.id.analytics_lib_credit_tv);
        TextView openSourceInfo = findViewById(R.id.opensource_info_tv);

        iconCredit.setText(getString(R.string.app_icon_credit_Niko, "Niko Hörkkö", "http://nikosite.net"));
        videoEditorCredit.setText(getString(R.string.video_editor_library_credit, "knowledge4life",
                "https://github.com/knowledge4life/k4l-video-trimmer",
                "MIT Opensource License"));
        analyticsCredit.setText(getString(R.string.analytics_library_credit, "Countly",
                "https://github.com/Countly/countly-sdk-android",
                "MIT Opensource License"));
        openSourceInfo.setText(getString(R.string.opensource_info, "https://github.com/vijai1996/screenrecorder", "GNU AGPLv3"));

        //Let's build the copyright text using String builder
        StringBuilder copyRight = new StringBuilder();
        copyRight.append("Copyright &copy; orpheusdroid 2014-")
                .append(Calendar.getInstance().get(Calendar.YEAR))
                .append("\n");


        //If the apk is beta version include version code. Else ignore
        if (BuildConfig.VERSION_NAME.contains("Beta")) {
            copyRight.append(getResources().getString(R.string.app_name))
                    .append(" Build")
                    .append(BuildConfig.VERSION_CODE)
                    .append(" V")
                    .append(BuildConfig.VERSION_NAME)
                    .append("\n Internal Build. Not to be released");
            //set the text as html to get copyright symbol
            appVersion.setText(fromHtml(copyRight.toString()));
        } else {
            copyRight.append(getResources().getString(R.string.app_name))
                    .append(" V")
                    .append(BuildConfig.VERSION_NAME);
            //set the text as html to get copyright symbol
            appVersion.setText(fromHtml(copyRight.toString()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //finish this activity and return to parent activity
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
