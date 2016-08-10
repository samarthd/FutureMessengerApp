package cs371m.hermes.futuremessenger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Drew on 7/30/2016.
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        //TODO CHANGE THE SERVICE THAT IS LAUNCHED. MAKE A SERVICE TO RE-SET ALL ALARMS/SEND MESSAGES.
        Log.w("Boot receiver", "working");
        Intent i = new Intent(context, BootService.class);
        context.startService(i);
        //Intent in = new Intent(context, AlarmReceiver.class);
        //context.startService(in);
    }
}
