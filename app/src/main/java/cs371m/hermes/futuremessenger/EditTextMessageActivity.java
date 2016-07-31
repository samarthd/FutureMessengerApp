package cs371m.hermes.futuremessenger;

import android.app.DialogFragment;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EditTextMessageActivity extends AppCompatActivity {

    private EditText _contact_field;
    private Button _date_button;
    private Button _time_button;
    private EditText _message_field;

    /* These variables hold the scheduling information that is
     * given to the button/taken from the button. */
    private int _hour = 0;
    private int _minute = 0;
    private int _year = 0;
    // January = 0, Februrary = 1, ...
    private int _month = 0;
    private int _dayOfMonth = 0;

    /* Are we making a brand new message?
     * If we're editing/deleting an existing message, store the ID of it here.
     * Otherwise it will be -1. */
    private long last_clicked_message_id;

    // Request code for starting the contact picker activity
    private static final int CONTACT_PICKER_REQUEST = 9999;

    // List that holds the currently selected contacts
    private ArrayList<Contact> currently_selected_contacts;

    // Adapter to populated currently selected contacts list
    private ContactListAdapter contactAdapter;

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

            //TODO: Initialize currently selected contacts to have all the existing contacts.

            last_clicked_message_id = intent.getLongExtra("message_id", -1);
            _contact_field.setText(intent.getStringExtra("num"));
            _message_field.setText(intent.getStringExtra("message"));
            String[] timeParsed = intent.getStringExtra("time").split(":");
            String[] dateParsed = intent.getStringExtra("date").split("-");
            setTimeButton(Integer.parseInt(timeParsed[0]), Integer.parseInt(timeParsed[1]));
            setDateButton(Integer.parseInt(dateParsed[0]), Integer.parseInt(dateParsed[1]) - 1, Integer.parseInt(dateParsed[2]));
        }
        else {

            // brand new contacts list
            currently_selected_contacts = new ArrayList<>();
            contactAdapter = new ContactListAdapter(this, currently_selected_contacts);
            ListView contactsLV = (ListView) findViewById(R.id.selected_contacts_list);
            contactsLV.setAdapter(contactAdapter);

            last_clicked_message_id = -1;
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


        // When the contact button is clicked, launch the contact picker.
        Button choose_contact = (Button) findViewById(R.id.choose_contact_button);
        choose_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent contactPickerIntent =
                        new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                contactPickerIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(contactPickerIntent, CONTACT_PICKER_REQUEST);

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FabButton", "Message Send button pressed.");

                /* TODO: add in sending of dates and time */
                String phonenum = _contact_field.getText().toString();
                String message = _message_field.getText().toString();
                long id;
                if (last_clicked_message_id == -1) {
                    id = saveSMS(phonenum, null, null, message);
                }
                else {
                    id = updateSMS(phonenum, message);
                }
                setAlarm(id, phonenum, message, _year, _month, _dayOfMonth, _hour, _minute);

                returnToMainActivity();
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CONTACT_PICKER_REQUEST:
                    updateSelectedContactsList(data);
                    break;
            }
        }
        else {
            Log.w("CONTACT PICKER RESULT", "NOT OK");
        }
    }

    // Update the selected contacts list
    private void updateSelectedContactsList(Intent data) {

        boolean showErrorToast = false;
        Uri contact_uri = data.getData();


        // Get the contact's name.
        Cursor cursor = getContentResolver()
                .query(contact_uri, null,
                null, null, null);

        String name = "";

        if (cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            name = cursor.getString(nameIndex);
        }
        else {
            showErrorToast = true;
        }

        // Get the contact's phone number.
        String phoneNumber = "";
        String contact_ID = contact_uri.getLastPathSegment();

        Cursor num_cursor = getContentResolver()
                            .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                   ContactsContract.CommonDataKinds.Phone._ID + "=?" ,
                                   new String[] { contact_ID }, null);
        if (num_cursor.moveToFirst()){
            int phoneIndex = num_cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);
            phoneNumber = num_cursor.getString(phoneIndex);
        }
        else {
            showErrorToast = true;
        }

        if (showErrorToast)
            Toast.makeText(this, "Something went wrong with that contact.", Toast.LENGTH_SHORT).show();
        else {
            Contact current_contact = new Contact(name, phoneNumber);
            // Only add this new contact if we haven't already added it.
            if (!currently_selected_contacts.contains(current_contact)) {
                currently_selected_contacts.add(current_contact);
                contactAdapter.notifyDataSetChanged();
            }
            else {
                Toast.makeText(this, "That contact is already a recipient!", Toast.LENGTH_SHORT).show();
            }
        }


    }

    /**
     * Create Alarm
     * @param phoneNum
     * @param message
     */
    private void setAlarm(long id, String phoneNum, String message, int year, int month, int day, int hour, int minute){
        /* Set the alarm with the selected parameters */
        Intent alarmIntent = new Intent(EditTextMessageActivity.this, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putLong("message_id", id);
        bundle.putCharSequence("num", phoneNum);
        bundle.putCharSequence("message", message);
        alarmIntent.putExtras(bundle);

        PendingIntent pendingIntent = PendingIntent.getService(EditTextMessageActivity.this,
                                      (int) id, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        /* Set calendar dates */
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.clear();
        calendar.set(year, month, day, hour, minute);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Log.d("setAlarm", "Message id = " + Long.toString(id));

        Toast.makeText(EditTextMessageActivity.this, "Saved your message!", Toast.LENGTH_SHORT).show();
    }


    /**
     * Save the new message into the database
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
        long result = mDb.updateTextMessage(last_clicked_message_id, phoneNumbers, dateTime, message);
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
     * Because this activity was started for a result, return to
     * the MainActivity and send it an "OK" result code.
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
