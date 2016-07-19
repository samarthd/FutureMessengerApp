package cs371m.hermes.futuremessenger;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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

public class EditTextMessageActivity extends AppCompatActivity {

    private EditText _contact_field;
    private EditText _message_field;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        _contact_field = (EditText) findViewById(R.id.recipients_field);
        _message_field = (EditText) findViewById(R.id.message_field);

        /* TODO: Get data from main screen, if editing scheduled message */
        Intent intent = getIntent();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FabButton", "Message Send button pressed.");

                /* TODO: add in sending of dates and time */
                String phonenum = _contact_field.getText().toString();
                String message = _message_field.getText().toString();
                saveSMS(phonenum, null, null, message);

                returnToMainActivity();
            }
        });
    }

    /**
     * DO NOT USE: actually send the message
     * Only kept as reference code for later
     * @param phoneNum
     * @param message
     */
    private void sendSMS(String phoneNum, String message) {
        try {
            Log.d("sendSMS", phoneNum);
            Log.d("sendSMS", message);

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNum, null, message, null, null);

        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),
                    ex.getMessage(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    /**
     * Save the message into the database
     * @param phoneNum number to send message to
     * @param date date to send message on
     * @param time time to send the message on
     * @param message the message to send
     */
    private void saveSMS(String phoneNum, String date, String time, String message) {
        /* TODO: implement saving of message
         * TODO: determine if message wants to be group, or individual
         * TODO: figure out how to send to multiple contacts
         */
        try {
            Log.d("saveSMS", phoneNum);
            Log.d("saveSMS", message);
//            SmsManager sms = SmsManager.getDefault();
//            sms.sendTextMessage(phoneNum, null, message, null, null);

            //Save the message
            String[] phoneNumbers = new String[] {phoneNum};
            String dateTime = date + time;
            MessengerDatabaseHelper mDb = new MessengerDatabaseHelper(EditTextMessageActivity.this);
            mDb.storeNewSMS(phoneNumbers, dateTime, message);
            mDb.close();

            
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),
                    ex.getMessage(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    /**
     * returns to Main screen
     */
    private void returnToMainActivity() {
        //TODO: Determine if data needs to be sent back to the main screen
        Intent ret = new Intent(this, MainActivity.class);
        startActivity(ret);
    }

}
