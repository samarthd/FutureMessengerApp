package cs371m.hermes.futuremessenger.tasks;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;

import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.repositories.isolated.MessageDao;

import static cs371m.hermes.futuremessenger.persistence.entities.embedded.Status.SCHEDULED;

/**
 * Checks if a given scheduled message has become deleted/sent or otherwise invalidated
 * and thus should no longer be editable. Sets a signal to kill the activity where the
 * message is being edited.
 *
 * @see cs371m.hermes.futuremessenger.ui.draft.screens.EditTextMessageActivity
 */
public class CloseEditActivityIfScheduledMessageInvalidated extends AsyncTask<Void, Integer, Message> {

    private AppDatabase mDb;
    private MessageDao mMessageDao;
    private Long mMessageID = Long.MIN_VALUE;

    private WeakReference<AppCompatActivity> mActivityToKill;

    /**
     * @param db An instance of the database to query.
     * @param messageID The ID of the message to check for.
     */
    public void setArguments(AppDatabase db, Long messageID, AppCompatActivity activityToKill) {
        this.mDb = db;
        this.mMessageID = messageID;
        this.mActivityToKill = new WeakReference<>(activityToKill);
    }

    @Override
    protected Message doInBackground(Void... voids) {

        if (mDb == null || mMessageID == Long.MIN_VALUE) {
            return null;
        }
        this.mMessageDao = mDb.messageDao();
        return mMessageDao.findMessage(mMessageID);
    }

    @Override
    protected void onPostExecute(Message result) {
        if (result == null ||
            !StringUtils.equals(SCHEDULED, result.getStatus().getCode())) {
            AppCompatActivity activityToKill = mActivityToKill.get();
            if (activityToKill != null) {
                activityToKill.finish();
            }
        }
    }
}
