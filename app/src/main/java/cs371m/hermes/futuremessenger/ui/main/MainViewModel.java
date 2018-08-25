package cs371m.hermes.futuremessenger.ui.main;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.InvalidationTracker;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;
import java.util.Set;

import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.entities.embedded.Status;
import cs371m.hermes.futuremessenger.persistence.pojo.MessageWithRecipients;
import cs371m.hermes.futuremessenger.tasks.QueryForMessagesWithRecipients;

/**
 * This is a ViewModel that holds the data necessary to populate all of the tabs in
 * the {@link cs371m.hermes.futuremessenger.ui.main.screens.MainActivity}.
 *
 * It observes the database's InvalidationTracker, and on changes to any tables,
 * runs a background task to query for the new data and updates the data it has.
 */
public class MainViewModel extends AndroidViewModel {

    // Tracks changes in the database
    private InvalidationTracker tableChangeTracker;

    /**
     * These are MutableLiveData objects that will be notified on changes in the
     * database, and upon seeing a change, will update themselves.
     *
     * They have lists that hold MessageWithRecipients objects, so when an update is triggered,
     * they will have the recipients for each message.
     */
    private MessagesWithRecipientsLiveData mScheduledMessagesWithRecipients;
    private MessagesWithRecipientsLiveData mSentMessagesWithRecipients;
    private MessagesWithRecipientsLiveData mFailedMessagesWithRecipients;


    private AppDatabase mDb;

    public MainViewModel (@NonNull Application application) {
        super(application);
        // Get database
        mDb = AppDatabase.getInstance(application);
        setUpInvalidationTracker();

    }

    private void setUpInvalidationTracker() {
        // Track changes in the database
        String[] tablesToTrack = {"messages", "recipients", "messages_recipients_join"};
        tableChangeTracker = mDb.getInvalidationTracker();
        tableChangeTracker.addObserver(new InvalidationTracker.Observer(tablesToTrack) {

            /**
             * When any of the tables are invalidated, we want to re-run the queries
             * for all LiveData that have active observers.
             *
             * @param tables the tables that were invalidated
             *
             * @see MessagesWithRecipientsLiveData#onActive()
             */
            @Override
            public void onInvalidated(@NonNull Set<String> tables) {
                Log.d("In onInvalidated()", "Tables invalidated: " + tables.toString());
                if (mScheduledMessagesWithRecipients != null &&
                        mScheduledMessagesWithRecipients.hasActiveObservers()) {
                    asyncUpdateScheduledMessagesWithRecipients();
                }
                if (mSentMessagesWithRecipients != null &&
                        mSentMessagesWithRecipients.hasActiveObservers()) {
                    asyncUpdateSentMessagesWithRecipients();
                }
                if (mFailedMessagesWithRecipients != null &&
                        mFailedMessagesWithRecipients.hasActiveObservers()) {
                    asyncUpdateFailedMessagesWithRecipients();
                }
            }
        });
    }


    private void asyncUpdateScheduledMessagesWithRecipients() {
        QueryForMessagesWithRecipients queryTask =
                new QueryForMessagesWithRecipients();
        queryTask.setArguments(mDb, Status.SCHEDULED, mScheduledMessagesWithRecipients);
        queryTask.execute();
    }

    private void asyncUpdateSentMessagesWithRecipients() {
        QueryForMessagesWithRecipients queryTask =
                new QueryForMessagesWithRecipients();
        queryTask.setArguments(mDb, Status.SENT, mSentMessagesWithRecipients);
        queryTask.execute();
    }

    private void asyncUpdateFailedMessagesWithRecipients() {
        QueryForMessagesWithRecipients queryTask =
                new QueryForMessagesWithRecipients();
        queryTask.setArguments(mDb, Status.FAILED, mFailedMessagesWithRecipients);
        queryTask.execute();
    }

    public LiveData<List<MessageWithRecipients>> getScheduledMessagesWithRecipients() {
        if (mScheduledMessagesWithRecipients == null) {
            Log.d("In ViewModel", "Scheduled messages LiveData was null, initializing");
            mScheduledMessagesWithRecipients =
                    new MessagesWithRecipientsLiveData(Status.SCHEDULED, mDb);
        }
        return mScheduledMessagesWithRecipients;
    }

    public LiveData<List<MessageWithRecipients>> getSentMessagesWithRecipients() {
        if (mSentMessagesWithRecipients == null) {
            Log.d("In ViewModel", "Sent messages LiveData was null, initializing");
            mSentMessagesWithRecipients = new MessagesWithRecipientsLiveData(Status.SENT, mDb);
        }
        return mSentMessagesWithRecipients;
    }

    public LiveData<List<MessageWithRecipients>> getFailedMessagesWithRecipients() {
        if (mFailedMessagesWithRecipients == null) {
            Log.d("In ViewModel", "Failed messages LiveData was null, initializing");
            mFailedMessagesWithRecipients = new MessagesWithRecipientsLiveData(Status.FAILED, mDb);
        }
        return mFailedMessagesWithRecipients;
    }
    
}
