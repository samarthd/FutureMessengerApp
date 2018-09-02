package cs371m.hermes.futuremessenger.ui.draft.screens.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import org.apache.commons.lang3.StringUtils;

import cs371m.hermes.futuremessenger.R;

public class NewRecipientDialogFragment extends android.support.v4.app.DialogFragment {

    public interface NewRecipientInfoSaveListener {
        void onSaveNewManualRecipient(String name, String phoneNumber);
    }

    private EditText mRecipientNameInput;

    private EditText mRecipientPhoneNumberInput;

    public NewRecipientDialogFragment() {
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
        View recipientInputDialogLayout = inflateAndReturnDialogLayout(inflater);

        setUpInputFields(recipientInputDialogLayout);
        setUpButtons(recipientInputDialogLayout);

        return recipientInputDialogLayout;
    }

    private View inflateAndReturnDialogLayout(LayoutInflater inflater) {
        View recipientInputDialogLayout = inflater.inflate(R.layout.new_recipient_input_dialog, null);

        // set the dialog's width to be 90% of the width of the screen
        Double dialogWidth = getContext().getResources().getDisplayMetrics().widthPixels * 0.90;
        recipientInputDialogLayout.setMinimumWidth(dialogWidth.intValue());
        return recipientInputDialogLayout;
    }

    private void setUpInputFields(View recipientInputDialogLayout) {
        mRecipientNameInput = recipientInputDialogLayout.findViewById(R.id.new_recipient_name_input);
        mRecipientPhoneNumberInput = recipientInputDialogLayout.findViewById(R.id.new_recipient_phone_number_input);

        mRecipientNameInput.requestFocus();
        mRecipientPhoneNumberInput.setInputType(InputType.TYPE_CLASS_PHONE);
        // format the phone number as it's entered
        mRecipientPhoneNumberInput.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
    }

    private void setUpButtons(View recipientInputDialogLayout) {
        AppCompatButton saveButton = recipientInputDialogLayout.findViewById(R.id.save_new_recipient_button);
        saveButton.setOnClickListener(view -> {
            NewRecipientInfoSaveListener listeningActivity = (NewRecipientInfoSaveListener) getActivity();
            String newRecipientName = mRecipientNameInput.getText().toString().trim();
            String newRecipientPhoneNumber = mRecipientPhoneNumberInput.getText().toString().trim();
            if (StringUtils.isEmpty(newRecipientName)) {
                mRecipientNameInput.setError(getString(R.string.error_blank));
                mRecipientNameInput.requestFocus();
            }
            else if (StringUtils.isEmpty(newRecipientPhoneNumber)){
                mRecipientPhoneNumberInput.setError(getString(R.string.error_blank));
                mRecipientPhoneNumberInput.requestFocus();
            }
            else {
                dismiss();
                listeningActivity.onSaveNewManualRecipient(newRecipientName, newRecipientPhoneNumber);
            }
        });

        AppCompatButton cancelButton = recipientInputDialogLayout.findViewById(R.id.cancel_new_recipient_button);
        cancelButton.setOnClickListener(view -> {
            dismiss();
        });
    }
}
