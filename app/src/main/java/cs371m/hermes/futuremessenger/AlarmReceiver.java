package cs371m.hermes.futuremessenger;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.android.mms.dom.smil.parser.SmilXmlSerializer;
import com.android.mms.service_alt.MmsConfig;
import com.android.mms.service_alt.MmsConfigManager;
import com.android.mms.service_alt.SendRequest;
import com.google.android.mms.ContentType;
import com.google.android.mms.InvalidHeaderValueException;
import com.google.android.mms.MMSPart;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.CharacterSets;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.PduBody;
import com.google.android.mms.pdu_alt.PduComposer;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduPart;
import com.google.android.mms.pdu_alt.PduPersister;
import com.google.android.mms.pdu_alt.SendReq;
import com.google.android.mms.smil.SmilHelper;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.MmsSentReceiver;
import com.klinker.android.send_message.Transaction;
import com.klinker.android.send_message.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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

            String numbers = results.getString("recip_nums");
            String messageText = results.getString("message");
            int groupFlag = results.getInt("group_flag");
            String img_path = results.getString("image_path");

            String[] indivNums = numbers.split(";");

            if(img_path != null){
                Log.d(TAG, "onStart, " + img_path);
                Log.d(TAG, "onStart, about to send picture MMS");
                //sendPictureMMS(messageID, numbers, messageText, img_path);

                try {
                    Uri imageUri = Uri.parse(img_path);
                    Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    //Jpeg, png, ??
                    String imageType = getContentResolver().getType(imageUri);
                    handleMMS(messageText, indivNums, new Bitmap[] {image}, new String[] {imageType}, "", messageID);
                }
                catch (IOException e) {
                    Log.d("About to send picMMS", "IO Exception");
                }

            }
            else if (groupFlag == MessengerDatabaseHelper.IS_GROUP_MESSAGE){
                Log.d(TAG, "onStart, about to send group MMS");
                //sendGroupMMS(messageID, numbers, messageText);
                handleMMS(messageText, indivNums, new Bitmap[]{}, new String[] {}, "", messageID);
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
    public void sendPictureMMS(long messageID, String phonenum, String message, String uri_path) {
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
               .setContentText(curResources.getString(R.string.send_picture_message))
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
    public void sendGroupMMS(long messageID, String phonenums, String message) {
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
                         .bigText(curResources.getString(R.string.open_mms_app) + " \"" + message + "\""))
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


    /* MMS Sending. Code taken from
    https://github.com/klinker41/android-smsmms
     */

    /* This code is used to build and send an MMS message to a recipient.
    *  Heavily based on Jacob Klinker's own implementation from https://github.com/klinker41
     *  Made my own modifications to some of his internal methods to get it working in addition
     *  to using some helpful classes that he made. Started with sendMmsMessage method in
      *  Transaction.java*/
    private void handleMMS (String messageText, String[] recipientNums, Bitmap[] images, String[] imgMimeTypes, String subject, final long messageID) {


        // Data chunks for the actual MMS
        ArrayList<MMSPart> data = new ArrayList<>();

        // Get all images from the bitmap array and put them in the MMS
        for (int i = 0; i < images.length; i++) {
            // Convert bitmap to bytes for storage
            byte[] imageBytes = Message.bitmapToByteArray(images[i]);
            // Is this a jpeg, a png, ??
            MMSPart curImagePart = new MMSPart();
            /** TODO: Properly set the type based on what kind of image it is (the calling method should
               automatically get this from the URI */
            curImagePart.MimeType = imgMimeTypes[i];
            curImagePart.Name = "image" + i;
            curImagePart.Data = imageBytes;
            data.add(curImagePart);
        }

        // Get the text part of the message if it exists
        if (!messageText.equals("")) {
            MMSPart textPart = new MMSPart();
            textPart.Name = "text";
            textPart.MimeType = "text/plain";
            textPart.Data = messageText.getBytes();
            data.add(textPart);
        }

        //build the pdu
        SendReq curReq = buildPdu(this, recipientNums, subject, data);
        processMMSSending(curReq, messageID, messageText);
    }

    /* Again, this code is heavily based on Jacob Klinker's own implementation in
       https://github.com/klinker41. I have made small adjustments to fit my needs.
     *  */
    private void processMMSSending (SendReq curReq, final long messageID, final String messageText) {

        final String fileName = "send." + String.valueOf(Math.abs(new Random().nextLong())) + ".dat";
        File sendingFile = new File (this.getCacheDir(), fileName);
        PduPersister persister = PduPersister.getPduPersister(this);
        try {
 /*          This method only works if you're the default SMS app.
                Uri messageUri = persister.persist(curReq, Uri.parse("content://mms/outbox"),
                    true, true, null);*/

            // Build an intent for the sent receiver
            String MMS_SENT_ACTION = "mms sent " + messageID;
            Intent intent = new Intent(MMS_SENT_ACTION);
            intent.putExtra("message", messageText);
            final PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            //Write the data to the file
            Uri writerUri = (new Uri.Builder())
                    .authority(this.getPackageName() + ".MmsFileProvider")
                    .path(fileName)
                    .scheme(ContentResolver.SCHEME_CONTENT)
                    .build();
            FileOutputStream writer = null;
            Uri contentUri = null;
            try {
                writer = new FileOutputStream(sendingFile);
                writer.write(new PduComposer(this, curReq).make());
                contentUri = writerUri;

            } catch (final IOException e) {
                com.klinker.android.logger.Log.e(TAG, "Error writing send file", e);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                    }
                }
            }

            Bundle configOverrides = new Bundle();
            configOverrides.putBoolean(SmsManager.MMS_CONFIG_GROUP_MMS_ENABLED, true);
            if (contentUri != null) {
                Log.d("AlarmReceiver", "ContentURI found to be :" + contentUri.toString());
                Log.d("AlarmReceiver", "File path: " + new File(contentUri.toString()).getAbsolutePath());
                /* Register to listen for MMS send action in order to notify the user on send status */
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
                        Log.d("MMSSentReceiver", "result: " + result + " resultCode: " + getResultCode());
                        sendDeliveryNotification(messageID, result, messageText);
                        unregisterReceiver(this);
                    }
                }, new IntentFilter(MMS_SENT_ACTION));


                SmsManager.getDefault().sendMultimediaMessage(this,
                        contentUri, null, configOverrides, pendingIntent);
            } else {
                com.klinker.android.logger.Log.e(TAG, "Error writing sending Mms");
                try {
                    pendingIntent.send(SmsManager.MMS_ERROR_IO_ERROR);
                } catch (PendingIntent.CanceledException ex) {
                    com.klinker.android.logger.Log.e(TAG, "Mms pending intent cancelled?", ex);
                }
            }


        }
        catch (Exception e) {
            Log.e("Process MMS Sending", "Sending exception.", e);
        }
    }


    /* Code copied and modified slightly from Jacob Klinker */
    private static SendReq buildPdu(Context context, String[] recipients, String subject,
                                    List<MMSPart> parts) {
        final SendReq req = new SendReq();
        // From, per spec
        final String lineNumber = Utils.getMyPhoneNumber(context);
        if (!TextUtils.isEmpty(lineNumber)) {
            req.setFrom(new EncodedStringValue(lineNumber));
        }
        // To
        for (String recipient : recipients) {
            req.addTo(new EncodedStringValue(recipient));
        }
        // Subject
        if (!TextUtils.isEmpty(subject)) {
            req.setSubject(new EncodedStringValue(subject));
        }
        // Date
        req.setDate(System.currentTimeMillis() / 1000);
        // Body
        PduBody body = new PduBody();
        // Add text part. Always add a smil part for compatibility, without it there
        // may be issues on some carriers/client apps
        int size = 0;
        for (int i = 0; i < parts.size(); i++) {
            MMSPart part = parts.get(i);
            size += addTextPart(body, part, i);
        }

    /*    // add a SMIL document for compatibility
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SmilXmlSerializer.serialize(SmilHelper.createSmilDocument(body), out);
        PduPart smilPart = new PduPart();
        smilPart.setContentId("smil".getBytes());
        smilPart.setContentLocation("smil.xml".getBytes());
        smilPart.setContentType(ContentType.APP_SMIL.getBytes());
        smilPart.setData(out.toByteArray());
        body.addPart(0, smilPart);
*/
        req.setBody(body);
        // Message size
        req.setMessageSize(size);
        // Message class
        req.setMessageClass(PduHeaders.MESSAGE_CLASS_PERSONAL_STR.getBytes());
        // Expiry
        req.setExpiry(Transaction.DEFAULT_EXPIRY_TIME);
        try {
            // Priority
            req.setPriority(Transaction.DEFAULT_PRIORITY);
            // Delivery report
            req.setDeliveryReport(PduHeaders.VALUE_NO);
            // Read report
            req.setReadReport(PduHeaders.VALUE_NO);
        } catch (InvalidHeaderValueException e) {}

        return req;
    }

    // Entirely Jacob Klinker
    private static int addTextPart(PduBody pb, MMSPart p, int id) {
        String filename = p.MimeType.split("/")[0] + "_" + id + ".mms";
        final PduPart part = new PduPart();
        // Set Charset if it's a text media.
        if (p.MimeType.startsWith("text")) {
            part.setCharset(CharacterSets.UTF_8);
        }
        // Set Content-Type.
        part.setContentType(p.MimeType.getBytes());
        // Set Content-Location.
        part.setContentLocation(filename.getBytes());
        int index = filename.lastIndexOf(".");
        String contentId = (index == -1) ? filename
                : filename.substring(0, index);
        part.setContentId(contentId.getBytes());
        part.setData(p.Data);
        pb.addPart(part);

        return part.getData().length;
    }



}
