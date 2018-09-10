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

public class ScheduledMessageOptionsDialog extends DialogFragment {

    private MessageWithRecipients mMessageWithRecipients;

    public ScheduledMessageOptionsDialog() {
        // empty constructor
    }

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        mMessageWithRecipients =
                (MessageWithRecipients) args.getSerializable(MessageWithRecipients.BUNDLE_KEY_MESSAGE_WITH_RECIPIENTS);
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
        View dialogLayout = inflateAndReturnDialogLayout(inflater);
        setUpOptionsDialog(dialogLayout);
        return dialogLayout;
    }

    private View inflateAndReturnDialogLayout(LayoutInflater inflater) {
        View dialogLayout =
                inflater.inflate(R.layout.options_dialog_scheduled_message, null);

        // set the dialog's width to be 90% of the width of the screen
        Double dialogWidth = getContext().getResources().getDisplayMetrics().widthPixels * 0.90;
        dialogLayout.setMinimumWidth(dialogWidth.intValue());
        return dialogLayout;
    }

    private void launchDeleteConfirmationDialog() {
        DeleteConfirmationDialog deleteConfirmationDialog =  new DeleteConfirmationDialog();
        Bundle args = new Bundle();
        args.putSerializable(DeleteConfirmationDialog.MESSAGE_ID_TO_DELETE_BUNDLE_KEY,
                             mMessageWithRecipients.getMessage().getId());
        deleteConfirmationDialog.setArguments(args);
        deleteConfirmationDialog.show(getActivity().getSupportFragmentManager(),
                DeleteConfirmationDialog.class.getName());
    }

    private void setUpOptionsDialog(View dialogLayout) {
        ViewGroup dialogMessageDetailsLayout = dialogLayout.findViewById(R.id.message_details_layout);
        /*
         * The message details layout itself does not have a background (for reusability),
         * and we want to separate it from our dialog's background.
         *
         * To give the layout a background, we could either create a wrapper layout that holds the
         * `<include layout="@layout/listed_message_details" />` and does nothing except specify
         * the background, but this is ugly and creates unnecessary nesting.
         *
         * To avoid this nesting, we just set the background programmatically here.
         */
        dialogMessageDetailsLayout.setBackground(
                dialogMessageDetailsLayout.getContext().getDrawable(
                        R.drawable.message_background_shape));

        populateViews(dialogMessageDetailsLayout);
        setUpButtons(dialogLayout);
    }

    private void setUpButtons(View dialogLayout) {
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

    private void populateViews(View dialogMessageDetailsLayout) {

        Message message = mMessageWithRecipients.getMessage();
        List<Recipient> recipients = mMessageWithRecipients.getRecipients();

        updateMessageContentTv(dialogMessageDetailsLayout, message.getTextContent());

        if (recipients.size() > 1)
            updateRecipientsLabelTv(dialogMessageDetailsLayout, true);
        else
            updateRecipientsLabelTv(dialogMessageDetailsLayout, false);

        updateRecipientsTv(dialogMessageDetailsLayout, getConcatenatedRecipientNames(recipients));

        updateScheduledDayTv(dialogMessageDetailsLayout, getFormattedDayOnly(message));

        updateScheduledDateTv(dialogMessageDetailsLayout, getFormattedDateOnly(message));

        updateScheduledTimeTv(dialogMessageDetailsLayout, getFormattedTimeOnly(message));
    }
}
