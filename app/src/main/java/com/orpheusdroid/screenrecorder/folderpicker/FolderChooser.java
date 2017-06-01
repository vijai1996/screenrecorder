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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.orpheusdroid.screenrecorder.Const;
import com.orpheusdroid.screenrecorder.R;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by vijai on 01-12-2016.
 */

public class FolderChooser extends DialogPreference implements View.OnClickListener,
        DirectoryRecyclerAdapter.OnDirectoryClickedListerner, AdapterView.OnItemSelectedListener {
    private RecyclerView rv;
    private TextView tv_currentDir;
    private TextView tv_empty;
    private File currentDir;
    private ArrayList<File> directories;
    private AlertDialog dialog;
    private DirectoryRecyclerAdapter adapter;
    private Spinner spinner;
    private static OnDirectorySelectedListerner onDirectorySelectedListerner;
    private List<Storages> storages = new ArrayList<>();
    private boolean isExternalStorageSelected = false;
    private SharedPreferences prefs;

    public FolderChooser(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(Const.TAG, "Constructor called");
        initialize();
    }

    private void initialize() {
        setPersistent(true);
        setDialogTitle(null);
        setDialogLayoutResource(R.layout.director_chooser);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        currentDir = new File(Environment.getExternalStorageDirectory() + File.separator + Const.APPDIR);
        setSummary(getPersistedString(currentDir.getPath()));
        Log.d(Const.TAG, "Persisted String is: " + getPersistedString(currentDir.getPath()));
        File[] SDCards = ContextCompat.getExternalFilesDirs(getContext().getApplicationContext(), null);
        storages.add(new Storages(Environment.getExternalStorageDirectory().getPath(), Storages.StorageType.Internal));
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (SDCards.length > 1)
            storages.add(new Storages(SDCards[1].getPath(), Storages.StorageType.External));
        //getRemovableSDPath(SDCards[1]);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        generateFoldersList();
        initView(view);
        initRecyclerView();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            Log.d(Const.TAG, "Directory choosed! " + currentDir.getPath());
            if (!currentDir.canWrite()) {
                Toast.makeText(getContext(), "Cannot write to selected directory. Path will not be saved.", Toast.LENGTH_SHORT).show();
                return;
            }
            persistString(currentDir.getPath());
            onDirectorySelectedListerner.onDirectorySelected();
            setSummary(currentDir.getPath());
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (currentDir == null) return superState;
        Bundle dialogState = dialog == null ? null : dialog.onSaveInstanceState();
        return new SavedStateHandler(superState, currentDir.getPath(), dialogState);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedStateHandler.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedStateHandler myState = (SavedStateHandler) state;
        super.onRestoreInstanceState(myState.getSuperState());

        setCurrentDir(currentDir.getPath());
        if (myState.dialogState != null) {
            // recreate dialog
            newDirDialog(myState.dialogState);
        }
    }

    private void initRecyclerView() {
        rv.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), layoutManager.getOrientation());
        rv.addItemDecoration(dividerItemDecoration);
        if (!isDirectoryEmpty()) {
            adapter = new DirectoryRecyclerAdapter(getContext(), this, directories);
            rv.setAdapter(adapter);
        }
        tv_currentDir.setText(currentDir.getPath());
    }

    private boolean isDirectoryEmpty() {
        if (directories.isEmpty()) {
            rv.setVisibility(View.GONE);
            tv_empty.setVisibility(View.VISIBLE);
            return true;
        } else {
            rv.setVisibility(View.VISIBLE);
            tv_empty.setVisibility(View.GONE);
            return false;
        }
    }

    private void generateFoldersList() {
        File[] dir = currentDir.listFiles(new DirectoryFilter());
        directories = new ArrayList<>(Arrays.asList(dir));
        Collections.sort(directories, new SortFileName());
        Log.d(Const.TAG, "Directory size " + directories.size());
    }

    private void initView(View view) {
        ImageButton up = (ImageButton) view.findViewById(R.id.nav_up);
        ImageButton createDir = (ImageButton) view.findViewById(R.id.create_dir);
        tv_currentDir = (TextView) view.findViewById(R.id.tv_selected_dir);
        rv = (RecyclerView) view.findViewById(R.id.rv);
        tv_empty = (TextView) view.findViewById(R.id.tv_empty);
        spinner = (Spinner) view.findViewById(R.id.storageSpinner);
        up.setOnClickListener(this);
        createDir.setOnClickListener(this);
        ArrayList<String> StorageStrings = new ArrayList<>();
        for (Storages storage : storages) {
            String storageType = storage.getType() == Storages.StorageType.Internal ? "Internal Storage" :
                    "Removable Storage";
            StorageStrings.add(storageType);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, StorageStrings);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(this);
    }

    private void changeDirectory(File file) {
        currentDir = file;
        Log.d(Const.TAG, "Changed dir is: " + file.getPath());
        generateFoldersList();
        if (!isDirectoryEmpty()) {
            adapter = new DirectoryRecyclerAdapter(getContext(), this, directories);
            rv.swapAdapter(adapter, true);
        }
        tv_currentDir.setText(currentDir.getPath());
    }

    public void setCurrentDir(String currentDir) {
        File dir = new File(currentDir);
        if (dir.exists() && dir.isDirectory()) {
            this.currentDir = dir;
            Log.d(Const.TAG, "Directory set");
        } else {
            createFolder(dir.getPath());
            Log.d(Const.TAG, "Directory created");
        }
    }

    public void setOnDirectoryClickedListerner(OnDirectorySelectedListerner onDirectoryClickedListerner) {
        FolderChooser.onDirectorySelectedListerner = onDirectoryClickedListerner;
    }

    private void newDirDialog(Bundle savedState) {
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = li.inflate(R.layout.directory_chooser_edit_text, null);
        final EditText input = (EditText) view.findViewById(R.id.et_new_folder);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (dialog != null) {
                    Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setEnabled(!s.toString().trim().isEmpty());
                }
            }
        });

        AlertDialog.Builder ab = new AlertDialog.Builder(getContext())
                .setTitle(R.string.alert_title_create_folder)
                .setMessage(R.string.alert_message_create_folder)
                .setView(view)
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                String dirName = input.getText().toString().trim();
                                if (!dirName.isEmpty()) createFolder(dirName);
                            }
                        });

        dialog = ab.create();
        if (savedState != null) dialog.onRestoreInstanceState(savedState);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!input.getText().toString().trim().isEmpty());
    }

    private boolean createFolder(String dirName) {
        if (currentDir == null) {
            Toast.makeText(getContext(), "No directory selected", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!currentDir.canWrite()) {
            Toast.makeText(getContext(), "No permission to write to directory", Toast.LENGTH_SHORT).show();
            return false;
        }

        File newDir;
        if (dirName.contains(Environment.getExternalStorageDirectory().getPath()))
            newDir = new File(dirName);
        else
            newDir = new File(currentDir, dirName);
        if (newDir.exists()) {
            Toast.makeText(getContext(), "Directory already exists", Toast.LENGTH_SHORT).show();
            changeDirectory(new File(currentDir, dirName));
            return false;
        }

        if (!newDir.mkdir()) {
            Toast.makeText(getContext(), "Error creating directory", Toast.LENGTH_SHORT).show();
            Log.d(Const.TAG, newDir.getPath());
            return false;
        }

        changeDirectory(new File(currentDir, dirName));

        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.nav_up:
                File parentDirectory = new File(currentDir.getParent());
                Log.d(Const.TAG, parentDirectory.getPath());
                if (!isExternalStorageSelected) {
                    if (parentDirectory.getPath().contains(storages.get(0).getPath()))
                        changeDirectory(parentDirectory);
                } else
                    changeExternalDirectory(parentDirectory);
                return;
            case R.id.create_dir:
                newDirDialog(null);
                return;
        }
    }

    private void changeExternalDirectory(File parentDirectory) {
        String externalBaseDir = getRemovableSDPath(storages.get(1).getPath());
        if (parentDirectory.getPath().contains(externalBaseDir) && parentDirectory.canWrite())
            changeDirectory(parentDirectory);
        else if (parentDirectory.getPath().contains(externalBaseDir) && !parentDirectory.canWrite())
            Toast.makeText(getContext(), R.string.external_storage_dir_not_writable, Toast.LENGTH_SHORT).show();
    }

    private String getRemovableSDPath(String pathSD) {
        //String pathSD = file.toString();
        int index = pathSD.indexOf("Android");
        Log.d(Const.TAG, "Short code is: " + pathSD.substring(0, index));
        String filename = pathSD.substring(0, index - 1);
        Log.d(Const.TAG, "External Base Dir " + filename);
        return filename;
    }

    @Override
    public void OnDirectoryClicked(File directory) {
        changeDirectory(directory);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(Const.TAG, "Selected storage is: " + storages.get(i));
        isExternalStorageSelected = (storages.get(i).getType() == Storages.StorageType.External);
        if (isExternalStorageSelected && !prefs.getBoolean(Const.ALERT_EXTR_STORAGE_CB_KEY, false)){
            showExtDirAlert();
        }
        changeDirectory(new File(storages.get(i).getPath()));
    }

    private void showExtDirAlert() {
        View checkBoxView = View.inflate(getContext(), R.layout.alert_checkbox, null);
        final CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.donot_warn_cb);
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.alert_ext_dir_warning_title)
                .setMessage(R.string.alert_ext_dir_warning_message)
                .setView(checkBoxView)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (checkBox.isChecked())
                            prefs.edit().putBoolean(Const.ALERT_EXTR_STORAGE_CB_KEY, true).apply();
                    }
                })
                .create().show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private class DirectoryFilter implements FileFilter {
        @Override
        public boolean accept(File file) {
            return file.isDirectory() && !file.isHidden();
        }
    }

    //sorts based on the files name
    private class SortFileName implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
        }
    }
}
