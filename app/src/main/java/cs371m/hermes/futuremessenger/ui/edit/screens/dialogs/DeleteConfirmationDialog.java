package cs371m.hermes.futuremessenger.ui.edit.screens.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.tasks.DeleteMessageAndRelatedData;

public class DeleteConfirmationDialog extends DialogFragment {

    public static String MESSAGE_ID_TO_DELETE_BUNDLE_KEY = "message_id_to_delete";

    private Long mMessageIDToDelete = Long.MIN_VALUE;

    public DeleteConfirmationDialog() {
        // empty constructor
    }

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        mMessageIDToDelete = args.getLong(MESSAGE_ID_TO_DELETE_BUNDLE_KEY);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View deleteConfirmationDialogLayout = inflateAndReturnDialogLayout(inflater);
        setUpButtons(deleteConfirmationDialogLayout);
        return deleteConfirmationDialogLayout;
    }

    private View inflateAndReturnDialogLayout(LayoutInflater inflater) {
        View deleteConfirmationDialogLayout = inflater.inflate(R.layout.delete_confirmation_dialog, null);

        // set the dialog's width to be 90% of the width of the screen
        Double dialogWidth = getContext().getResources().getDisplayMetrics().widthPixels * 0.90;
        deleteConfirmationDialogLayout.setMinimumWidth(dialogWidth.intValue());
        return deleteConfirmationDialogLayout;
    }

    private void setUpButtons(View deleteConfirmationDialogLayout) {
        AppCompatButton confirmDeleteButton = deleteConfirmationDialogLayout.findViewById(R.id.confirm_delete_button);
        confirmDeleteButton.setOnClickListener(view -> {
            dismiss();
            DeleteMessageAndRelatedData deleteTask = new DeleteMessageAndRelatedData();
            deleteTask.setArguments(
                    getContext(),
                    AppDatabase.getInstance(getContext()),
                    mMessageIDToDelete);
            deleteTask.execute();
        });

        AppCompatButton cancelButton = deleteConfirmationDialogLayout.findViewById(R.id.cancel_delete_button);
        cancelButton.setOnClickListener(view -> {
            dismiss();
        });
    }
}
