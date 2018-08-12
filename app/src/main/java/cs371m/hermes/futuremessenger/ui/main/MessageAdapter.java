package cs371m.hermes.futuremessenger.ui.main;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * RecyclerView adapter for lists of messages.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private final List<MessageWithRecipients> mMessagesWithRecipients;
    private final DateFormat mDayFormatter = new SimpleDateFormat("EEEE");
    private final DateFormat mDateFormatter = DateFormat.getDateInstance(DateFormat.LONG);
    private final DateFormat mTimeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT);

    public MessageAdapter() {
        mMessagesWithRecipients = new ArrayList<>();
    }

    public void updateMessageList(List<MessageWithRecipients> messages) {
        mMessagesWithRecipients.clear();
        mMessagesWithRecipients.addAll(messages);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mMessagesWithRecipients.size();
    }

    /**
     * Called by the LayoutManager to create new views
     */
    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View listedMessageView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listed_message, parent, false);
        return new ViewHolder(listedMessageView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        MessageWithRecipients currentMessageWithRecipients = mMessagesWithRecipients.get(position);
        Message message = currentMessageWithRecipients.message;
        List<Recipient> recipients = currentMessageWithRecipients.recipients;

        TextView messageContentTv =
                holder.listedMessageView.findViewById(R.id.message_content_tv);
        messageContentTv.setText(message.getTextContent());

        if (recipients.size() > 1) {
            TextView recipientLabelTv =
                    holder.listedMessageView.findViewById(R.id.recipients_label_tv);
            recipientLabelTv.setText(R.string.recipients_label_plural);
        }
        TextView recipientsTv =
                holder.listedMessageView.findViewById(R.id.recipients_tv);
        recipientsTv.setText(getConcatenatedRecipientNames(recipients));

        TextView scheduledDayTv =
                holder.listedMessageView.findViewById(R.id.scheduled_day_tv);
        scheduledDayTv.setText(getFormattedDayOnly(message));

        TextView scheduledDateTv =
                holder.listedMessageView.findViewById(R.id.scheduled_date_tv);
        scheduledDateTv.setText(getFormattedDateOnly(message));

        TextView scheduledTimeTv =
                holder.listedMessageView.findViewById(R.id.scheduled_time_tv);
        scheduledTimeTv.setText(getFormattedTimeOnly(message));


    }

    private String getFormattedTimeOnly(Message message) {
        Calendar scheduledDateTime = message.getScheduledDateTime();
        return mTimeFormatter.format(scheduledDateTime.getTime()).toUpperCase();
    }

    private String getFormattedDateOnly(Message message) {
        Calendar scheduledDate = message.getScheduledDateTime();
        return mDateFormatter.format(scheduledDate.getTime()).toUpperCase();

    }

    private String getFormattedDayOnly(Message message) {
        Calendar scheduledDate = message.getScheduledDateTime();
        return mDayFormatter.format(scheduledDate.getTime()).toUpperCase();
    }

    private String getConcatenatedRecipientNames(List<Recipient> recipients) {
        if (recipients.size() == 0)
            return "";

        StringBuilder result = new StringBuilder();

        result.append(getRecipientNameOrPhone(recipients.get(0)));

        for(int i = 1; i < recipients.size(); i++) {
            result.append(", ");
            result.append(getRecipientNameOrPhone(recipients.get(i)));
        }

        return result.toString();
    }

    // Returns the recipient's name, or the phone number if there is no name.
    private String getRecipientNameOrPhone(Recipient recipient) {
        if (recipient.getName() == null || recipient.getName().equals(""))
            return recipient.getPhoneNumber();
        return recipient.getName();
    }


    /**
     * ViewHolder
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        private View listedMessageView;

        public ViewHolder(View itemView) {
            super(itemView);
            listedMessageView = itemView;
        }
    }
}
