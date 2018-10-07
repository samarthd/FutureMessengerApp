package cs371m.hermes.futuremessenger.ui.main.support.adapters;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.persistence.pojo.MessageWithRecipients;
import cs371m.hermes.futuremessenger.ui.main.screens.dialogs.FailedMessageOptionsDialog;
import cs371m.hermes.futuremessenger.ui.main.support.viewholders.MessageViewHolder;

public class FailedMessageAdapter extends MessageAdapter {
    // Need this in order to create dialog fragments
    private FragmentManager mSupportFragmentManager;

    public FailedMessageAdapter(FragmentManager supportFragmentManager) {
        this.mSupportFragmentManager = supportFragmentManager;
    }


    /**
     * Called by the LayoutManager to create new views.
     * Inflates the sent message layout.
     */
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View fullMessageLayout = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listed_message, parent, false);
        return new MessageViewHolder(fullMessageLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int i) {
        updateWithoutPayloads(holder);
        setUpOnClick(holder);
    }

    private void setUpOnClick(MessageViewHolder holder) {
        holder.fullMessageLayout
                .setOnClickListener(scheduledMessageView -> {
                    FailedMessageOptionsDialog optionsDialog =
                            new FailedMessageOptionsDialog();
                    Bundle args = new Bundle();
                    args.putSerializable(MessageWithRecipients.BUNDLE_KEY_MESSAGE_WITH_RECIPIENTS,
                            mMessagesWithRecipients.get(holder.getAdapterPosition()));
                    optionsDialog.setArguments(args);
                    optionsDialog.show(mSupportFragmentManager,
                            FailedMessageOptionsDialog.class.getName());
                });
    }
}
