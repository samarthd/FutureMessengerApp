package cs371m.hermes.futuremessenger.ui.main;

import android.arch.lifecycle.MutableLiveData;

import java.util.List;

import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.pojo.MessageWithRecipients;

public class MessagesWithRecipientsLiveData extends MutableLiveData<List<MessageWithRecipients>> {

    private AppDatabase mDb;
    private String messageStatus;

    public MessagesWithRecipientsLiveData(String messageStatus, AppDatabase db) {
        this.mDb = db;
        this.messageStatus = messageStatus;
    }

    /**
     * Override this method so we immediately query for data when we get an observer.
     */
    @Override
    protected void onActive() {
        QueryForMessagesWithRecipients queryTask =
                new QueryForMessagesWithRecipients();
        queryTask.setArguments(mDb, messageStatus, this);
        queryTask.execute();
    }
}
