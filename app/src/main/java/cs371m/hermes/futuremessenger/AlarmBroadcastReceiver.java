package cs371m.hermes.futuremessenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Drew on 7/30/2016.
 * Broadcast receiver that listens for BOOT COMPLETED broadcast from Android.
 * Registered in the Manifest.
 * Starts the BootService on receipt.
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("Boot receiver", "working");
        Intent i = new Intent(context, BootService.class);
        context.startService(i);
    }
}
