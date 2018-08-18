package cs371m.hermes.futuremessenger.ui.main.adapters.message;

import android.support.annotation.NonNull;
import android.support.transition.ChangeBounds;
import android.support.transition.Explode;
import android.support.transition.Fade;
import android.support.transition.Slide;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.ui.main.adapters.message.viewholders.MessageViewHolder;

public class ScheduledMessageAdapter extends MessageAdapter {

    private int mExpandedPosition = -1;

    /**
     * Called by the LayoutManager to create new views.
     * Inflates the scheduled message layout.
     */
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View fullMessageLayout = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listed_scheduled_message, parent, false);
        return new MessageViewHolder(fullMessageLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {

        // always use getAdapterPosition for guaranteed correctness
        position = holder.getAdapterPosition();

        super.onBindViewHolder(holder, position, payloads);

        // inflate the options and set onclick listeners
        setUpOptionsLayout(holder, position);

    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        // always use getAdapterPosition for guaranteed correctness
        position = holder.getAdapterPosition();

        super.onBindViewHolder(holder, position);

        // inflate the options and set onclick listeners
        setUpOptionsLayout(holder, position);
    }

    private void setUpOptionsLayout(MessageViewHolder holder, int position) {

        ViewGroup optionsLayout = holder.fullMessageLayout.findViewById(R.id.options_layout);

        boolean isExpanded = position == mExpandedPosition;
        optionsLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.fullMessageLayout.setActivated(isExpanded);


        holder.fullMessageLayout.findViewById(R.id.general_message_layout)
                .setOnClickListener(view -> {
                    // Collapse the currently expanded item
                    if (mExpandedPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(mExpandedPosition);
                    }
                    mExpandedPosition = isExpanded ? -1 : position;
                    TransitionManager.beginDelayedTransition((ViewGroup) holder.fullMessageLayout, new ChangeBounds());
                    notifyItemChanged(position);
                });
    }
}
