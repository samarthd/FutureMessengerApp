package cs371m.hermes.futuremessenger.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.pojo.MessageWithRecipients;
import cs371m.hermes.futuremessenger.persistence.repositories.isolated.MessageDao;
import cs371m.hermes.futuremessenger.persistence.repositories.isolated.RecipientDao;
import cs371m.hermes.futuremessenger.persistence.repositories.joined.MessageRecipientJoinDao;
import cs371m.hermes.futuremessenger.ui.main.MessagesWithRecipientsLiveData;

/**
 * An asynchronous task that can query for messages with a certain status, retrieve their recipients,
 * and then update the specified LiveData object with the updated values.
 */
public class QueryForMessagesWithRecipients
            extends AsyncTask<Void, Integer, List<MessageWithRecipients>> {

        private AppDatabase mDb;
        private MessageDao mMessageDao;
        private MessageRecipientJoinDao mJoinDao;
        private RecipientDao mRecipientDao;

        private MessagesWithRecipientsLiveData targetLiveData;
        private String messageStatus;

    /**
     * @param db An instance of the database to query.
     * @param messageStatus The type of message to get (scheduled, sent, failed)
     * @param targetLiveData The target LiveData which will be updated with the final results
     */
        public void setArguments(AppDatabase db,
                                 String messageStatus,
                                 MessagesWithRecipientsLiveData targetLiveData) {
            this.mDb = db;
            this.targetLiveData = targetLiveData;
            this.messageStatus = messageStatus;
        }

        @Override
        protected List<MessageWithRecipients> doInBackground(Void... params) {
            if (targetLiveData == null || mDb == null || messageStatus == null)
                return new ArrayList<>();

            this.mMessageDao = mDb.messageDao();
            this.mJoinDao = mDb.messageRecipientJoinDao();
            this.mRecipientDao = mDb.recipientDao();

            List<Message> messages = mMessageDao.findAllMessagesWithStatusCode(messageStatus);
            if (messageStatus.equals(cs371m.hermes.futuremessenger.persistence.entities.embedded.Status.SCHEDULED))
                Log.d("In async query task",
                      "Found " + messages.size() + " messages with status " + messageStatus);
            if (messages.isEmpty())
                return new ArrayList<>();
            return mapFromMessagesToMessagesWithRecipients(messages);
        }

        @Override
        protected void onPostExecute(List<MessageWithRecipients> result) {
            // Call setValue() as onPostExecute() is called on the UI thread
            targetLiveData.setValue(result);
        }

        /**
         * Takes a list of messages, and finds the recipients for each and returns the new list of
         * MessageWithRecipients objects.
         */
        private List<MessageWithRecipients> mapFromMessagesToMessagesWithRecipients(List<Message> messages) {
            List<MessageWithRecipients> result = new ArrayList<>();
            for(Message message : messages) {
                List<Recipient> recipients =
                        mJoinDao.findRecipientsForMessage(message.getId());
                MessageWithRecipients messageWithRecipients
                        = new MessageWithRecipients(message, recipients);
                result.add(messageWithRecipients);
            }
            return result;
        }
    }