package cs371m.hermes.futuremessenger;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class TextMessage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final EditText contact_field = (EditText) findViewById(R.id.contact_field);
        final EditText message_field = (EditText) findViewById(R.id.editTextMultiline);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TextMessage: ", "Message Send button pressed.");

                String phonenum = contact_field.getText().toString();
                String message = message_field.getText().toString();
                sendSMS(phonenum, message);
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
    }

    private void sendSMS(String phoneNumber, String message) {
        try {
            Log.d("sendSMS number is ", phoneNumber);
            Log.d("sendSMS", message);
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
            sms.sendTextMessage(phoneNumber, null, message, sentPI, deliverPI);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),
                    ex.getMessage().toString(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

}
