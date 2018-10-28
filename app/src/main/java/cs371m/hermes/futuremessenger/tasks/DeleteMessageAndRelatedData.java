package cs371m.hermes.futuremessenger.tasks;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.repositories.MessageDao;
import cs371m.hermes.futuremessenger.persistence.repositories.MessageRecipientJoinDao;
import cs371m.hermes.futuremessenger.persistence.repositories.RecipientDao;

import static cs371m.hermes.futuremessenger.support.SchedulingSupport.createMessageSendingIntent;
import static cs371m.hermes.futuremessenger.support.SchedulingSupport.getUniqueHashIdForMessage;

/**
 * Deletes a message from the database, along with all join entries. If the associated recipients
 * do not have any other messages, delete them as well.
 * <p>
 * All operations are performed in one database transaction.
 */
public class DeleteMessageAndRelatedData extends AsyncTask<Void, Integer, Void> {

    private static final String TAG = DeleteMessageAndRelatedData.class.getName();

    private AppDatabase mDb;
    private MessageDao mMessageDao;
    private MessageRecipientJoinDao mJoinDao;
    private RecipientDao mRecipientDao;
    private Context mContext;

    private Long mMessageID = Long.MIN_VALUE;

    /**
     * @param db        An instance of the database to query.
     * @param messageID The ID of the message to delete.
     */
    public void setArguments(Context context, AppDatabase db, Long messageID) {
        this.mContext = context;
        this.mDb = db;
        this.mMessageID = messageID;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (mDb == null || mMessageID == Long.MIN_VALUE) {
            return null;
        }
        this.mMessageDao = mDb.messageDao();
        this.mJoinDao = mDb.messageRecipientJoinDao();
        this.mRecipientDao = mDb.recipientDao();

        // This is the most important part - everything needs to be done in 1 transaction
        mDb.runInTransaction(() -> {
            Message message = mMessageDao.findMessage(mMessageID);
            if (message == null) {
                Log.w(TAG, "Can't find message to delete with ID: " + mMessageID);
                return;
            }
            List<Recipient> recipients = mJoinDao.findRecipientsForMessage(mMessageID);
            deleteAllRelationshipsForThisMessage();
            deleteRecipientsWhoHaveNoOtherMessages(recipients);
            deleteMessage(message);
            cancelAlarm(message);
        });
        return null;
    }

    private void deleteRecipientsWhoHaveNoOtherMessages(List<Recipient> recipients) {
        for (Recipient recipient : recipients) {
            List<Message> messagesForRecipient =
                    mJoinDao.findMessagesForRecipient(recipient.getId());
            if (messagesForRecipient.isEmpty()) {
                int deletedRecipientCount = mRecipientDao.deleteRecipient(recipient);
                if (deletedRecipientCount == 0) {
                    Log.w(TAG, "Error deleting recipient: " + recipient);
                } else {
                    Log.d(TAG, "Recipient successfully deleted: " + recipient);
                }
            }
        }
    }

    private void deleteAllRelationshipsForThisMessage() {
        int deletedRelationships = mJoinDao.deleteRelationshipsForMessage(mMessageID);
        if (deletedRelationships == 0) {
            Log.w(TAG, "No relationships found for message " + mMessageID);
        }
    }

    private void deleteMessage(Message message) {
        int deletedMessageCount = mMessageDao.deleteMessageByID(mMessageID);
        if (deletedMessageCount == 0) {
            Log.w(TAG, "Error deleting message: " + message);
        }
    }

    private void cancelAlarm(Message message) {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent messageSendingIntent = createMessageSendingIntent(mContext, message);

        int pendingIntentUniqueId =
                getUniqueHashIdForMessage(message.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                pendingIntentUniqueId,
                messageSendingIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        alarmManager.cancel(pendingIntent);
    }
}
