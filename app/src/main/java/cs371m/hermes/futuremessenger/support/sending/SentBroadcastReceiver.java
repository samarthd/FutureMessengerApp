package cs371m.hermes.futuremessenger.support.sending;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.tasks.UpdateMessageStatusWithResultCodeOfMessagePart;

import static cs371m.hermes.futuremessenger.support.SchedulingSupport.BUNDLE_KEY_MESSAGE_ID;
import static cs371m.hermes.futuremessenger.support.SchedulingSupport.BUNDLE_KEY_MESSAGE_PART_INDEX;
import static cs371m.hermes.futuremessenger.support.SchedulingSupport.BUNDLE_KEY_RECIPIENT;

public class SentBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int sentResultCode = getResultCode();

        /*
            Need to do this stupid nested bundle for this reason:
            https://stackoverflow.com/questions/39209579/how-to-pass-custom-serializable-object-to-broadcastreceiver-via-pendingintent
            https://commonsware.com/blog/2016/07/22/be-careful-where-you-use-custom-parcelables.html
         */
        Recipient recipient =
                (Recipient) intent.getBundleExtra(BUNDLE_KEY_RECIPIENT)
                                  .getSerializable(BUNDLE_KEY_RECIPIENT);

        int messagePartIndex = intent.getIntExtra(BUNDLE_KEY_MESSAGE_PART_INDEX, Integer.MIN_VALUE);
        Long messageID = intent.getLongExtra(BUNDLE_KEY_MESSAGE_ID, Long.MIN_VALUE);

        PendingResult pendingResult = goAsync();
        AppDatabase db = AppDatabase.getInstance(context);
        Resources resources = context.getApplicationContext().getResources();

        UpdateMessageStatusWithResultCodeOfMessagePart updateMessageStatusTask =
                new UpdateMessageStatusWithResultCodeOfMessagePart();
        updateMessageStatusTask.setArguments(context, pendingResult, resources, db, messageID,
                recipient, messagePartIndex, sentResultCode);
        updateMessageStatusTask.execute();
    }
}
