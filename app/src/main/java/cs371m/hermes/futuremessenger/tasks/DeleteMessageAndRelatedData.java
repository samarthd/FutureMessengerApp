package cs371m.hermes.futuremessenger.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.repositories.isolated.MessageDao;
import cs371m.hermes.futuremessenger.persistence.repositories.isolated.RecipientDao;
import cs371m.hermes.futuremessenger.persistence.repositories.joined.MessageRecipientJoinDao;

/**
 * Deletes a message from the database, along with all join entries. If the associated recipients
 * do not have any other messages, delete them as well.
 *
 * All operations are performed in one database transaction.
 */
public class DeleteMessageAndRelatedData extends AsyncTask<Void, Integer, Void>{

    private static final String TAG = DeleteMessageAndRelatedData.class.getName();

    private AppDatabase mDb;
    private MessageDao mMessageDao;
    private MessageRecipientJoinDao mJoinDao;
    private RecipientDao mRecipientDao;

    private Long messageID = Long.MIN_VALUE;

    /**
     * @param db An instance of the database to query.
     * @param messageID The ID of the message to delete.
     */
    public void setArguments(AppDatabase db, Long messageID) {
        this.mDb = db;
        this.messageID = messageID;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (mDb == null || messageID == Long.MIN_VALUE) {
            return null;
        }
        this.mMessageDao = mDb.messageDao();
        this.mJoinDao = mDb.messageRecipientJoinDao();
        this.mRecipientDao = mDb.recipientDao();

        Runnable deleteMessageAndRelatedData =
                () -> {
                    Message message = mMessageDao.findMessage(messageID);
                    if (message == null) {
                        Log.w(TAG, "Can't find message to delete with ID: " + messageID);
                        return;
                    }

                    // first get all of the recipients for the message
                    List<Recipient> recipients = mJoinDao.findRecipientsForMessage(messageID);

                    // delete relationships for this message
                    int deletedRelationships = mJoinDao.deleteRelationshipsForMessage(messageID);
                    if (deletedRelationships == 0) {
                        Log.w(TAG, "No relationships found for message " + messageID);
                    }

                    // for each of the recipients query if they have any messages now, delete if none
                    for (Recipient recipient : recipients) {
                        List<Message> messagesForRecipient =
                                mJoinDao.findMessagesForRecipient(recipient.getId());
                        if (messagesForRecipient.isEmpty()) {
                            int deletedRecipientCount = mRecipientDao.deleteRecipient(recipient);
                            if (deletedRecipientCount == 0) {
                                Log.w(TAG, "Error deleting recipient: " + recipient);
                            }
                            else {
                                Log.d(TAG, "Recipient successfully deleted: " + recipient);
                            }
                        }
                    }

                    // delete message
                    int deletedMessageCount = mMessageDao.deleteMessageByID(messageID);
                    if (deletedMessageCount == 0) {
                        Log.w(TAG, "Error deleting message: " + message);
                    }
                };
        // This is the key to avoid
        mDb.runInTransaction(deleteMessageAndRelatedData);
        return null;
    }
}
