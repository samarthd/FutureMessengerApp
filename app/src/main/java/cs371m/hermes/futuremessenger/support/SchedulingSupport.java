package cs371m.hermes.futuremessenger.support;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.text.HtmlCompat;
import android.telephony.SmsManager;
import android.text.Html;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.entities.embedded.Status;
import cs371m.hermes.futuremessenger.persistence.pojo.StatusDetails;
import cs371m.hermes.futuremessenger.support.sending.ScheduledMessageBroadcastReceiver;
import cs371m.hermes.futuremessenger.support.sending.SentBroadcastReceiver;
import cs371m.hermes.futuremessenger.ui.edit.screens.activities.EditTextMessageActivity;
import cs371m.hermes.futuremessenger.ui.main.screens.activities.MainActivity;

import static android.text.Html.FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE;
import static android.text.Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST;

/**
 * Class that has support methods necessary for scheduling messages.
 */
public class SchedulingSupport {

    private static final int WAKEUP_ALARM_TYPE = AlarmManager.RTC_WAKEUP;

    public static final String BUNDLE_KEY_MESSAGE_ID = "message_id";
    public static final String BUNDLE_KEY_RECIPIENT = "recipient";
    public static final String BUNDLE_KEY_MESSAGE_PART_INDEX = "message_part_index";
    public static final String BUNDLE_KEY_MESSAGE_PART = "message_part";

    /**
     * Creates the intent that will start the target message sending service with the message ID
     * as an argument.
     */
    public static Intent createMessageSendingIntent(Context context,
                                                    Message message) {
        context = context.getApplicationContext();

        Intent messageSendingIntent = new Intent(context, ScheduledMessageBroadcastReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putLong(BUNDLE_KEY_MESSAGE_ID, message.getId());
        messageSendingIntent.putExtras(bundle);

        return messageSendingIntent;
    }

    public static void scheduleMessageNonRepeating(Context context,
                                                   Message message) {
        context = context.getApplicationContext();

        long timeToSend = message.getScheduledDateTime().getTimeInMillis();
        Intent messageSendingIntent = createMessageSendingIntent(context, message);


        int pendingIntentUniqueId =
                getUniqueHashIdForMessage(message.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                pendingIntentUniqueId,
                messageSendingIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(WAKEUP_ALARM_TYPE, timeToSend, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(WAKEUP_ALARM_TYPE, timeToSend, pendingIntent);
        } else {
            alarmManager.set(WAKEUP_ALARM_TYPE, timeToSend, pendingIntent);
        }

    }

    /* The message ID is a long, so to safely convert to a unique int, hash it. */
    public static int getUniqueHashIdForMessage(Long messageID) {
        return Objects.hash(messageID);
    }

    public static int getUniquePendingIntentIdForMessagePart(Long messageID,
                                                             String messagePart,
                                                             int index) {
        return Objects.hash(messageID, messagePart, index);
    }

    public static String getSentIntentActionForMessagePart(Long messageID,
                                                           Long recipientID,
                                                           int index) {
        return "SENT message " + messageID + " recipient " + recipientID + " index " + index;
    }

    public static Intent getSentIntentForMessagePart(Context context, Long messageID,
                                                     String messagePart, Recipient recipient,
                                                     int index) {
        String sentIntentAction = getSentIntentActionForMessagePart(messageID, recipient.getId(), index);
        Intent intent = new Intent(context.getApplicationContext(), SentBroadcastReceiver.class);
        intent.setAction(sentIntentAction);

        Bundle extras = new Bundle();
        extras.putLong(BUNDLE_KEY_MESSAGE_ID, messageID);
        /*
            Need to do this stupid nested bundle for this reason:
            https://stackoverflow.com/questions/39209579/how-to-pass-custom-serializable-object-to-broadcastreceiver-via-pendingintent
            https://commonsware.com/blog/2016/07/22/be-careful-where-you-use-custom-parcelables.html
         */
        Bundle nestedBundleForRecipient = new Bundle();
        nestedBundleForRecipient.putSerializable(BUNDLE_KEY_RECIPIENT, recipient);
        extras.putBundle(BUNDLE_KEY_RECIPIENT, nestedBundleForRecipient);
        extras.putInt(BUNDLE_KEY_MESSAGE_PART_INDEX, index);
        extras.putString(BUNDLE_KEY_MESSAGE_PART, messagePart);
        intent.putExtras(extras);

        return intent;
    }

    public static void showOrUpdateSentNotificationForMessage(Context context, Message message, CharSequence notificationContentText) {
        context = context.getApplicationContext();

        CharSequence notificationTitle;
        if (StringUtils.equals(message.getStatus().getCode(), Status.SENT)) {
            notificationTitle = context.getString(R.string.send_success);
        } else {
            notificationTitle = context.getString(R.string.send_fail);
        }

        notificationTitle = HtmlCompat.fromHtml(notificationTitle.toString(),
                Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM |
                        FROM_HTML_SEPARATOR_LINE_BREAK_LIST |
                        FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE);


        // TODO add arguments to select the appropriate tab after launch
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, MainActivity.NOTIFICATION_CHANNEL_ID)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_EVENT)
                        .setSmallIcon(R.drawable.ic_text)
                        .setColor(ContextCompat.getColor(context, R.color.colorTertiary))
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationContentText)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(notificationContentText))
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(getUniqueHashIdForMessage(message.getId()), builder.build());
    }

    // TODO examine https://developer.android.com/guide/topics/resources/string-resource#java
    public static CharSequence getContentTextForMessageFromSentResults(Context context, Message message) {
        String messageHeadline = getNotificationMessageHeadlineWithMessageTextContent(context, message);
        StatusDetails statusDetails = Objects.requireNonNull(message.getStatus().getDetails());
        int totalMessageParts = statusDetails.getTotalMessagePartCountForEachRecipient();
        StringBuilder result = new StringBuilder(messageHeadline);
        for (Recipient recipient : statusDetails.getDetailsMap().keySet()) {
            result.append(
                    getContextTextForRecipient(context,
                            recipient,
                            statusDetails.getDetailsMap().get(recipient),
                            totalMessageParts));
        }
        return convertFromHtml(result);
    }

    public static CharSequence getContentTextForMessageScheduledWhileOff(Context context, Message message) {
        String messageHeadline = getNotificationMessageHeadlineWithMessageTextContent(context, message);
        return convertFromHtml(messageHeadline + context.getString(R.string.device_off));
    }

    public static CharSequence getContentTextForMessageFailedDueToPermissions(Context context, Message message) {
        String messageHeadline = getNotificationMessageHeadlineWithMessageTextContent(context, message);
        return convertFromHtml(messageHeadline + context.getString(R.string.permission_not_available));
    }

    private static String getNotificationMessageHeadlineWithMessageTextContent(Context context, Message message) {
        int maxLength = (message.getTextContent().length() < 30) ? message.getTextContent().length() : 30;
        String shortenedString = message.getTextContent().substring(0, maxLength);
        if (shortenedString.length() < message.getTextContent().length()) {
            shortenedString += "...";
        }
        return context.getString(R.string.message_headline, shortenedString);
    }

    private static CharSequence convertFromHtml(CharSequence htmlText) {
        return HtmlCompat.fromHtml(htmlText.toString(),
                Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM |
                        FROM_HTML_SEPARATOR_LINE_BREAK_LIST |
                        FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE);
    }

    private static CharSequence getContextTextForRecipient(Context context,
                                                           Recipient recipient,
                                                           SortedMap<Integer, Integer> resultMapForRecipient,
                                                           int totalMessagePartCount) {
        context = context.getApplicationContext();

        List<CharSequence> messagePartErrorResultStrings =
                getResultStringsForErroredParts(context, resultMapForRecipient, totalMessagePartCount);

        String fullRecipientResultText;
        if (messagePartErrorResultStrings.isEmpty()) {
            fullRecipientResultText = context.getString(R.string.message_full_success);
        } else if (messagePartErrorResultStrings.size() == totalMessagePartCount) {
            // all messages failed
            fullRecipientResultText =
                    StringUtils.join(messagePartErrorResultStrings.toArray(), ", ");
        } else {
            // not all messages experienced errors
            fullRecipientResultText =
                    StringUtils.join(messagePartErrorResultStrings.toArray(), ", ") +
                            ", " + context.getString(R.string.remaining_parts_success);
        }
        return context.getString(R.string.recipient_result_notification,
                recipient.getName().toUpperCase(),
                fullRecipientResultText);
    }

    private static List<CharSequence> getResultStringsForErroredParts(Context context, SortedMap<Integer,
            Integer> resultMapForRecipient, int totalMessagePartCount) {
        List<CharSequence> messagePartErrorResultStrings = new ArrayList<>();
        for (Integer messagePartIndex : resultMapForRecipient.keySet()) {
            int resultCodeForMessagePart = resultMapForRecipient.get(messagePartIndex);
            if (resultCodeForMessagePart != Activity.RESULT_OK) {
                int stringCode =
                        getAppropriateStringResourceForErrorResultCode(
                                resultMapForRecipient.get(messagePartIndex));
                messagePartErrorResultStrings.add(
                        context.getString(stringCode,
                                messagePartIndex + 1,
                                totalMessagePartCount));
            }
        }
        return messagePartErrorResultStrings;
    }

    private static int getAppropriateStringResourceForErrorResultCode(int resultCode) {
        int stringCode;
        switch (resultCode) {
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                stringCode = R.string.generic_error;
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                stringCode = R.string.radio_off;
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                stringCode = R.string.no_pdu;
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                stringCode = R.string.no_service;
                break;
            default:
                stringCode = R.string.generic_error;
                break;
        }
        return stringCode;
    }

    public static boolean areDateAndTimeValid(Calendar scheduledDateTime, EditTextMessageActivity editTextMessageActivity) {
        scheduledDateTime.set(Calendar.SECOND, 0); // set the seconds to 0 to avoid delays
        scheduledDateTime.set(Calendar.MILLISECOND, 0);

        Calendar minimumDateTime = Calendar.getInstance();
        // messages must be scheduled for at least 2 minutes more than the current minute
        minimumDateTime.add(Calendar.MINUTE, 2);
        // reset the seconds to 0 to avoid confusing the user when they set it 2 minutes ahead but the seconds cause it to fail the check
        minimumDateTime.set(Calendar.SECOND, 0);
        minimumDateTime.set(Calendar.MILLISECOND, 0);
        if (scheduledDateTime.before(minimumDateTime)) {
            Snackbar.make(editTextMessageActivity.findViewById(R.id.schedule_button),
                    R.string.error_datetime_not_future, Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


}
