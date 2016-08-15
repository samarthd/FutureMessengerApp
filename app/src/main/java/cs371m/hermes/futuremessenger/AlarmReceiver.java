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

import java.util.ArrayList;

/**
 * Created by Drew on 7/18/2016.
 * Service that is started when a message's alarm goes off.
 * Performs the sending and post-send deletion of messages.
 */
public class AlarmReceiver extends Service {

    private static String TAG = "AlarmReceiver";
    // Action to broadcast for refreshing the MainActivity's scheduled messages ListView
    private static final String REFRESH_LV_ACTION = "cs371m.hermes.futuremessenger.refreshlv";

    // Broadcast to the MainActivity that the message list has been updated
    private void broadcastRefreshLV() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent (REFRESH_LV_ACTION));
    }

    @Override
    public void onCreate(){
        Log.d(TAG, "onCreate, Creating AlarmReceiver");
    }

    @Override
    public IBinder onBind(Intent intent){
        Log.d(TAG, "onBind");
        return null;
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy, Destroying");
    }

    /* Need to do this to get bundled extras, since Service won't allow getIntent.getExtras() */
    @Override
    public void onStart(Intent intent, int startId){
        super.onStart(intent, startId);
        long messageID = -1;

        /* Check if extras even exist; this suppresses null pointer exceptions that happen
           due to checking for null extras. */
        try {
            Bundle bundle = intent.getExtras();
            messageID = bundle.getLong("message_id");
        } catch(NullPointerException e){
            Log.d(TAG, "onStart, NullPointerException");
        }
        MessengerDatabaseHelper mdb = MessengerDatabaseHelper.getInstance(this);
        Bundle results = mdb.getScheduledMessageData(messageID);
        if (results == null) {
            Log.d(TAG, "onStart, No message data found.");
        }
        else {
            Log.d(TAG, "onStart, " + Long.toString(messageID));
            String[] nameArray = results.getString("recip_names").split(";");
            String[] numArray = results.getString("recip_nums").split(";");
            ArrayList<String> namesList = new ArrayList<>();
            /* For each recipient, if they don't have a given name (aren't a contact), display
               their phone number instead. */
            for (int i = 0; i < nameArray.length; i++) {
                if (nameArray[i].equals(" "))
                    namesList.add(numArray[i]);
                else
                    namesList.add(nameArray[i]);
            }
            String names = android.text.TextUtils.join(", ", namesList);
            String numbers = results.getString("recip_nums");
            String messageText = results.getString("message");
            int groupFlag = results.getInt("group_flag");
            String img_path = results.getString("image_path");

            if(img_path != null){
                Log.d(TAG, "onStart, " + img_path);
                Log.d(TAG, "onStart, about to send picture MMS");
                sendPictureMMS(messageID, names, numbers, messageText, img_path);
            }
            else if (groupFlag == MessengerDatabaseHelper.IS_GROUP_MESSAGE){
                Log.d(TAG, "onStart, about to send group MMS");
                sendGroupMMS(messageID, names, numbers, messageText);
            }
            else {
                Log.d(TAG, "onStart, about to send SMS");
                sendIndividualSMS(messageID, numbers, messageText);
            }

            // Delete the message from our database after sending has been completed
            mdb.deleteMessage(messageID);
            mdb.close();
            broadcastRefreshLV();
        }
    }

    // Sends individual SMS messages to all listed recipients (same message)
    public void sendIndividualSMS(long messageID, String numbers, String messageText){
        String[] numbersArray = numbers.split(";");
        for(String number : numbersArray) {
            sendSMS(messageID, number, messageText);
        }
    }

    // Sends a picture message to the specified recipient
    public void sendPictureMMS(long messageID, String name, String phonenum, String message, String uri_path) {
        String TAG = "sendPictureMMS";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra("address", phonenum);
        intent.putExtra("sms_body", message);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(uri_path));
        intent.setType("image/*");

        Resources curResources = getResources();
        PendingIntent pending =
                PendingIntent.getActivity(this, (int) messageID,
                        Intent.createChooser(intent, getResources().getString(R.string.mms_chooser_text)), 0);


        // Use the app's launcher icon as the notification's large icon.
        Bitmap largeIcon = BitmapFactory.decodeResource(curResources, R.mipmap.launcher_icon);
        // Build a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(pending)
               .setSmallIcon(R.drawable.picture_icon)
               .setLargeIcon(largeIcon)
               .setColor(ContextCompat.getColor(this, R.color.colorAccent))
               .setContentTitle(curResources.getString(R.string.app_name))
               .setContentText(curResources.getString(R.string.send_picture_message) + " to " + name)
               .setAutoCancel(true)  // Clear the notification after user clicks on it
               .setOngoing(true) // Prevent this notification from being cleared via swipe
               .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Log.d("Picture notify ID", "" + messageID);
        mNotificationManager.notify((int) messageID, builder.build());
        Log.d(TAG, "Finished pushing notification.");
    }

    // Sends a text message to the specified recipients as a group
    public void sendGroupMMS(long messageID, String names, String phonenums, String message) {
        String TAG = "sendGroupMMS";
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + phonenums));
        intent.putExtra("sms_body", message);
        Resources curResources = getResources();

        PendingIntent pending =
                PendingIntent.getActivity(this, (int) messageID, Intent.createChooser(intent, getResources()
                        .getString(R.string.mms_chooser_text)), 0);

        // Use the app's launcher icon as the notification's large icon.
        Bitmap largeIcon = BitmapFactory.decodeResource(curResources, R.mipmap.launcher_icon);
        // Build a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(pending)
               .setSmallIcon(R.drawable.text_icon)
               .setLargeIcon(largeIcon)
               .setColor(ContextCompat.getColor(this, R.color.colorAccent))
               .setContentTitle(curResources.getString(R.string.app_name))
               .setContentText(curResources.getString(R.string.group_ready))
               .setStyle(new NotificationCompat.BigTextStyle() // On expansion, show the message to be sent
                         .bigText(curResources.getString(R.string.group_ready) + " to " + names + ": \"" + message + "\""))
               .setOngoing(true)  // Prevent this notification from being cleared by a swipe
               .setAutoCancel(true)   // Clears notification after user clicks on it
               .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Log.d("Group notify ID", "" + messageID);
        mNotificationManager.notify((int) messageID, builder.build());
        Log.d(TAG, "Finished pushing notification.");
    }

    // Notifies the user of the success/failure of SMS delivery.
    public void sendDeliveryNotification(long messageID, String result, String message){
        // This intent defines where the user goes after they click the notification
        // In this case, we are opening the MainActivity
        Intent resultIntent = new Intent(this, MainActivity.class);

        PendingIntent pendInt = PendingIntent.getActivity(this, (int) messageID, resultIntent, 0);

        // Set the notification icon to the launcher icon
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.launcher_icon);
        // Build a notification
        NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.text_icon)
                .setLargeIcon(largeIcon)
                .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(result)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(result + ":\n\"" + message + "\""))
                .setContentIntent(pendInt)
                .setAutoCancel(true); // Clears notification after user clicks on it

        NotificationManager notification =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d("Delivery notify ID", "" + messageID);
        // Notification ids are the same as the message IDs
        notification.notify((int)messageID, mBuilder.build());
    }

    @Override
    public boolean onUnbind(Intent intent){
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    /**
     * Actually performs the sending of an SMS to a recipient phone number, and
     * registers a receiver for its delivery status
     * @param messageID, needed to listen for delivery status
     * @param phoneNum, the target recipient
     * @param message, the text content to send
     */
    private void sendSMS(final long messageID, String phoneNum, final String message) {
        try {

            Log.d("sendSMS", phoneNum + " " + message);
            // SENT will be used to build this particular message's intent action, so that
            // no duplicates can be made.
            String SENT = "sent " + " " + messageID + " " + phoneNum + " " + message;
            String DELIVERED = "delivered";

            /* Create pending intents */
            Intent sentIntent = new Intent(SENT);
            PendingIntent sentPI = PendingIntent.getBroadcast(
                        getApplicationContext(), 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Intent deliveryIntent = new Intent(DELIVERED);
            PendingIntent deliverPI = PendingIntent.getBroadcast(
                        getApplicationContext(), 0, deliveryIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            /* Register to listen for SMS send action in order to notify the user on send status */
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
                    sendDeliveryNotification(messageID, result, message);
                    unregisterReceiver(this);
                }
            }, new IntentFilter(SENT));

            // Used for debugging
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d("Delivery onReceive", "Received delivery status");
                    unregisterReceiver(this);
                }
            }, new IntentFilter(DELIVERED));

            // Actually send the message
            SmsManager sms = SmsManager.getDefault();
            if (message.length() >= 160) {
                ArrayList<String> parts = sms.divideMessage(message);
                ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
                ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
                for(int i = 0; i < parts.size(); ++i) {
                    sentIntents.add(PendingIntent.getBroadcast(getApplicationContext(), 0,
                            sentIntent, PendingIntent.FLAG_UPDATE_CURRENT));
                    deliveryIntents.add(PendingIntent.getBroadcast(getApplicationContext(), 0,
                            deliveryIntent, PendingIntent.FLAG_UPDATE_CURRENT));
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
