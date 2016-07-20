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
        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent){
        Toast.makeText(this, "onBind", Toast.LENGTH_LONG).show();
        return null;
    }

    //not needed
    @Override
    public void onDestroy(){
        Toast.makeText(this, "onDestroy", Toast.LENGTH_LONG).show();
        super.onDestroy();
    }
    /* Need to do this to get bundled extras, since Service won't allow getIntent.getExtras() */
    @Override
    public void onStart(Intent intent, int startId){
        super.onStart(intent, startId);

        Bundle bundle = intent.getExtras();
        phoneNum = (String) bundle.getCharSequence("num");
        message = (String) bundle.getCharSequence("message");
        Toast.makeText(this, "onStart, trying to send message", Toast.LENGTH_LONG).show();
        Toast.makeText(this, "number: "+phoneNum+"\nmessage: "+message, Toast.LENGTH_LONG).show();
        sendSMS(phoneNum, message);

    }

    @Override
    public boolean onUnbind(Intent intent){
        Toast.makeText(this, "onUnbind", Toast.LENGTH_LONG).show();
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
            Log.d("sendSMS", phoneNum);
            Log.d("sendSMS", message);

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNum, null, message, null, null);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }




}
