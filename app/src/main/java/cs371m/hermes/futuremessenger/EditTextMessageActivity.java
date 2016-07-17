package cs371m.hermes.futuremessenger;

import android.content.Intent;
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

        Intent intent = getIntent();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FabButton", "Message Send button pressed.");

                String phonenum = _contact_field.getText().toString();
                String message = _message_field.getText().toString();
                saveSMS(phonenum, null, null, message);

                returnToMainActivity();
            }
        });
    }

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

    private void saveSMS(String phoneNum, String date, String time, String message) {
        //TODO: implement saving of message
        try {
            Log.d("saveSMS", phoneNum);
            Log.d("saveSMS", message);
//            SmsManager sms = SmsManager.getDefault();
//            sms.sendTextMessage(phoneNum, null, message, null, null);

        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),
                    ex.getMessage(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    private void returnToMainActivity() {
        Intent ret = new Intent(this, MainActivity.class);
        startActivity(ret);
    }

}
