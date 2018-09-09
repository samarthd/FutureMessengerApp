package cs371m.hermes.futuremessenger.support;

import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.ui.main.screens.dialogs.ScheduledMessageOptionsDialog;
import cs371m.hermes.futuremessenger.ui.main.support.adapters.MessageAdapter;

/**
 * A support class that holds basic functionality necessary to populate a message details view.
 *
 * @see MessageAdapter
 * @see ScheduledMessageOptionsDialog
 *
 */
public class MessageDetailsViewBindingSupport {

    public static final DateFormat DAY_FORMATTER = new SimpleDateFormat("EEEE");
    public static final DateFormat DATE_FORMATTER = DateFormat.getDateInstance(DateFormat.MEDIUM);
    public static final DateFormat TIME_FORMATTER = DateFormat.getTimeInstance(DateFormat.SHORT);

    public static final String PAYLOAD_KEY_MESSAGE_CONTENT = "message_content";
    public static final String PAYLOAD_KEY_RECIPIENTS = "recipients";
    public static final String PAYLOAD_KEY_RECIPIENTS_PLURAL_FLAG = "recipients_plural_flag";
    public static final String PAYLOAD_KEY_SCHEDULED_DAY = "scheduled_day";
    public static final String PAYLOAD_KEY_SCHEDULED_DATE = "scheduled_date";
    public static final String PAYLOAD_KEY_SCHEDULED_TIME = "scheduled_time";

    public static void updateMessageContentTv(View messageDetailsLayout, String messageContent) {
        TextView messageContentTv =
                messageDetailsLayout.findViewById(R.id.message_content_tv);
        messageContentTv.setText(messageContent);
    }

    public static void updateRecipientsTv(View messageDetailsLayout, String concatenatedRecipientNames) {
        TextView recipientsTv =
                messageDetailsLayout.findViewById(R.id.recipients_tv);
        recipientsTv.setText(concatenatedRecipientNames);
    }

    public static void updateRecipientsLabelTv(View messageDetailsLayout, boolean isPlural) {
        TextView recipientLabelTv =
                messageDetailsLayout.findViewById(R.id.recipients_label_tv);
        if (isPlural) {
            recipientLabelTv.setText(R.string.recipients_label_plural);
        }
        else {
            recipientLabelTv.setText(R.string.recipients_label_singular);
        }
    }

    public static void updateScheduledDayTv(View messageDetailsLayout, String dayString) {
        TextView scheduledDayTv =
                messageDetailsLayout.findViewById(R.id.scheduled_day_tv);
        scheduledDayTv.setText(dayString);
    }

    public static void updateScheduledDateTv(View messageDetailsLayout, String dateString) {
        TextView scheduledDateTv =
                messageDetailsLayout.findViewById(R.id.scheduled_date_tv);
        scheduledDateTv.setText(dateString);
    }

    public static void updateScheduledTimeTv(View messageDetailsLayout, String timeString) {
        TextView scheduledTimeTv =
                messageDetailsLayout.findViewById(R.id.scheduled_time_tv);
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

        result.append(recipients.get(0).getName());

        for(int i = 1; i < recipients.size(); i++) {
            result.append("\n");
            result.append(recipients.get(i).getName());
        }

        return result.toString();
    }
}
