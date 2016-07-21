package cs371m.hermes.futuremessenger;

import android.app.DialogFragment;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

public class EditTextMessageActivity extends AppCompatActivity {

    private EditText _contact_field;
    private Button _date_button;
    private Button _time_button;
    private EditText _message_field;
    private PendingIntent pendingIntent;

    private int _hour = 0;
    private int _minute = 0;
    private int _year = 0;
    // January = 0, Februrary = 1, ...
    private int _month = 0;
    private int _dayOfMonth = 0;

    // Are we making a brand new message?
    // If we're editing an existing message, store the ID of it here. Otherwise it will be -1.
    private long id_of_message_to_edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        _contact_field = (EditText) findViewById(R.id.recipients_field);
        _message_field = (EditText) findViewById(R.id.message_field);
        _date_button = (Button) findViewById(R.id.button_date);
        _time_button = (Button) findViewById(R.id.button_time);

        /* TODO: If editing scheduled message, cancel previous version first */
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        // If these extras aren't null, then we're editing an existing message.
        if (extras != null) {
            id_of_message_to_edit = intent.getLongExtra("message_id", -1);
            _contact_field.setText(intent.getStringExtra("num"));
            // _date_button.setText(intent.getStringExtra("date"));
            // _time_button.setText(intent.getStringExtra("time"));
            _message_field.setText(intent.getStringExtra("message"));
            String[] timeParsed = intent.getStringExtra("time").split(":");
            String[] dateParsed = intent.getStringExtra("date").split("-");
            setTimeButton(Integer.parseInt(timeParsed[0]), Integer.parseInt(timeParsed[1]));
            setDateButton(Integer.parseInt(dateParsed[0]), Integer.parseInt(dateParsed[1]) - 1, Integer.parseInt(dateParsed[2]));
        }
        else {
            id_of_message_to_edit = -1;
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            setDateButton(year, month, day);
            Log.d("onCreate", "Today's month is " + Integer.toString(month));
            setTimeButton(hour, minute);
        }



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FabButton", "Message Send button pressed.");

                /* TODO: add in sending of dates and time */
                String phonenum = _contact_field.getText().toString();
                String message = _message_field.getText().toString();
                if (id_of_message_to_edit == -1) {
                    saveSMS(phonenum, null, null, message);
                }
                else {
                    updateSMS(phonenum, message);
                }
                setAlarm(phonenum, message, _year, _month, _dayOfMonth, _hour, _minute);

                returnToMainActivity();
            }
        });
    }

    /**
     * Create Alarm
     * NEED TO ADD DATE AND TIME PARAMETERS
     * @param phoneNum
     * @param message
     */
    private void setAlarm(String phoneNum, String message, int year, int month, int day, int hour, int minute){
        /* Set the alarm with the selected parameters */
        Intent alarmIntent = new Intent(EditTextMessageActivity.this, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putCharSequence("num", phoneNum);
        bundle.putCharSequence("message", message);
        alarmIntent.putExtras(bundle);

        pendingIntent = PendingIntent.getService(EditTextMessageActivity.this, 0, alarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        //String date = _date_button.getText().toString();


        /* Set calendar dates */
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.clear();
        calendar.set(year, month, day, hour, minute);
        //calendar.set(2016, Calendar.JULY, 20, 4, 20);
        //calendar.add(Calendar.SECOND, 10);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Log.d("setAlarm", calendar.toString());

        //Toast.makeText(EditTextMessageActivity.this, "Date string: " + , Toast.LENGTH_LONG).show();
        Toast.makeText(EditTextMessageActivity.this, "Start Alarm", Toast.LENGTH_LONG).show();
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
     *
     * Returns the ID of the saved message.
     */
    private long saveSMS(String phoneNum, String date, String time, String message) {
        /* TODO: determine if message wants to be group, or individual
         * TODO: figure out how to send to multiple contacts
         */
        long result = -1;
        try {
            Log.d("saveSMS", phoneNum);
            Log.d("saveSMS", message);
//            SmsManager sms = SmsManager.getDefault();
//            sms.sendTextMessage(phoneNum, null, message, null, null);

            //Save the message
            String[] phoneNumbers = new String[] {phoneNum};
            String dateTime = getDateTimeFromButtons();
            MessengerDatabaseHelper mDb = new MessengerDatabaseHelper(EditTextMessageActivity.this);
            result = mDb.storeNewSMS(phoneNumbers, dateTime, message);
            mDb.close();

        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),
                    ex.getMessage(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
        return result;
    }

    // Delete the existing copy of the user-chosen message, and return the ID of the
    // new, updated version.
    private long updateSMS(String phoneNum, String message) {
        //TODO: Add code to cancel the existing alarm of the message..
        Log.d("updateSMS", phoneNum);
        Log.d("updateSMS", message);

        //Save the message
        String dateTime = getDateTimeFromButtons();
        String[] phoneNumbers = new String[] {phoneNum};
        MessengerDatabaseHelper mDb = new MessengerDatabaseHelper(EditTextMessageActivity.this);
        long result = mDb.updateTextMessage(id_of_message_to_edit, phoneNumbers, dateTime, message);
        mDb.close();
        return result;
    }

    private String getDateTimeFromButtons() {
        String iso_date = _date_button.getText().toString();
        String iso_time = (_hour < 10 ?"0":"") + Integer.toString(_hour)
                + (_minute < 10 ?":0":":") + Integer.toString(_minute)
                + ":00";
        Log.d("getDateTimeFromButtons", iso_date + " " + iso_time);
        return iso_date + " " + iso_time;
    }

    /**
     * returns to Main screen
     */
    private void returnToMainActivity() {
        Intent ret = new Intent(this, MainActivity.class);
        setResult(MainActivity.RESULT_OK, ret);
        finish();
    }

    public void showTimePickerDialog (View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog (View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void setTimeButton(int h, int m) {
        _hour = h;
        _minute = m;
        _time_button.setText(buildTimeString(h, m));
    }

    public void setDateButton(int y, int m, int d) {
        _year = y;
        _month = m;
        _dayOfMonth = d;
        _date_button.setText(buildDateString(y, m, d));
    }

    public int get_hour() { return _hour; }

    public int get_minute() { return _minute; }

    public int get_year() { return _year; }

    public int get_month() { return _month; }

    public int get_dayOfMonth() { return _dayOfMonth; };

    private String buildDateString (int y, int m, int d) {
        String date = Integer.toString(y);
        date = date + (m + 1 < 10?"-0":"-") + Integer.toString(m + 1);
        date = date + (d < 10?"-0":"-") + Integer.toString(d);
        return date;
    }

    private String buildTimeString (int h, int m) {
        String time = (h % 12 == 0? "12" : Integer.toString(h%12) ) + ":"
                + (m < 10 ? "0" : "") + Integer.toString(m)
                + (h < 12 ? " AM":" PM");
        return time;
    }

}
