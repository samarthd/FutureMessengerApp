package cs371m.hermes.futuremessenger.support.sending;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.tasks.SendMessageWithRecipients;

import static cs371m.hermes.futuremessenger.support.SchedulingSupport.BUNDLE_KEY_MESSAGE_ID;

/**
 * This is the only way to guarantee having a wakelock.
 * https://www.reddit.com/r/androiddev/comments/71arjj/im_confused_about_android_o_and_task_execution/
 */
public class ScheduledMessageBroadcastReceiver extends BroadcastReceiver {

    // According to Google documentation, any broadcast that did not get started with Intent.FLAG_RECEIVER_FOREGROUND
    // will be able to run for 30 seconds or maybe more.
    // https://developer.android.com/reference/android/content/BroadcastReceiver#goAsync()

    @Override
    public void onReceive(Context context, Intent intent) {
        Context appContext = context.getApplicationContext();
        PendingResult pendingResult = goAsync();
        Long messageID = intent.getLongExtra(BUNDLE_KEY_MESSAGE_ID, Long.MIN_VALUE);
        SendMessageWithRecipients sendMessageTask = new SendMessageWithRecipients();
        sendMessageTask.setArguments(appContext, AppDatabase.getInstance(appContext), messageID, pendingResult);
        sendMessageTask.execute();
    }
}
