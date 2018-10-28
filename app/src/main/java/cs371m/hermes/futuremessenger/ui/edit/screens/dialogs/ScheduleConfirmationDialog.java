package cs371m.hermes.futuremessenger.ui.edit.screens.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.List;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.pojo.MessageWithRecipients;
import cs371m.hermes.futuremessenger.tasks.SaveAndScheduleMessage;
import cs371m.hermes.futuremessenger.ui.edit.screens.activities.EditTextMessageActivity;
import cs371m.hermes.futuremessenger.ui.main.screens.activities.MainActivity;

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
import static cs371m.hermes.futuremessenger.support.SchedulingSupport.areDateAndTimeValid;

public class ScheduleConfirmationDialog extends DialogFragment {

    private MessageWithRecipients mMessageWithRecipients;

    public ScheduleConfirmationDialog() {
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
        View scheduleConfirmationDialogLayout = inflateAndReturnDialogLayout(inflater);
        setUpButtons(scheduleConfirmationDialogLayout);
        setUpMessageDetailsLayout(scheduleConfirmationDialogLayout);
        return scheduleConfirmationDialogLayout;
    }

    private void setUpMessageDetailsLayout(View dialogLayout) {
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

    private View inflateAndReturnDialogLayout(LayoutInflater inflater) {
        View scheduleConfirmationDialogLayout = inflater.inflate(R.layout.schedule_confirmation_dialog, null);

        // set the dialog's width to be 90% of the width of the screen
        Double dialogWidth = getContext().getResources().getDisplayMetrics().widthPixels * 0.90;
        scheduleConfirmationDialogLayout.setMinimumWidth(dialogWidth.intValue());
        return scheduleConfirmationDialogLayout;
    }

    private void setUpButtons(View scheduleConfirmationDialogLayout) {
        AppCompatButton confirmScheduleButton = scheduleConfirmationDialogLayout.findViewById(R.id.confirm_schedule_button);
        confirmScheduleButton.setOnClickListener(view -> {
            dismiss();
            if (areDateAndTimeValid(mMessageWithRecipients.getMessage().getScheduledDateTime(), (EditTextMessageActivity) getActivity())) {
                runScheduleTask();
            }
        });

        AppCompatButton cancelButton = scheduleConfirmationDialogLayout.findViewById(R.id.cancel_schedule_button);
        cancelButton.setOnClickListener(view -> {
            dismiss();
        });
    }

    private void runScheduleTask() {
        SaveAndScheduleMessage scheduleMessageTask = new SaveAndScheduleMessage();
        scheduleMessageTask.setArguments(getContext(), AppDatabase.getInstance(getContext()),
                mMessageWithRecipients);
        scheduleMessageTask.execute();
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


}
