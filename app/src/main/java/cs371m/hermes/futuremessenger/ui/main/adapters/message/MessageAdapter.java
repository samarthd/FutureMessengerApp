package cs371m.hermes.futuremessenger.ui.main.adapters.message;

import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.pojo.MessageWithRecipients;
import cs371m.hermes.futuremessenger.ui.main.MessagesDiffCallback;
import cs371m.hermes.futuremessenger.ui.main.adapters.message.viewholders.MessageViewHolder;

/**
 * RecyclerView adapter for lists of messages.
 */
public abstract class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    protected final List<MessageWithRecipients> mMessagesWithRecipients = new ArrayList<>();

    private static final DateFormat DAY_FORMATTER = new SimpleDateFormat("EEEE");
    private static final DateFormat DATE_FORMATTER = DateFormat.getDateInstance(DateFormat.LONG);
    private static final DateFormat TIME_FORMATTER = DateFormat.getTimeInstance(DateFormat.SHORT);

    public static final String PAYLOAD_KEY_MESSAGE_CONTENT = "message_content";
    public static final String PAYLOAD_KEY_RECIPIENTS = "recipients";
    public static final String PAYLOAD_KEY_RECIPIENTS_PLURAL_FLAG = "recipients_plural_flag";
    public static final String PAYLOAD_KEY_SCHEDULED_DAY = "scheduled_day";
    public static final String PAYLOAD_KEY_SCHEDULED_DATE = "scheduled_date";
    public static final String PAYLOAD_KEY_SCHEDULED_TIME = "scheduled_time";


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


    protected void updateWithPayloads(MessageViewHolder holder, List<Object> payloads) {
        Bundle bundle = (Bundle) payloads.get(0);
        for (String key : bundle.keySet()) {
            switch (key) {
                case PAYLOAD_KEY_MESSAGE_CONTENT:
                    updateMessageContentTv(holder, bundle.getString(key));
                case PAYLOAD_KEY_RECIPIENTS:
                    updateRecipientsTv(holder, bundle.getString(key));
                case PAYLOAD_KEY_RECIPIENTS_PLURAL_FLAG:
                    updateRecipientsLabelTv(holder, bundle.getBoolean(key));
                case PAYLOAD_KEY_SCHEDULED_DATE:
                    updateScheduledDateTv(holder, bundle.getString(key));
                case PAYLOAD_KEY_SCHEDULED_DAY:
                    updateScheduledDayTv(holder, bundle.getString(key));
                case PAYLOAD_KEY_SCHEDULED_TIME:
                    updateScheduledTimeTv(holder, bundle.getString(key));
            }
        }
    }

    protected void updateWithoutPayloads(MessageViewHolder holder) {
        // always use getAdapterPosition for guaranteed correctness
        int position = holder.getAdapterPosition();

        MessageWithRecipients currentMessageWithRecipients = mMessagesWithRecipients.get(position);
        Message message = currentMessageWithRecipients.getMessage();
        List<Recipient> recipients = currentMessageWithRecipients.getRecipients();

        updateMessageContentTv(holder, message.getTextContent());

        if (recipients.size() > 1)
            updateRecipientsLabelTv(holder, true);
        else
            updateRecipientsLabelTv(holder, false);

        updateRecipientsTv(holder, getConcatenatedRecipientNames(recipients));

        updateScheduledDayTv(holder, getFormattedDayOnly(message));

        updateScheduledDateTv(holder, getFormattedDateOnly(message));

        updateScheduledTimeTv(holder, getFormattedTimeOnly(message));
    }

    private void updateMessageContentTv(MessageViewHolder holder, String messageContent) {
        TextView messageContentTv =
                holder.fullMessageLayout.findViewById(R.id.message_content_tv);
        messageContentTv.setText(messageContent);
    }

    private void updateRecipientsTv(MessageViewHolder holder, String concatenatedRecipientNames) {
        TextView recipientsTv =
                holder.fullMessageLayout.findViewById(R.id.recipients_tv);
        recipientsTv.setText(concatenatedRecipientNames);
    }

    private void updateRecipientsLabelTv(MessageViewHolder holder, boolean isPlural) {
        TextView recipientLabelTv =
                holder.fullMessageLayout.findViewById(R.id.recipients_label_tv);
        if (isPlural) {
            recipientLabelTv.setText(R.string.recipients_label_plural);
        }
        else {
            recipientLabelTv.setText(R.string.recipients_label_singular);
        }
    }

    private void updateScheduledDayTv(MessageViewHolder holder, String dayString) {
        TextView scheduledDayTv =
                holder.fullMessageLayout.findViewById(R.id.scheduled_day_tv);
        scheduledDayTv.setText(dayString);
    }

    private void updateScheduledDateTv(MessageViewHolder holder, String dateString) {
        TextView scheduledDateTv =
                holder.fullMessageLayout.findViewById(R.id.scheduled_date_tv);
        scheduledDateTv.setText(dateString);
    }

    private void updateScheduledTimeTv(MessageViewHolder holder, String timeString) {
        TextView scheduledTimeTv =
                holder.fullMessageLayout.findViewById(R.id.scheduled_time_tv);
        scheduledTimeTv.setText(timeString);
    }



    public static String getFormattedTimeOnly(Message message) {
        Calendar scheduledDateTime = message.getScheduledDateTime();
        return TIME_FORMATTER.format(scheduledDateTime.getTime()).toUpperCase();
    }

    public static String getFormattedDateOnly(Message message) {
        Calendar scheduledDate = message.getScheduledDateTime();
        return DATE_FORMATTER.format(scheduledDate.getTime()).toUpperCase();

    }

    public static String getFormattedDayOnly(Message message) {
        Calendar scheduledDate = message.getScheduledDateTime();
        return DAY_FORMATTER.format(scheduledDate.getTime()).toUpperCase();
    }

    public static String getConcatenatedRecipientNames(List<Recipient> recipients) {
        if (recipients.size() == 0)
            return "";

        StringBuilder result = new StringBuilder();

        result.append(getRecipientNameOrPhone(recipients.get(0)));

        for(int i = 1; i < recipients.size(); i++) {
            result.append("\n");
            result.append(getRecipientNameOrPhone(recipients.get(i)));
        }

        return result.toString();
    }

    /**
     * Returns the recipient's name, or just the phone number
     * if there is no name.
     */
    private static String getRecipientNameOrPhone(Recipient recipient) {
        if (recipient.getName() == null || recipient.getName().equals(""))
            return recipient.getPhoneNumber();
        return "\u2022 " + recipient.getName();
    }
}
