package cs371m.hermes.futuremessenger.ui.main.support.livedata;

import android.arch.lifecycle.MutableLiveData;

import java.util.List;

import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.pojo.MessageWithRecipients;
import cs371m.hermes.futuremessenger.tasks.QueryForMessagesWithRecipients;
import cs371m.hermes.futuremessenger.ui.main.screens.activities.MainActivity;

/**
 * MutableLiveData that the fragment tabs in
 * {@link MainActivity} look to for data.
 */
public class MessagesWithRecipientsLiveData extends MutableLiveData<List<MessageWithRecipients>> {

    private AppDatabase mDb;
    private String messageStatus;

    public MessagesWithRecipientsLiveData(String messageStatus, AppDatabase db) {
        this.mDb = db;
        this.messageStatus = messageStatus;
    }

    /**
     * Override this method so we immediately query for data when we go from 0 observers to 1.
     * <p>
     * This is important, as we cannot only update data when a table is invalidated and there
     * are active observers.
     * <p>
     * There will come a moment when there is already some data present, then a table is
     * invalidated, and there are no active observers so this data is not updated. Then an observer
     * can come online and will only have the outdated data available to it.
     * <p>
     * This method ensures that when we go from 0 observers to 1, we always query for fresh data
     * and thus each observer always has the latest data.
     */
    @Override
    protected void onActive() {
        QueryForMessagesWithRecipients queryTask =
                new QueryForMessagesWithRecipients();
        queryTask.setArguments(mDb, messageStatus, this);
        queryTask.execute();
    }
}
