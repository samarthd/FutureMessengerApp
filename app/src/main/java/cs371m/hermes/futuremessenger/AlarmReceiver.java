package cs371m.hermes.futuremessenger;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Drew on 7/18/2016.
 */
public class AlarmReceiver extends Service {
    String phoneNum, message;

    // Action to broadcast for refreshing listview
    private static final String REFRESH_LV_ACTION = "cs371m.hermes.futuremessenger.refreshlv";

    // Broadcast to the MainActivity that the message list has been updated
    private void broadcastRefreshLV() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent (REFRESH_LV_ACTION));
    }

    @Override
    public void onCreate(){
        Log.d("AlarmReciever: onCreate", "Creating AlarmReceiver");
    }

    @Override
    public IBinder onBind(Intent intent){
        Toast.makeText(this, "onBind", Toast.LENGTH_LONG).show();
        Log.d("AlarmReciever: onBind", "");
        return null;
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d("AlarmReciever: Destroy", "Destroying");
    }
    /* Need to do this to get bundled extras, since Service won't allow getIntent.getExtras() */
    @Override
    public void onStart(Intent intent, int startId){
        super.onStart(intent, startId);

        Bundle bundle = intent.getExtras();
        long message_id = bundle.getLong("message_id");
        phoneNum = (String) bundle.getCharSequence("num");
        message = (String) bundle.getCharSequence("message");
        Log.d("AlarmReciever: onStart", Long.toString(message_id));
        Log.d("AlarmReciever: onStart", "About to send SMS");

        sendSMS(phoneNum, message);
        MessengerDatabaseHelper mDb = new MessengerDatabaseHelper(this);
        mDb.deleteMessage(message_id);
        mDb.close();
        broadcastRefreshLV();
        //Update the MainActivity to have the right value.

    }

    public void onStartCommand(){

    }
    @Override
    public boolean onUnbind(Intent intent){
        Toast.makeText(this, "onUnbind", Toast.LENGTH_LONG).show();
        Log.d("AlarmReciever: onUnbind", "");
        return super.onUnbind(intent);
    }

    //TODO: Determine whether this method needs to be used in order to use BroadCast Receivers
    /*
    @Override
    public void onReceive(Context context, Intent intent){
        Toast.makeText(context, "Made it to alarm", Toast.LENGTH_SHORT).show();

        Bundle extras = intent.getExtras();
        if (extras != null) {
            phoneNum = (String) extras.getCharSequence("num");
            message = (String) extras.getCharSequence("message");
        }
        sendSMS(phoneNum, message);
    }*/

    private void sendSMS(String phoneNum, String message) {
        try {
            Log.d("sendSMS", phoneNum + " " + message);

            /* Create pending intents */
            Intent sentIntent = new Intent("sent");
            PendingIntent sentPI = PendingIntent.getBroadcast(getApplicationContext(), 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Intent deliveryIntent = new Intent("delivered");
            PendingIntent deliverPI = PendingIntent.getBroadcast(getApplicationContext(), 0, deliveryIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            /* Register for SMS send action */
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String result = "";
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            result = "Transmission successful";
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            result = "Transmission failed";
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            result = "Radio off";
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            result = "No PDU defined";
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            result = "No service";
                            break;
                    }
                    Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                }
            }, new IntentFilter("sent"));

            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Toast.makeText(getApplicationContext(), "Delivered", Toast.LENGTH_LONG).show();
                }
            }, new IntentFilter("delivered"));

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNum, null, message, sentPI, deliverPI);
            Log.d("sendSMS", "Text sent?");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
