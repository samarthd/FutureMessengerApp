package cs371m.hermes.futuremessenger;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

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
        //Toast.makeText(this, "onBind", Toast.LENGTH_LONG).show();
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
        String TAG = "AlarmReceiver, onStart";
        super.onStart(intent, startId);
        //check if extras even exist; this suppresses null pointer exceptions that happen
        //due to checking for null extras.
        long messageID = -1;

        try {
            Bundle bundle = intent.getExtras();
            messageID = bundle.getLong("message_id");
        }catch(NullPointerException e){
            Log.d(TAG, "NullPointerException");
        }
        MessengerDatabaseHelper mdb = MessengerDatabaseHelper.getInstance(this);
        Bundle results = mdb.getScheduledMessageData(messageID);
        if (results == null) {
            Log.d(TAG, "No message data found.");
        }
        else{
            String names = results.getString("recip_names");
            String numbers = results.getString("recip_nums");
            String messageText = results.getString("message");
            int groupFlag = results.getInt("group_flag");
            String img_path = results.getString("image_path");

            if (img_path != null)
                Log.d(TAG, img_path);

            Log.d(TAG, Long.toString(messageID));

            if(img_path != null){
                Log.d(TAG, "about to send picture MMS");
                sendPictureMMS(messageID, numbers, messageText, img_path);
            }
            else if (groupFlag==MessengerDatabaseHelper.IS_GROUP_MESSAGE){
                Log.d(TAG, "about to send group MMS");
                sendGroupMMS(messageID, numbers, messageText);
            }
            else {
                Log.d(TAG, "About to send SMS");
                sendIndividualSMS(messageID, names, numbers, messageText);
            }
            // Delete the message from our database after sending has been completed
            mdb.deleteMessage(messageID);
            mdb.close();
            broadcastRefreshLV();
        }
    }
    // Sends individual SMS messages to all listed recipients (same message)
    public void sendIndividualSMS(long messageID, String names, String numbers, String messageText){
        String[] numbersArray = numbers.split(";");

        for(String number : numbersArray) {
            sendSMS(messageID, number, messageText);
        }
    }

    // Sends a picture message to the specified recipient
    public void sendPictureMMS(long messageID, String phonenum, String message, String uri_path) {
        String TAG = "sendPictureMMS";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra("address", phonenum);
        intent.putExtra("sms_body", message);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(uri_path));
        intent.setType("image/*");

        Resources curResources = getResources();
        //TODO customize the text of this notification to inform the user it's a picture message.
        PendingIntent pending =
                PendingIntent.getActivity(this, (int) messageID, Intent.createChooser(intent, getResources()
                                                          .getString(R.string.mms_chooser_text)), 0);
        Bitmap largeIcon = BitmapFactory.decodeResource(curResources, R.mipmap.launcher_icon);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(pending)
               .setSmallIcon(R.drawable.picture_icon)
               .setLargeIcon(largeIcon)
               .setColor(ContextCompat.getColor(this, R.color.colorAccent))
               .setContentTitle(curResources.getString(R.string.app_name))    //title of the notification
               .setContentText(curResources.getString(R.string.send_picture_message))//actual notification content
               .setAutoCancel(true)    //clears notification after user clicks on it
               .setOngoing(true)
               .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Log.d("Picture notify ID", "" + messageID);

        mNotificationManager.notify((int) messageID, builder.build());
        Log.d(TAG, "Finished pushing notification.");
    }

    // Sends a text message to the specified recipients as a group
    public void sendGroupMMS(long messageID, String phonenums, String message) {
        String TAG = "sendGroupMMS";
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + phonenums));
        intent.putExtra("sms_body", message);
        Resources curResources = getResources();

        PendingIntent pending =
                PendingIntent.getActivity(this, (int) messageID, Intent.createChooser(intent, getResources()
                        .getString(R.string.mms_chooser_text)), 0);
        Bitmap largeIcon = BitmapFactory.decodeResource(curResources, R.mipmap.launcher_icon);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(pending)
               .setSmallIcon(R.drawable.text_icon)
               .setLargeIcon(largeIcon)
               .setColor(ContextCompat.getColor(this, R.color.colorAccent))
               .setContentTitle(curResources.getString(R.string.app_name))    //title of the notification
               .setContentText(curResources.getString(R.string.group_ready))//actual notification content
               .setStyle(new NotificationCompat.BigTextStyle()
                         .bigText(curResources.getString(R.string.open_mms_app) + " \"" + message + "\""))
               .setOngoing(true)
               .setAutoCancel(true)    //clears notification after user clicks on it
               .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Log.d("Group notify ID", "" + messageID);
        mNotificationManager.notify((int) messageID, builder.build());
        Log.d(TAG, "Finished pushing notification.");
    }

    // Notifies the user of the success/failure of SMS delivery.
    public void sendDeliveryNotification(long messageID, String result, String message){
        //TODO: change notification icon and customize text to display message
        //this intent defines where the user goes after they click the notification
        Intent resultIntent = new Intent(this, MainActivity.class);
        //resultIntent.setAction(""+System.currentTimeMillis());
        PendingIntent pendInt = PendingIntent.getActivity(this, (int) messageID, resultIntent, 0);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.launcher_icon);
        NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.text_icon)
                .setLargeIcon(largeIcon)
                .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setContentTitle(getResources().getString(R.string.app_name))//title of the notification
                .setContentText(result)//actual notification content
                .setStyle(new NotificationCompat.BigTextStyle().bigText(result + ":\n\"" + message + "\""))
                .setContentIntent(pendInt)
                .setAutoCancel(true); //clears notification after user clicks on it

        //DO NOT REMOVE THIS CODE
        /*TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);*/

        NotificationManager notification =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d("Delivery notify ID", "" + messageID);
        //notification ids are the same as the message ids
        notification.notify((int)messageID, mBuilder.build());
    }
    @Override
    public boolean onUnbind(Intent intent){
        //Toast.makeText(this, "onUnbind", Toast.LENGTH_LONG).show();
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

    private void sendSMS(final long messageID, String phoneNum, final String message) {
        try {
            Log.d("sendSMS", phoneNum + " " + message);
            String SENT = "sent " + " " + messageID + " " + phoneNum + " " + message;
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
                    Resources curResources = getResources();
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            result = curResources.getString(R.string.send_success);
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            result = curResources.getString(R.string.send_fail);
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            result = curResources.getString(R.string.radio_off);
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            result = curResources.getString(R.string.no_pdu);
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            result = curResources.getString(R.string.no_service);
                            break;
                    }
                    //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                    sendDeliveryNotification(messageID, result, message);
                    unregisterReceiver(this);
                }
            }, new IntentFilter(SENT));

            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    //Toast.makeText(getApplicationContext(), "Delivered", Toast.LENGTH_LONG).show();
                }
            }, new IntentFilter("delivered"));

            SmsManager sms = SmsManager.getDefault();
            if (message.length() >= 160) {
                ArrayList<String> parts = sms.divideMessage(message);
                ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
                ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
                for(int i = 0; i < parts.size(); ++i) {
                    sentIntents.add(PendingIntent.getBroadcast(getApplicationContext(), 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT));
                    deliveryIntents.add(PendingIntent.getBroadcast(getApplicationContext(), 0, deliveryIntent, PendingIntent.FLAG_UPDATE_CURRENT));
                }
                sms.sendMultipartTextMessage(phoneNum, null, parts, sentIntents, deliveryIntents);
                Log.d("sendSMS", "sent split messages");
            } else {
                sms.sendTextMessage(phoneNum, null, message, sentPI, deliverPI);
            }
            Log.d("sendSMS", "Text sent");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
