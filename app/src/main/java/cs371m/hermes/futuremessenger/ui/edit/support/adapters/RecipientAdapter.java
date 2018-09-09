package cs371m.hermes.futuremessenger.ui.edit.support.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.ui.edit.support.viewholders.RecipientViewHolder;

public class RecipientAdapter extends RecyclerView.Adapter<RecipientViewHolder>{

    private final List<Recipient> mRecipientList = new ArrayList<>();

    private final RecipientRemoveListener mRecipientRemoveListener;

    public RecipientAdapter(RecipientRemoveListener recipientRemoveListener)  {
        this.mRecipientRemoveListener = recipientRemoveListener;
    }

    public void updateRecipientList(List<Recipient> updatedRecipientList) {
        this.mRecipientList.clear();
        this.mRecipientList.addAll(updatedRecipientList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View listedRecipientLayout = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listed_recipient_layout, parent, false);
        return new RecipientViewHolder(listedRecipientLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipientViewHolder holder, int position) {
        position = holder.getAdapterPosition();

        Recipient currentRecipient = mRecipientList.get(position);

        TextView recipientNameTv = holder.listedRecipientLayout.findViewById(R.id.listed_recipient_name);
        recipientNameTv.setText(currentRecipient.getName().toUpperCase());

        TextView recipientPhoneTv = holder.listedRecipientLayout.findViewById(R.id.listed_recipient_phone_number);
        recipientPhoneTv.setText(currentRecipient.getPhoneNumber());

        ImageButton removeButton = holder.listedRecipientLayout.findViewById(R.id.remove_recipient_button);
        removeButton.setOnClickListener(view -> {
            Recipient recipientToRemove = mRecipientList.get(holder.getAdapterPosition());
            this.mRecipientRemoveListener.removeRecipient(recipientToRemove);
        });
    }

    @Override
    public int getItemCount() {
        return mRecipientList.size();
    }

    public interface RecipientRemoveListener {
        void removeRecipient(Recipient recipient);
    }
}
