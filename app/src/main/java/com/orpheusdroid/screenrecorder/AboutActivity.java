package com.orpheusdroid.screenrecorder;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up arrow to close the activity
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_about);

        //Let's set the copyright and app version dynamically
        TextView appVersion = (TextView) findViewById(R.id.versionTxt);
        TextView iconCredit = (TextView) findViewById(R.id.icon_credit_tv);
        TextView videoEditorCredit = (TextView) findViewById(R.id.video_editor_lib_credit_tv);
        TextView analyticsCredit = (TextView) findViewById(R.id.analytics_lib_credit_tv);
        TextView openSourceInfo = (TextView) findViewById(R.id.opensource_info_tv);

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
        copyRight.append("Copyright &copy; orpheusdroid 2014-2016\n");


        //If the apk is beta version include version code. Else ignore
        if (BuildConfig.VERSION_NAME.contains("Beta")) {
            copyRight.append(getResources().getString(R.string.app_name))
                    .append(" V")
                    .append(BuildConfig.VERSION_CODE)
                    .append(" ")
                    .append(BuildConfig.VERSION_NAME);
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

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
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
