package cs371m.hermes.futuremessenger.support.sending;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.tasks.SendMessageWithRecipients;

import static cs371m.hermes.futuremessenger.support.SchedulingSupport.BUNDLE_KEY_MESSAGE_ID;

/**
 * This is the broadcast receiver that is fired when a scheduled message's alarm goes off.
 *
 * In newer versions of Android (after M and O especially), the device does everything it can to
 * preserve battery, so exact scheduling of jobs is extremely difficult to guarantee.
 *
 * The good news, however, is that when a BroadcastReceiver is fired, it automatically holds the
 * wakelock while its onReceive() is being executed. This means that all code in an onReceive()
 * is guaranteed to work on-time. Note that this doesn't mean, however, that you can start a service
 * in the receiver and have it work on time, because during the handoff between the receiver and
 * the service, the wakelock isn't held and the system can shut it down for a few minutes.
 *
 * This means all of our work needs to be in the onReceive() method. However, the receiver runs
 * in the main thread, so queries/updates can't be done. The solution here is {@link #goAsync()}.
 * Calling it allows the broadcast to remain active while the async task runs, and the task can
 * call finish() on it when the work is complete. According to Google documentation, any
 * broadcast that did not get started with Intent.FLAG_RECEIVER_FOREGROUND will be able to run for
 * 30 seconds or maybe more.
 *
 * https://www.reddit.com/r/androiddev/comments/71arjj/im_confused_about_android_o_and_task_execution/
 */
public class ScheduledMessageBroadcastReceiver extends BroadcastReceiver {

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
