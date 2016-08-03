package cs371m.hermes.futuremessenger;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Drew on 7/18/2016.
 */
public class AlarmReceiver extends Service {
    String phoneNum, message;
    long messageID;
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
        //check if extras even exist; this suppresses null pointer exceptions that happen
        //due to checking for null extras.
        try {
            Bundle bundle = intent.getExtras();
            messageID = bundle.getLong("message_id");
            phoneNum = (String) bundle.getCharSequence("num");
            message = (String) bundle.getCharSequence("message");
        }catch(NullPointerException e){
            Log.d("Alarm", "NullPointerException");
        }

        Log.d("AlarmReciever: onStart", Long.toString(messageID));
        Log.d("AlarmReciever: onStart", "About to send SMS");

        sendSMS(phoneNum, message);
/*        MessengerDatabaseHelper mDb = new MessengerDatabaseHelper(this);
        mDb.deleteMessage(messageID);
        mDb.close();*/
        broadcastRefreshLV();
        //Update the MainActivity to have the right value.

    }


    public void sendNotification(String result){
        //TODO: change notification icon and customize text to display message
        //this intent defines where the user goes after they click the notification
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, resultIntent, 0);

        NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Future Messenger")//title of the notification
                .setContentText(result)//actual notification content
                .setContentIntent(pendInt)
                .setAutoCancel(true)//clears notification after user clicks on it
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        //DO NOT REMOVE THIS CODE
        /*TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);*/

        NotificationManager notification =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //notification ids are the same as the message ids
        notification.notify((int)messageID, mBuilder.build());
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
            String SENT = "sent";
            String DELIVERED = "delivered";

            /* Create pending intents */
            Intent sentIntent = new Intent(SENT);
            PendingIntent sentPI = PendingIntent.getBroadcast(getApplicationContext(), 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Intent deliveryIntent = new Intent(DELIVERED);
            PendingIntent deliverPI = PendingIntent.getBroadcast(getApplicationContext(), 0, deliveryIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            /* Register for SMS send action */
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String result = "";
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            result = "Message successfully sent.";
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            result = "Message failed to send.";
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
                    //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                    sendNotification(result);
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
            Log.d("sendSMS", "Text sent");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
