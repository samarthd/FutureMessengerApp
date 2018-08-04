package cs371m.hermes.futuremessenger.ui.main;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.entities.embedded.Status;
import cs371m.hermes.futuremessenger.persistence.pojo.MessageWithRecipients;
import cs371m.hermes.futuremessenger.persistence.repositories.isolated.MessageDao;
import cs371m.hermes.futuremessenger.persistence.repositories.joined.MessageRecipientJoinDao;


public class MainViewModel extends ViewModel {


    // Message lists that Room will automatically update on message table updates
    private LiveData<List<Message>> mScheduledMessages;
    private LiveData<List<Message>> mSentMessages;
    private LiveData<List<Message>> mFailedMessages;

    /**
     * These are MediatorLiveData objects that will observe for changes in the
     * LiveData message lists, and upon seeing a change, will update themselves.
     *
     * These lists hold MessageWithRecipients objects, so when an update is triggered,
     * they will query the database for the recipients of each message.
     */
    private MediatorLiveData<List<MessageWithRecipients>> mScheduledMessagesWithRecipients;
    private MediatorLiveData<List<MessageWithRecipients>> mSentMessagesWithRecipients;
    private MediatorLiveData<List<MessageWithRecipients>> mFailedMessagesWithRecipients;


    private AppDatabase mDb;
    private MessageDao mMessageDao;
    private MessageRecipientJoinDao mMessageRecipientJoinDao;


    public MainViewModel (@NonNull Application application) {
        // Get database and repositories
        mDb = AppDatabase.getInstance(application);
        mMessageDao = mDb.messageDao();
        mMessageRecipientJoinDao = mDb.messageRecipientJoinDao();
    }

    /**
     * Takes a list of messages, and finds the recipients for each and returns the new list of
     * MessageWithRecipients objects.
     */
    private List<MessageWithRecipients> mapFromMessagesToMessagesWithRecipients(List<Message> messages) {
        List<MessageWithRecipients> result = new ArrayList<>();
        for(Message message : messages) {
            List<Recipient> recipients =
                    mMessageRecipientJoinDao.findRecipientsForMessage(message.getId());
            MessageWithRecipients messageWithRecipients
                    = new MessageWithRecipients(message, recipients);
            result.add(messageWithRecipients);
        }
        return result;
    }

    public LiveData<List<MessageWithRecipients>> getScheduledMessagesWithRecipients() {
        if (mScheduledMessagesWithRecipients == null) {
            mScheduledMessages = mMessageDao.findAllMessagesWithStatusCode(Status.SCHEDULED);
            mScheduledMessagesWithRecipients = new MediatorLiveData<>();
            mScheduledMessagesWithRecipients
                    .addSource(mScheduledMessages, this::mapFromMessagesToMessagesWithRecipients);
        }
        return mScheduledMessagesWithRecipients;
    }

    public LiveData<List<MessageWithRecipients>> getSentMessagesWithRecipients() {
        if (mSentMessagesWithRecipients == null) {
            mSentMessages = mMessageDao.findAllMessagesWithStatusCode(Status.SENT);
            mSentMessagesWithRecipients = new MediatorLiveData<>();
            mSentMessagesWithRecipients
                    .addSource(mSentMessages, this::mapFromMessagesToMessagesWithRecipients);
        }
        return mSentMessagesWithRecipients;
    }

    public LiveData<List<MessageWithRecipients>> getFailedMessagesWithRecipients() {
        if (mFailedMessagesWithRecipients == null) {
            mFailedMessages = mMessageDao.findAllMessagesWithStatusCode(Status.FAILED);
            mFailedMessagesWithRecipients = new MediatorLiveData<>();
            mFailedMessagesWithRecipients
                    .addSource(mFailedMessages, this::mapFromMessagesToMessagesWithRecipients);
        }
        return mFailedMessagesWithRecipients;
    }
    
}
