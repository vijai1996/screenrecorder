package com.ls.directoryselector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import java.io.File;

public class DirectoryDialog extends DialogFragment {

    private static final String INITIAL_DIR_KEY = "initial_dir";
    private static final String SELECTED_DIR_KEY = "selected_dir";
    private static final int CREATE_DIR_CODE = 1000;
    private final DirectorySelector.Callback dirSelectorCallback = new DirectorySelector.Callback() {
        @Override
        public void onNewDirButtonClicked() {
            EditTextDialog dialog = EditTextDialog.newInstance(DirectoryDialog.this, CREATE_DIR_CODE,
                    getString(R.string.create_folder), getString(R.string.create_folder_msg));
            dialog.show(getFragmentManager(), "createDirDialog");
        }
    };
    private final DirectorySelector dirChooser = new DirectorySelector(dirSelectorCallback) {
        @Override
        protected Context getContext() {
            return getActivity();
        }

        @Override
        protected File getInitialDirectory() {
            return Environment.getExternalStorageDirectory();
        }
    };
    private Listener listener;
    private String selectedDir;

    public DirectoryDialog() {
    }

    public static DirectoryDialog newInstance(String initialDirectory) {
        DirectoryDialog ret = new DirectoryDialog();
        Bundle args = new Bundle();
        args.putString(INITIAL_DIR_KEY, initialDirectory);
        ret.setArguments(args);
        return ret;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        File selectedDir = dirChooser.getSelectedDir();
        if (selectedDir != null) {
            outState.putString(SELECTED_DIR_KEY, selectedDir.getPath());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectedDir = null;
        if (getArguments() != null) {
            selectedDir = getArguments().getString(INITIAL_DIR_KEY);
        }

        if (savedInstanceState != null) {
            selectedDir = savedInstanceState.getString(SELECTED_DIR_KEY);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (listener != null)
                                    listener.onDirectorySelected(dirChooser.getSelectedDir());
                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (listener != null) listener.onCancelled();
                            }
                        }
                );

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(dirChooser.getViewResId(), null);
        dirChooser.initViews(view);
        if (!TextUtils.isEmpty(selectedDir)) dirChooser.setSelectedDir(selectedDir);

        builder.setView(view);

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Listener) listener = (Listener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        dirChooser.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        dirChooser.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CREATE_DIR_CODE:
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    String value = data.getStringExtra(EditTextDialog.EDIT_VALUE_KEY);
                    dirChooser.createFolder(value);
                }
                break;
        }
    }

    public interface Listener {
        void onDirectorySelected(File dir);

        void onCancelled();
    }
}
