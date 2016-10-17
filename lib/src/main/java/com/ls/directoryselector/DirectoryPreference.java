package com.ls.directoryselector;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;

public class DirectoryPreference extends DialogPreference {
    private final DirectorySelector.Callback dirSelectorCallback = new DirectorySelector.Callback() {
        @Override
        public void onNewDirButtonClicked() {
            createNewFolderDialog(null);
        }
    };

    private final DirectorySelector dirChooser = new DirectorySelector(dirSelectorCallback) {
        @Override
        protected Context getContext() {
            return DirectoryPreference.this.getContext();
        }

        @Override
        protected File getInitialDirectory() {
            File ret = null;
            String value = getPersistedString(null);
            if (value != null) {
                File file = new File(value);
                if (file.exists() && file.isDirectory()) ret = file;
            }
            if (ret == null) ret = Environment.getExternalStorageDirectory();
            return ret;
        }
    };
    private AlertDialog dialog;

    public DirectoryPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public DirectoryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        dirChooser.initViews(view);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            String value = dirChooser.getSelectedDir().getPath();
            persistString(value);
            callChangeListener(value);
        }
    }

    private void init(Context context) {
        setPersistent(true);
        setDialogTitle(null);
        setDialogLayoutResource(dirChooser.getViewResId());
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
    }

    @Override
    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        dirChooser.onResume();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        dirChooser.onPause();
        super.onDismiss(dialog);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        File selectedDir = dirChooser.getSelectedDir();
        if (selectedDir == null) return superState;
        Bundle dialogState = dialog == null ? null : dialog.onSaveInstanceState();
        SavedState myState = new SavedState(superState, selectedDir.getPath(), dialogState);
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        dirChooser.setSelectedDir(myState.selectedDir);
        if (myState.dialogState != null) {
            // recreate dialog
            createNewFolderDialog(myState.dialogState);
        }
    }

    @Override
    public void onActivityDestroy() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        super.onActivityDestroy();
    }

    private void createNewFolderDialog(Bundle savedState) {
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = li.inflate(R.layout.edit_text_layout, null);
        final EditText input = (EditText) view.findViewById(R.id.edit_value);
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
                .setTitle(R.string.create_folder)
                .setMessage(R.string.create_folder_msg)
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
                                if (!dirName.isEmpty()) dirChooser.createFolder(dirName);
                            }
                        });

        dialog = ab.create();
        if (savedState != null) dialog.onRestoreInstanceState(savedState);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!input.getText().toString().trim().isEmpty());
    }

    private static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        public final String selectedDir;
        public final Bundle dialogState;

        public SavedState(Parcelable superState, String selectedDir, Bundle dialogState) {
            super(superState);
            this.selectedDir = selectedDir;
            this.dialogState = dialogState;
        }

        public SavedState(Parcel source) {
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
}
