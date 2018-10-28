package cs371m.hermes.futuremessenger.tasks;

import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;

import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.repositories.MessageDao;
import cs371m.hermes.futuremessenger.ui.edit.screens.activities.EditTextMessageActivity;
import cs371m.hermes.futuremessenger.ui.main.screens.dialogs.ScheduledMessageOptionsDialog;

import static cs371m.hermes.futuremessenger.persistence.entities.embedded.Status.SCHEDULED;

/**
 * Checks if a given scheduled message has become deleted/sent or otherwise invalidated
 * and thus is no longer valid to be interacted with. If the given object is a dialog, dismisses
 * it, or if it's an activity, will finish it so that the user can't interact with it anymore.
 *
 * @see EditTextMessageActivity
 * @see ScheduledMessageOptionsDialog
 */
public class FinishActivityOrDismissDialogIfScheduledMessageInvalidated
        extends AsyncTask<Void, Integer, Boolean> {

    private AppDatabase mDb;
    private MessageDao mMessageDao;
    private Long mMessageID = Long.MIN_VALUE;

    private WeakReference<Object> mActivityOrDialogToKill;

    /**
     * @param db        An instance of the database to query.
     * @param messageID The ID of the message to check for.
     */
    public void setArguments(AppDatabase db, Long messageID, Object activityOrDialogToKill) {
        this.mDb = db;
        this.mMessageID = messageID;
        this.mActivityOrDialogToKill = new WeakReference<>(activityOrDialogToKill);
    }

    /**
     * Return true if the activity should be killed, false otherwise.
     */
    @Override
    protected Boolean doInBackground(Void... voids) {

        if (mDb == null || mMessageID == null || mMessageID == Long.MIN_VALUE) {
            return false;
        }

        this.mMessageDao = mDb.messageDao();
        Message foundMessage = mMessageDao.findMessage(mMessageID);
        return foundMessage == null || // message was deleted
                !StringUtils.equals(SCHEDULED, foundMessage.getStatus().getCode());

    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result != null && result) {
            Object activityOrDialogToKill = mActivityOrDialogToKill.get();
            if (activityOrDialogToKill != null) {
                if (activityOrDialogToKill instanceof AppCompatActivity) {
                    ((AppCompatActivity) activityOrDialogToKill).finish();
                } else if (activityOrDialogToKill instanceof DialogFragment) {
                    ((DialogFragment) activityOrDialogToKill).dismiss();
                }
            }
        }
    }
}
