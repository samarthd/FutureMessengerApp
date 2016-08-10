package cs371m.hermes.futuremessenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Drew on 7/30/2016.
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("Boot receiver", "working");
        Intent i = new Intent(context, BootService.class);
        context.startService(i);
    }
}
