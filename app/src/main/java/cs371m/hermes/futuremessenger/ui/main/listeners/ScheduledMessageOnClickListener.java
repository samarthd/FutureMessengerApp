package cs371m.hermes.futuremessenger.ui.main.listeners;

import android.util.Log;
import android.view.View;

import cs371m.hermes.futuremessenger.ui.main.adapters.message.viewholders.MessageViewHolder;
import cs371m.hermes.futuremessenger.ui.main.screens.dialogs.ScheduledMessageOptionsDialog;

public class ScheduledMessageOnClickListener implements View.OnClickListener {

    private MessageViewHolder mMessageViewHolder;

    public ScheduledMessageOnClickListener(MessageViewHolder holder) {
        mMessageViewHolder = holder;
    }
    @Override
    public void onClick(View scheduledMessageView) {
        Log.d("Click", "ayE");
        ScheduledMessageOptionsDialog optionsDialog =
                new ScheduledMessageOptionsDialog(mMessageViewHolder);
        optionsDialog.show();
    }
}
