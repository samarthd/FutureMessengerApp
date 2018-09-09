package cs371m.hermes.futuremessenger.ui.main.support.adapters;

import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.pojo.MessageWithRecipients;
import cs371m.hermes.futuremessenger.ui.main.support.diff.MessagesDiffCallback;
import cs371m.hermes.futuremessenger.ui.main.support.viewholders.MessageViewHolder;

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

/**
 * RecyclerView adapter for lists of messages.
 */
public abstract class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    protected final List<MessageWithRecipients> mMessagesWithRecipients = new ArrayList<>();

    public void updateMessageList(List<MessageWithRecipients> updatedMessageList) {
        DiffUtil.DiffResult diffResult =
                DiffUtil.calculateDiff(
                        new MessagesDiffCallback(mMessagesWithRecipients, updatedMessageList));
        this.mMessagesWithRecipients.clear();
        this.mMessagesWithRecipients.addAll(updatedMessageList);
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public long getItemId(int position) {
        return mMessagesWithRecipients.get(position).getMessage().getId();
    }

    @Override
    public int getItemCount() {
        return mMessagesWithRecipients.size();
    }


    protected void updateWithoutPayloads(MessageViewHolder holder) {
        // always use getAdapterPosition for guaranteed correctness
        int position = holder.getAdapterPosition();

        MessageWithRecipients currentMessageWithRecipients = mMessagesWithRecipients.get(position);
        Message message = currentMessageWithRecipients.getMessage();
        List<Recipient> recipients = currentMessageWithRecipients.getRecipients();

        View listedMessageDetailsLayout = holder.fullMessageLayout;
        updateMessageContentTv(listedMessageDetailsLayout, message.getTextContent());

        if (recipients.size() > 1)
            updateRecipientsLabelTv(listedMessageDetailsLayout, true);
        else
            updateRecipientsLabelTv(listedMessageDetailsLayout, false);

        updateRecipientsTv(listedMessageDetailsLayout, getConcatenatedRecipientNames(recipients));

        updateScheduledDayTv(listedMessageDetailsLayout, getFormattedDayOnly(message));

        updateScheduledDateTv(listedMessageDetailsLayout, getFormattedDateOnly(message));

        updateScheduledTimeTv(listedMessageDetailsLayout, getFormattedTimeOnly(message));
    }

}
