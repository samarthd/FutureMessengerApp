package cs371m.hermes.futuremessenger.ui.main.support.adapters;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.persistence.pojo.MessageWithRecipients;
import cs371m.hermes.futuremessenger.ui.main.screens.dialogs.ScheduledMessageOptionsDialogFragment;
import cs371m.hermes.futuremessenger.ui.main.support.viewholders.MessageViewHolder;

public class ScheduledMessageAdapter extends MessageAdapter {

    // Need this in order to create dialog fragments
    private FragmentManager mSupportFragmentManager;

    public ScheduledMessageAdapter(FragmentManager supportFragmentManager) {
        this.mSupportFragmentManager = supportFragmentManager;
    }

    /**
     * Called by the LayoutManager to create new views.
     * Inflates the scheduled message layout.
     */
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View fullMessageLayout = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listed_message, parent, false);
        return new MessageViewHolder(fullMessageLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        updateWithoutPayloads(holder);
        setUpOnClick(holder);
    }

    private void setUpOnClick(MessageViewHolder holder) {
        holder.fullMessageLayout
                .setOnClickListener(scheduledMessageView -> {
                    ScheduledMessageOptionsDialogFragment optionsDialog =
                            new ScheduledMessageOptionsDialogFragment();
                    Bundle args = new Bundle();
                    args.putSerializable(MessageWithRecipients.BUNDLE_KEY_MESSAGE_WITH_RECIPIENTS,
                                         mMessagesWithRecipients.get(holder.getAdapterPosition()));
                    optionsDialog.setArguments(args);
                    optionsDialog.show(mSupportFragmentManager,
                                       ScheduledMessageOptionsDialogFragment.class.getName());
                });
    }
}
