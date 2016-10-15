package com.ls.directoryselector;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.FileObserver;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ls.directoryselector.utils.DirectoryFileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

abstract class DirectorySelector {

    private static final String TAG = DirectorySelector.class.getSimpleName();
    private final Handler handler = new Handler();
    private final Callback callback;
    private final ArrayList<File> files = new ArrayList<>();
    private ImageButton btnNavUp;
    private ImageButton btnCreateFolder;
    private TextView txtSelectedFolder;
    private ListView listDirectories;

    private FileAdapter listAdapter;
    private File selectedDir;
    private FileObserver fileObserver;
    private final AdapterView.OnItemClickListener listClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            changeDirectory(listAdapter.getItem(position));
        }
    };
    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_nav_up) {
                changeUp();
            } else if (v.getId() == R.id.btn_create_folder) {
                callback.onNewDirButtonClicked();
            }
        }
    };

    protected DirectorySelector(Callback callback) {
        this.callback = callback;
    }

    protected abstract Context getContext();

    protected abstract File getInitialDirectory();

    protected int getViewResId() {
        return R.layout.directory_chooser;
    }

    protected void onPause() {
        if (fileObserver != null) fileObserver.stopWatching();
    }

    protected void onResume() {
        if (fileObserver != null) fileObserver.startWatching();
    }

    protected File getSelectedDir() {
        return selectedDir;
    }

    protected void setSelectedDir(String path) {
        changeDirectory(new File(path));
    }

    protected boolean createFolder(String dirName) {
        if (selectedDir == null) {
            showToast(R.string.no_dir_selected);
            return false;
        }
        if (!selectedDir.canWrite()) {
            showToast(R.string.no_write_access);
            return false;
        }

        File newDir = new File(selectedDir, dirName);
        if (newDir.exists()) {
            showToast(R.string.error_already_exists);
            return false;
        }

        if (!newDir.mkdir()) {
            showToast(R.string.create_folder_error);
            return false;
        }

        changeDirectory(new File(selectedDir, dirName));

        return true;
    }

    protected void initViews(View view) {
        btnNavUp = (ImageButton) view.findViewById(R.id.btn_nav_up);
        btnCreateFolder = (ImageButton) view.findViewById(R.id.btn_create_folder);
        txtSelectedFolder = (TextView) view.findViewById(R.id.txt_selected_folder);
        listDirectories = (ListView) view.findViewById(R.id.list_dirs);
        listDirectories.setEmptyView(view.findViewById(R.id.txt_list_empty));

        listDirectories.setOnItemClickListener(listClickListener);
        btnNavUp.setOnClickListener(clickListener);
        btnCreateFolder.setOnClickListener(clickListener);

        adjustImages();

        listAdapter = new FileAdapter(getContext(), files);
        listDirectories.setAdapter(listAdapter);

        changeDirectory(getInitialDirectory());
    }

    private void adjustImages() {
        // change up button to light version if using dark theme
        int color = 0xFFFFFF;
        Resources.Theme theme = getContext().getTheme();
        if (theme != null) {
            TypedArray ba = theme.obtainStyledAttributes(new int[]{android.R.attr.colorBackground});
            if (ba != null) {
                color = ba.getColor(0, 0xFFFFFF);
                ba.recycle();
            }
        }

        // convert to greyscale and check if < 128
        if ((color != 0xFFFFFF) && (0.21 * Color.red(color) + 0.71 * Color.green(color) + 0.07 * Color.blue(color) < 128)) {
            btnNavUp.setImageResource(R.drawable.navigation_up_light);
            btnCreateFolder.setImageResource(R.drawable.ic_action_create_light);
        }
    }

    private void changeUp() {
        File parent;
        if ((selectedDir != null) && (parent = selectedDir.getParentFile()) != null) {
            changeDirectory(parent);
        }
    }

    private void changeDirectory(File dir) {
        if ((dir == null) || !dir.isDirectory()) return;

        File[] files = dir.listFiles(new DirectoryFileFilter());
        List<File> filesList = files != null ? Arrays.asList(files) : new ArrayList<File>();

        this.files.clear();
        this.files.addAll(filesList);
        Collections.sort(this.files);
        if (listAdapter != null) listAdapter.notifyDataSetChanged();

        selectedDir = dir;
        txtSelectedFolder.setText(dir.getAbsolutePath());

        if (fileObserver != null) fileObserver.stopWatching();
        fileObserver = createFileObserver(dir.getAbsolutePath());
        fileObserver.startWatching();

        Log.d(TAG, "Changed directory to " + dir.getAbsolutePath());
    }

    private void refreshDirectory() {
        if (selectedDir != null) changeDirectory(selectedDir);
    }

    private FileObserver createFileObserver(String path) {
        return new FileObserver(path, FileObserver.CREATE | FileObserver.DELETE | FileObserver.MOVED_FROM | FileObserver.MOVED_TO |
                FileObserver.DELETE_SELF | FileObserver.MOVE_SELF) {
            @Override
            public void onEvent(int event, String path) {
                Log.d(TAG, "FileObserver received event " + event);
                if (((event & FileObserver.CREATE) != 0) ||
                        ((event & FileObserver.DELETE) != 0) ||
                        ((event & FileObserver.MOVED_FROM) != 0) ||
                        ((event & FileObserver.MOVED_TO) != 0)) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            refreshDirectory();
                        }
                    });
                } else if (((event & FileObserver.DELETE_SELF) != 0) || ((event & FileObserver.MOVE_SELF) != 0)) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            changeUp();
                        }
                    });
                }
            }
        };
    }

    private void showToast(int resId) {
        Toast.makeText(getContext(), resId, Toast.LENGTH_LONG).show();
    }

    public interface Callback {
        void onNewDirButtonClicked();
    }
}
