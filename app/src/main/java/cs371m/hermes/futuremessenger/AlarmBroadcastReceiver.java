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
        Log.d("Boot receiver", "working");
        Toast.makeText(context, "BOOT RECEIVER WORKING", Toast.LENGTH_LONG).show();
        //Intent in = new Intent(context, AlarmReceiver.class);
        //context.startService(in);

    }
}
