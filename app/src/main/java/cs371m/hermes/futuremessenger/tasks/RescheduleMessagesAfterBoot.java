package cs371m.hermes.futuremessenger.tasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.AsyncTask;

import java.util.Calendar;
import java.util.List;

import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.entities.Message;

import static cs371m.hermes.futuremessenger.persistence.entities.embedded.Status.FAILED;
import static cs371m.hermes.futuremessenger.persistence.entities.embedded.Status.SCHEDULED;
import static cs371m.hermes.futuremessenger.support.SchedulingSupport.getContentTextForMessageScheduledWhileOff;
import static cs371m.hermes.futuremessenger.support.SchedulingSupport.scheduleMessageNonRepeating;
import static cs371m.hermes.futuremessenger.support.SchedulingSupport.showOrUpdateSentNotificationForMessage;

public class RescheduleMessagesAfterBoot extends AsyncTask<Void, Void, Void> {

    private Context mContext;
    private BroadcastReceiver.PendingResult mPendingResult;

    public void setArguments(Context context, BroadcastReceiver.PendingResult pendingResult) {
        mPendingResult = pendingResult;
        mContext = context.getApplicationContext();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        AppDatabase db = AppDatabase.getInstance(mContext);
        List<Message> scheduledMessages =
                db.messageDao().findAllMessagesWithStatusCode(SCHEDULED);
        Calendar currentTime = Calendar.getInstance();
        for (Message message : scheduledMessages) {
            if (currentTime.after(message.getScheduledDateTime())) {
                message.getStatus().setCode(FAILED);
                db.messageDao().updateMessage(message);
                CharSequence notificationContentText = getContentTextForMessageScheduledWhileOff(mContext, message);
                showOrUpdateSentNotificationForMessage(mContext, message, notificationContentText);
            } else {
                scheduleMessageNonRepeating(mContext, message);
            }
        }
        mPendingResult.finish();
        return null;
    }
}
