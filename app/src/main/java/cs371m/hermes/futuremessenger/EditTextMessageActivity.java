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
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class EditTextMessageActivity extends AppCompatActivity
        implements EnterPhoneNumberDialogFragment.EnterPhoneNumberListener, DatePickerFragment.DatePickerListener, TimePickerFragment.TimePickerListener {

    private EditText _phonenum_field;
    private Button _date_button;
    private Button _time_button;
    private EditText _message_field;

    /**
     * _calendar holds the Date that the Buttons are displaying
     */
    Calendar _calendar;
    /**
     * some common DateFormat objects we will use
     */
    private final DateFormat DF_DATE     = DateFormat.getDateInstance(DateFormat.MEDIUM);
    private final DateFormat DF_TIME     = DateFormat.getTimeInstance(DateFormat.SHORT);
    private final DateFormat DF_DATETIME = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

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

        _phonenum_field = (EditText) findViewById(R.id.recipients_field);
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
            _phonenum_field.setText(intent.getStringExtra("num"));
            _message_field.setText(intent.getStringExtra("message"));
            //TODO: Intent should send in date & time as one string
            String datetime = intent.getStringExtra("date") + " " + intent.getStringExtra("time");
            Log.d("editing text", datetime);
            try {
                _calendar.setTime(DF_DATETIME.parse(datetime));
            } catch (ParseException e) {
                //TODO: Major error if this is run, need to do something
                Log.e("onCreate", "Attempt to parse failed: " + datetime);
                e.printStackTrace();
            }
        }
        else {
            // brand new contacts list
            currently_selected_contacts = new ArrayList<>();
            contactAdapter = new ContactListAdapter(this, currently_selected_contacts);
            ListView contactsLV = (ListView) findViewById(R.id.selected_contacts_list);
            contactsLV.setAdapter(contactAdapter);

            last_clicked_message_id = -1;
            _calendar = Calendar.getInstance();
            updateDateButtonText();
            updateTimeButtonText();
        }

        initializeContactChooserButton();
        initializePhoneNumberButton();
        initializeScheduleButton();

    }

    // When the contact button is clicked, launch the contact picker.
    private void initializeContactChooserButton() {
        CardView choose_contact = (CardView) findViewById(R.id.choose_contact_button);
        choose_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent contactPickerIntent =
                        new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                contactPickerIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(contactPickerIntent, CONTACT_PICKER_REQUEST);
            }
        });
    }

    // When the phone number button is clicked, launch the phone number input fragment.
    private void initializePhoneNumberButton() {
        CardView add_number = (CardView) findViewById(R.id.enter_number_button);
        add_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnterPhoneNumberDialogFragment enterNumFragment =
                        new EnterPhoneNumberDialogFragment();
                enterNumFragment.show(getFragmentManager(), "Enter Phone Number");
            }
        });
    }

    // Initialize the schedule button.
    private void initializeScheduleButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FabButton", "Message Send button pressed.");

                /* TODO: add in sending of dates and time */
                String phonenum = _phonenum_field.getText().toString();
                String message = _message_field.getText().toString();
                long id;
                if (last_clicked_message_id == -1) {
                    id = saveSMS(phonenum, null, null, message);
                }
                else {
                    id = updateSMS(phonenum, message);
                }

                int year = _calendar.get(Calendar.YEAR);
                int month = _calendar.get(Calendar.MONTH);
                int day = _calendar.get(Calendar.DAY_OF_MONTH);
                int hour = _calendar.get(Calendar.HOUR_OF_DAY);
                int minute = _calendar.get(Calendar.MINUTE);
                //TODO: Change header?
                setAlarm(id, phonenum, message, year, month, day, hour, minute);

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
                    receiveContactAndAddToList(data);
                    break;
            }
        }
        else {
            Log.w("CONTACT PICKER RESULT", "NOT OK");
        }
    }

    // Update the selected contacts list
    private void receiveContactAndAddToList(Intent data) {

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
            phoneNumber = PhoneNumberUtils.formatNumber(phoneNumber);
        }
        else {
            showErrorToast = true;
        }


        if (showErrorToast)
            Toast.makeText(this, "Something went wrong with that contact.", Toast.LENGTH_SHORT).show();
        else {
            Contact current_contact = new Contact(name, phoneNumber);
            addContactToRecipientList(current_contact);
        }
    }

    // Adds a new contact to the recipient list. Ensures no duplicates are added.
    private void addContactToRecipientList (Contact new_contact) {
        ListView contactsLV = (ListView) findViewById(R.id.selected_contacts_list);
        // Only add this new contact if we haven't already added it.
        if (!currently_selected_contacts.contains(new_contact)) {
            currently_selected_contacts.add(new_contact);
                /* If the size of the list is now greater than 3, restrict the ListView height
                   This solution was found on:
                   http://stackoverflow.com/questions/5487552/limit-height-of-listview-on-android
                   http://stackoverflow.com/questions/14020859/change-height-of-a-listview-dynamicallyandroid */
            if (currently_selected_contacts.size() > 3) {
                RelativeLayout.LayoutParams list = (RelativeLayout.LayoutParams) contactsLV.getLayoutParams();
                View item = contactAdapter.getView(0, null, contactsLV);
                item.measure(0,0);
                list.height = (int) (3.5 * item.getMeasuredHeight());
                contactsLV.setLayoutParams(list);
            }
            contactAdapter.notifyDataSetChanged();
            // Make sure the most recently added item is in view by scrolling to the bottom.
            contactsLV.setSelection(contactAdapter.getCount() - 1);
        }
        else {
            Toast.makeText(this, R.string.already_recipient, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFinishEnterPhoneNum(String phoneNum) {
        addPhoneNumToRecipientList(phoneNum);
    }

    private void addPhoneNumToRecipientList(String phoneNum) {
        Contact new_contact = new Contact("", phoneNum);
        addContactToRecipientList(new_contact);
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
         * TODO: save numbers as "5554;5556;5558;..."
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

    //TODO: Remove (if not used often), or change name to be more descriptive of what it does
    private String getDateTimeFromButtons() {
        return DF_DATETIME.format(_calendar.getTime());
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
        int hr = _calendar.get(Calendar.HOUR_OF_DAY);
        int min = _calendar.get(Calendar.MINUTE);

        DialogFragment newFragment = TimePickerFragment.newInstance(hr, min);
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog (View v) {
        int y = _calendar.get(Calendar.YEAR);
        int m = _calendar.get(Calendar.MONTH);
        int d = _calendar.get(Calendar.DAY_OF_MONTH);
        DialogFragment newFragment = DatePickerFragment.newInstance(y, m, d);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    /**
     * Method called by DatePickerDialogFragment, once user has selected the date
     * @param year the year selected
     * @param month the month selected
     * @param dayOfMonth the day of month selected
     */
    @Override
    public void onDateSelected(int year, int month, int dayOfMonth) {
        _calendar.set(Calendar.YEAR, year);
        _calendar.set(Calendar.MONTH, month);
        _calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateDateButtonText();
    }

    /**
     * Method called by TimePickerDialogFragment, once user has selected the time
     * @param hour the hour selected
     * @param minute the minute selected
     */
    @Override
    public void onTimeSelected (int hour, int minute) {
        _calendar.set(Calendar.HOUR_OF_DAY, hour);
        _calendar.set(Calendar.MINUTE, minute);
        updateTimeButtonText();
        // setTimeButton(hour, minute);
    }

    private void updateTimeButtonText() {
        _time_button.setText(DF_TIME.format(_calendar.getTime()));
    }

    private void updateDateButtonText() {
        _date_button.setText(DF_DATE.format(_calendar.getTime()));
    }

}
