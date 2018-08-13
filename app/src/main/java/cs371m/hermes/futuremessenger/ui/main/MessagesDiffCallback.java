package cs371m.hermes.futuremessenger.ui.main;

import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.util.Log;

import java.util.List;

import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.pojo.MessageWithRecipients;

/**
 * DiffUtil callback implementation that compares two lists of messages with recipients
 * and determines where changes were made.
 */
public class MessagesDiffCallback extends DiffUtil.Callback {

    private List<MessageWithRecipients> existingList;
    private List<MessageWithRecipients> updatedList;

    public MessagesDiffCallback(List<MessageWithRecipients> existing,
                                List<MessageWithRecipients> updated) {
        Log.d("In MessagesDiffCallback",  "In constructor");
        this.existingList = existing;
        this.updatedList = updated;
    }

    @Override
    public int getOldListSize() {
        return existingList.size();
    }

    @Override
    public int getNewListSize() {
        return updatedList.size();
    }

    /*
        This method is used to determine whether two items represent the same object, and
        thus we must only compare their IDs.
     */
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        MessageWithRecipients oldItem = existingList.get(oldItemPosition);
        MessageWithRecipients newItem = updatedList.get(newItemPosition);
        Log.d("In MessagesDiffCallback", "Are items the same, determined: " +
                oldItem.message.getId().equals(newItem.message.getId()));
        return oldItem.message.getId().equals(newItem.message.getId());
    }

    /*
        Return whether all non-ID fields are the same. This method is only concerned with
        visual changes to the element, so we compare all but the ID fields.
    */
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        MessageWithRecipients oldItem = existingList.get(oldItemPosition);
        MessageWithRecipients newItem = updatedList.get(newItemPosition);
        Log.d("In MessagesDiffCallback", "Are contents the same, determined: " +
                oldItem.equals(newItem));
        return oldItem.equals(newItem);
    }

    /**
     * This method returns what is actually changed about the object in the event that
     * an object is determined to have been updated.
     *
     * Put all the updated values in a payload so the adapter's onBindViewHolder (the version
     * that accepts a payload argument) will be able to properly updated only the desired fields
     * from the payload.
     */
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        MessageWithRecipients newMessageWithRecipients = updatedList.get(newItemPosition);
        MessageWithRecipients oldMessageWithRecipients = existingList.get(oldItemPosition);
        Bundle diffBundle = new Bundle();

        Message newMessage = newMessageWithRecipients.message;
        Message oldMessage = oldMessageWithRecipients.message;
        // Compare the message fields themselves
        if (!newMessage.getTextContent().equals(oldMessage.getTextContent())) {
            diffBundle.putString(MessageAdapter.PAYLOAD_KEY_MESSAGE_CONTENT,
                                 newMessage.getTextContent());
        }
        if (!newMessage.getScheduledDateTime().equals(oldMessage.getScheduledDateTime())) {
            diffBundle.putString(MessageAdapter.PAYLOAD_KEY_SCHEDULED_DATE,
                                 MessageAdapter.getFormattedDateOnly(newMessage));
            diffBundle.putString(MessageAdapter.PAYLOAD_KEY_SCHEDULED_DAY,
                                 MessageAdapter.getFormattedDayOnly(newMessage));
            diffBundle.putString(MessageAdapter.PAYLOAD_KEY_SCHEDULED_TIME,
                                 MessageAdapter.getFormattedTimeOnly(newMessage));
        }

        // Compare the recipient strings
        List<Recipient> newRecipients = newMessageWithRecipients.recipients;
        List<Recipient> oldRecipients = oldMessageWithRecipients.recipients;

        if (!newRecipients.equals(oldRecipients)) {
            diffBundle.putString(MessageAdapter.PAYLOAD_KEY_RECIPIENTS,
                                 MessageAdapter.getConcatenatedRecipientNames(newRecipients));
            diffBundle.putBoolean(MessageAdapter.PAYLOAD_KEY_RECIPIENTS_PLURAL_FLAG,
                                  newRecipients.size() > 1);
        }

        if (diffBundle.size() == 0) return null;
        return diffBundle;
    }


}