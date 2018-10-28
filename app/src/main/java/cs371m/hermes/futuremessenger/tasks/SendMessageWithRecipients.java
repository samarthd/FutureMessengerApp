package cs371m.hermes.futuremessenger.tasks;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.pojo.MessageWithRecipients;
import cs371m.hermes.futuremessenger.persistence.repositories.MessageDao;
import cs371m.hermes.futuremessenger.persistence.repositories.MessageRecipientJoinDao;
import cs371m.hermes.futuremessenger.support.EntityMissingException;

import static android.Manifest.permission.SEND_SMS;
import static cs371m.hermes.futuremessenger.persistence.entities.embedded.Status.FAILED;
import static cs371m.hermes.futuremessenger.support.SchedulingSupport.getContentTextForMessageFailedDueToPermissions;
import static cs371m.hermes.futuremessenger.support.SchedulingSupport.getSentIntentForMessagePart;
import static cs371m.hermes.futuremessenger.support.SchedulingSupport.getUniquePendingIntentIdForMessagePart;
import static cs371m.hermes.futuremessenger.support.SchedulingSupport.showOrUpdateSentNotificationForMessage;

public class SendMessageWithRecipients extends AsyncTask<Void, Void, Void> {

    private static final String TAG = SendMessageWithRecipients.class.getName();

    private AppDatabase mDb;
    private MessageDao mMessageDao;
    private MessageRecipientJoinDao mJoinDao;

    private Context mContext;

    private BroadcastReceiver.PendingResult mPendingResult;
    private Long mMessageID;

    public void setArguments(Context context, AppDatabase db, Long messageID, BroadcastReceiver.PendingResult pendingResult) {
        mContext = context.getApplicationContext();
        mDb = db;
        mMessageID = messageID;
        mPendingResult = pendingResult;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (mDb == null || mMessageID == null) {
            return null;
        }

        this.mMessageDao = mDb.messageDao();
        this.mJoinDao = mDb.messageRecipientJoinDao();

        try {
            mDb.runInTransaction(() -> {
                MessageWithRecipients messageWithRecipients = findMessageWithRecipients();
                if (ContextCompat.checkSelfPermission(mContext, SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    setMessageFailedDueToPermissionsAndNotify(messageWithRecipients.getMessage());
                } else {
                    sendMessageToAllRecipients(messageWithRecipients);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        mPendingResult.finish();
        return null;
    }

    private void setMessageFailedDueToPermissionsAndNotify(Message message) {
        message.getStatus().setCode(FAILED);
        mDb.messageDao().updateMessage(message);
        CharSequence notificationContentText = getContentTextForMessageFailedDueToPermissions(mContext, message);
        showOrUpdateSentNotificationForMessage(mContext, message, notificationContentText);
    }

    private MessageWithRecipients findMessageWithRecipients() {
        Message message = mMessageDao.findMessage(mMessageID);
        if (message == null) {
            Log.e(TAG, "Couldn't find any message with that ID.");
            throw new EntityMissingException("Couldn't find any message with that ID");
        }

        List<Recipient> recipientList = mJoinDao.findRecipientsForMessage(mMessageID);
        return new MessageWithRecipients(message, recipientList);
    }

    private void sendMessageToAllRecipients(MessageWithRecipients messageWithRecipients) {
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> messageParts =
                smsManager.divideMessage(messageWithRecipients.getMessage().getTextContent());

        for (Recipient recipient : messageWithRecipients.getRecipients()) {
            sendMessageToRecipient(smsManager, messageParts,
                    messageWithRecipients.getMessage().getId(), recipient);
        }
    }

    private void sendMessageToRecipient(SmsManager smsManager,
                                        ArrayList<String> messageParts,
                                        Long messageID,
                                        Recipient recipient) {
        ArrayList<PendingIntent> sentIntents = new ArrayList<>();

        int index = 0;
        for (String messagePart : messageParts) {
            Intent sentIntent =
                    getSentIntentForMessagePart(mContext, messageID, messagePart, recipient, index);
            sentIntents.add(PendingIntent.getBroadcast(mContext,
                    getUniquePendingIntentIdForMessagePart(messageID, messagePart, index),
                    sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT));
            index++;
        }
        smsManager.sendMultipartTextMessage(recipient.getPhoneNumber(),
                null, messageParts, sentIntents, null);

    }
}
