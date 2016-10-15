package com.ls.directoryselector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditTextDialog extends DialogFragment {

    public static final String EDIT_VALUE_KEY = "value";
    private static final String TITLE_KEY = "title";
    private static final String MESSAGE_KEY = "message";
    private String title;
    private String message;

    private AlertDialog dialog;

    public static EditTextDialog newInstance(Fragment targetFragment, int requestCode, String title, String message) {
        EditTextDialog ret = new EditTextDialog();
        Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);
        args.putString(MESSAGE_KEY, message);
        ret.setArguments(args);
        ret.setTargetFragment(targetFragment, requestCode);
        return ret;
    }

    private static Intent getReturnIntent(String result) {
        Intent ret = new Intent();
        ret.putExtra(EDIT_VALUE_KEY, result);
        return ret;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            title = getArguments().getString(TITLE_KEY);
            message = getArguments().getString(MESSAGE_KEY);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater li = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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


        AlertDialog.Builder ab = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setView(view)
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
                            }
                        })
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                String editValue = input.getText().toString().trim();
                                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getReturnIntent(editValue));
                            }
                        });

        dialog = ab.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        return dialog;
    }
}
