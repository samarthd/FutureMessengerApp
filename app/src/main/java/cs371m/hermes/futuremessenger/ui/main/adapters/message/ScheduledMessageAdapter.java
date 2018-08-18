package cs371m.hermes.futuremessenger.ui.main.adapters.message;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.ui.main.adapters.message.viewholders.MessageDetailsViewHolder;

public class ScheduledMessageAdapter extends MessageAdapter {

    private int mExpandedPosition = -1;

    /**
     * Called by the LayoutManager to create new views.
     * Inflates the scheduled message layout.
     */
    @NonNull
    @Override
    public MessageDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View listedMessageView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listed_scheduled_message, parent, false);
        return new MessageDetailsViewHolder(listedMessageView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageDetailsViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);

        // inflate the options and set onclick listeners
        setUpOptionsLayout(holder, position);

    }

    @Override
    public void onBindViewHolder(@NonNull MessageDetailsViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        // inflate the options and set onclick listeners
        setUpOptionsLayout(holder, position);
    }

    private void setUpOptionsLayout(MessageDetailsViewHolder holder, int position) {

        LinearLayout optionsLayout = holder.listedMessageView.findViewById(R.id.options_layout);

        boolean isExpanded = position == mExpandedPosition;
        optionsLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        optionsLayout.setActivated(isExpanded);


        holder.listedMessageView.findViewById(R.id.general_message_layout)
                .setOnClickListener(view -> {
                    mExpandedPosition = isExpanded ? -1 : position;
                    notifyItemChanged(position);
                });
    }
}
