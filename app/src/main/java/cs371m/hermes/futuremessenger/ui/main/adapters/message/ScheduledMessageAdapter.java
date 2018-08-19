package cs371m.hermes.futuremessenger.ui.main.adapters.message;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.ui.main.adapters.message.viewholders.MessageViewHolder;
import cs371m.hermes.futuremessenger.ui.main.listeners.ScheduledMessageOnClickListener;

public class ScheduledMessageAdapter extends MessageAdapter {

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
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {

        if (!payloads.isEmpty()) {
            updateWithPayloads(holder, payloads);
            setUpOnClick(holder);
        }
        else {
            onBindViewHolder(holder, position);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        updateWithoutPayloads(holder);
        setUpOnClick(holder);
    }

    private void setUpOnClick(MessageViewHolder holder) {
        holder.fullMessageLayout
                .setOnClickListener(new ScheduledMessageOnClickListener(holder));
    }
}
