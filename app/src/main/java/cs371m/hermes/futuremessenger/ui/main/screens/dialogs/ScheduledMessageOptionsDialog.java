package cs371m.hermes.futuremessenger.ui.main.screens.dialogs;

import android.app.Dialog;
import android.arch.persistence.room.InvalidationTracker;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Set;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.pojo.MessageWithRecipients;
import cs371m.hermes.futuremessenger.tasks.FinishActivityOrDismissDialogIfScheduledMessageInvalidated;
import cs371m.hermes.futuremessenger.ui.edit.screens.activities.EditTextMessageActivity;

public class ScheduledMessageOptionsDialog extends MessageOptionsDialog {


    public ScheduledMessageOptionsDialog() {
        // empty constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setUpInvalidationTracker();
        return super.onCreateDialog(savedInstanceState);
    }

    /*
      We want to make sure if this message gets sent/deleted while the dialog is open,
      we close the activity and prevent the user from interacting with a now-invalid message.
     */
    private void setUpInvalidationTracker() {
        AppDatabase db = AppDatabase.getInstance(getContext());
        String[] tablesToTrack = {"messages"};
        InvalidationTracker invalidationTracker = db.getInvalidationTracker();
        DialogFragment currentDialog = this;
        invalidationTracker.addObserver(new InvalidationTracker.Observer(tablesToTrack) {
            /**
             * If the message being examined is invalidated, dismiss the dialog.
             * @param tables the tables that were invalidated
             */
            @Override
            public void onInvalidated(@NonNull Set<String> tables) {
                Log.d(this.getClass().getName(), "Tables invalidated: " + tables.toString());
                FinishActivityOrDismissDialogIfScheduledMessageInvalidated checkIfMessageInvalidatedTask =
                        new FinishActivityOrDismissDialogIfScheduledMessageInvalidated();
                checkIfMessageInvalidatedTask.setArguments(db,
                        mMessageWithRecipients.getMessage().getId(),
                        currentDialog);
                checkIfMessageInvalidatedTask.execute();
            }
        });
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
