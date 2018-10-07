package cs371m.hermes.futuremessenger.ui.main.screens.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.List;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.pojo.MessageWithRecipients;
import cs371m.hermes.futuremessenger.ui.edit.screens.activities.EditTextMessageActivity;
import cs371m.hermes.futuremessenger.ui.edit.screens.dialogs.DeleteConfirmationDialog;

import static cs371m.hermes.futuremessenger.support.MessageDetailsViewBindingSupport.getConcatenatedRecipientNames;
import static cs371m.hermes.futuremessenger.support.MessageDetailsViewBindingSupport.getFormattedDateOnly;
import static cs371m.hermes.futuremessenger.support.MessageDetailsViewBindingSupport.getFormattedDayOnly;
import static cs371m.hermes.futuremessenger.support.MessageDetailsViewBindingSupport.getFormattedTimeOnly;
import static cs371m.hermes.futuremessenger.support.MessageDetailsViewBindingSupport.updateMessageContentTv;
import static cs371m.hermes.futuremessenger.support.MessageDetailsViewBindingSupport.updateRecipientsLabelTv;
import static cs371m.hermes.futuremessenger.support.MessageDetailsViewBindingSupport.updateRecipientsTv;
import static cs371m.hermes.futuremessenger.support.MessageDetailsViewBindingSupport.updateScheduledDateTv;
import static cs371m.hermes.futuremessenger.support.MessageDetailsViewBindingSupport.updateScheduledDayTv;
import static cs371m.hermes.futuremessenger.support.MessageDetailsViewBindingSupport.updateScheduledTimeTv;

public class ScheduledMessageOptionsDialog extends MessageOptionsDialog {


    public ScheduledMessageOptionsDialog() {
        // empty constructor
    }

    @Override
    protected View inflateAndReturnDialogLayout(LayoutInflater inflater) {
        View dialogLayout =
                inflater.inflate(R.layout.options_dialog_scheduled_message, null);

        // set the dialog's width to be 90% of the width of the screen
        Double dialogWidth = getContext().getResources().getDisplayMetrics().widthPixels * 0.90;
        dialogLayout.setMinimumWidth(dialogWidth.intValue());
        return dialogLayout;
    }

    @Override
    protected void setUpButtons(View dialogLayout) {
        dialogLayout.findViewById(R.id.edit_option).setOnClickListener(view -> {
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
