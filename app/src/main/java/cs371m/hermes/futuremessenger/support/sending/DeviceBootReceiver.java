package cs371m.hermes.futuremessenger.support.sending;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cs371m.hermes.futuremessenger.tasks.RescheduleMessagesAfterBoot;

/**
 * Broadcast receiver that is triggered when the device boots. Necessary to re-set all alarms,
 * as alarms are cleared as soon as the device is turned off.
 */
public class DeviceBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PendingResult pendingResult = goAsync();
        RescheduleMessagesAfterBoot rescheduleMessagesAfterBoot = new RescheduleMessagesAfterBoot();
        rescheduleMessagesAfterBoot.setArguments(context, pendingResult);
        rescheduleMessagesAfterBoot.execute();
    }
}
