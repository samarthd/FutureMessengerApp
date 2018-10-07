package cs371m.hermes.futuremessenger.ui.main.screens.dialogs;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.persistence.pojo.MessageWithRecipients;
import cs371m.hermes.futuremessenger.ui.edit.screens.activities.EditTextMessageActivity;

public class SentMessageOptionsDialog extends MessageOptionsDialog {

    public SentMessageOptionsDialog() {
        // empty constructor
    }

    @Override
    protected View inflateAndReturnDialogLayout(LayoutInflater inflater) {
        View dialogLayout =
                inflater.inflate(R.layout.options_dialog_sent_message, null);

        // set the dialog's width to be 90% of the width of the screen
        Double dialogWidth = getContext().getResources().getDisplayMetrics().widthPixels * 0.90;
        dialogLayout.setMinimumWidth(dialogWidth.intValue());
        return dialogLayout;
    }

    @Override
    protected void setUpButtons(View dialogLayout) {
        dialogLayout.findViewById(R.id.resend_option).setOnClickListener(view -> {
            dismiss();
            Intent intent = new Intent(getContext(), EditTextMessageActivity.class);
            intent.putExtra(MessageWithRecipients.BUNDLE_KEY_MESSAGE_WITH_RECIPIENTS,
                    mMessageWithRecipients);
            getContext().startActivity(intent);
        });
        dialogLayout.findViewById(R.id.delete_option).setOnClickListener(view -> {
            dismiss();
            launchDeleteConfirmationDialog();
        });
    }

}
