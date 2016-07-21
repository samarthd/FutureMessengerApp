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
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Drew on 7/18/2016.
 */
public class AlarmReceiver extends Service {
    String phoneNum, message;


    @Override
    public void onCreate(){
        //Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();
        Log.d("AlarmReciever: onCreate", "Creating AlarmReceiver");
    }

    @Override
    public IBinder onBind(Intent intent){
        Toast.makeText(this, "onBind", Toast.LENGTH_LONG).show();
        Log.d("AlarmReciever: onBind", "");
        return null;
    }

    //not needed
    @Override
    public void onDestroy(){
        Toast.makeText(this, "onDestroy", Toast.LENGTH_LONG).show();
        Log.d("AlarmReciever: Destroy", "Destroying");
        super.onDestroy();
    }
    /* Need to do this to get bundled extras, since Service won't allow getIntent.getExtras() */
    @Override
    public void onStart(Intent intent, int startId){
        super.onStart(intent, startId);

        Bundle bundle = intent.getExtras();
        long message_id = bundle.getLong("message_id");
        phoneNum = (String) bundle.getCharSequence("num");
        message = (String) bundle.getCharSequence("message");
        // Toast.makeText(this, "onStart, trying to send message", Toast.LENGTH_LONG).show();
        // Toast.makeText(this, "number: "+phoneNum+"\nmessage: "+message, Toast.LENGTH_LONG).show();
        Log.d("AlarmReciever: onStart", Long.toString(message_id));
        Log.d("AlarmReciever: onStart", "About to send SMS");

        sendSMS(phoneNum, message);

    }

    @Override
    public boolean onUnbind(Intent intent){
        Toast.makeText(this, "onUnbind", Toast.LENGTH_LONG).show();
        Log.d("AlarmReciever: onUnbind", "");
        return super.onUnbind(intent);
    }

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
