package cs371m.hermes.futuremessenger.ui.edit.screens.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import cs371m.hermes.futuremessenger.R;

public class ExitConfirmationDialog extends DialogFragment {
    public ExitConfirmationDialog() {
        // empty constructor
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
        View exitConfirmationDialogLayout = inflateAndReturnDialogLayout(inflater);
        setUpButtons(exitConfirmationDialogLayout);
        return exitConfirmationDialogLayout;
    }

    private View inflateAndReturnDialogLayout(LayoutInflater inflater) {
        View exitConfirmationDialogLayout = inflater.inflate(R.layout.exit_confirmation_dialog, null);

        // set the dialog's width to be 90% of the width of the screen
        Double dialogWidth = getContext().getResources().getDisplayMetrics().widthPixels * 0.90;
        exitConfirmationDialogLayout.setMinimumWidth(dialogWidth.intValue());
        return exitConfirmationDialogLayout;
    }

    private void setUpButtons(View exitConfirmationDialogLayout) {
        AppCompatButton confirmExitButton = exitConfirmationDialogLayout.findViewById(R.id.confirm_exit_button);
        confirmExitButton.setOnClickListener(view -> {
            getActivity().finish();
            dismiss();
        });

        AppCompatButton cancelButton = exitConfirmationDialogLayout.findViewById(R.id.cancel_exit_button);
        cancelButton.setOnClickListener(view -> {
            dismiss();
        });
    }
}
