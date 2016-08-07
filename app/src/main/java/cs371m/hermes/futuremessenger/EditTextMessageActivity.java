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
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

public class EditTextMessageActivity extends AppCompatActivity
        implements EnterPhoneNumberDialogFragment.EnterPhoneNumberListener,
        DatePickerFragment.DatePickerListener, TimePickerFragment.TimePickerListener,
        GroupDialogFragment.GroupDialogListener {

    /**
     *
     */
    private String TAG = "EditTextMessageActivity ";

    private TextView _date_button;
    private TextView _time_button;
    private EditText _message_field;

    /**
     * _calendar holds the Date that the Buttons are displaying
     */
    private Calendar _calendar;
    /**
     * some common DateFormat objects we will use
     */
    public static final DateFormat DF_DATE     = DateFormat.getDateInstance(DateFormat.MEDIUM);
    public static final DateFormat DF_TIME     = DateFormat.getTimeInstance(DateFormat.SHORT);
    public static final DateFormat DF_DATETIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /* Are we making a brand new message?
     * If we're editing/deleting an existing message, store the ID of it here.
     * Otherwise it will be -1. */
    protected long last_clicked_message_id;

    // Request code for starting the contact picker activity
    private static final int CONTACT_PICKER_REQUEST = 9999;

    // Request code for starting the select preset activity
    private static final int POPULATE_FROM_PRESET_REQUEST = 9998;

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

        _message_field = (EditText) findViewById(R.id.message_field);
        _date_button = (TextView) findViewById(R.id.button_date).findViewById(R.id.button_date_text);
        _time_button = (TextView) findViewById(R.id.button_time).findViewById(R.id.button_time_text);
        _calendar = Calendar.getInstance();

        /* TODO: If editing scheduled message, cancel previous version first */
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            //TODO: Initialize currently selected contacts to have all the existing contacts.
            buildContactListFromExisting(intent.getStringExtra("recip_names"),
                                         intent.getStringExtra("recip_nums"));

            last_clicked_message_id = intent.getLongExtra("message_id", -1);
            _message_field.setText(intent.getStringExtra("message"));
            //TODO: Intent should send in date & time as one string
            String datetime = intent.getStringExtra("date") + " " + intent.getStringExtra("time");
            Log.d(TAG + "editing text", datetime);
            try {
                _calendar.setTime(DF_DATETIME.parse(datetime));
            } catch (ParseException e) {
                // Major error if this is run, need to do something
                // Editing a text, but the parse of the datetime fails
                Log.e(TAG + "onCreate", "Attempt to parse failed: " + datetime);
                e.printStackTrace();
            }
        }
        else {
            // brand new contacts list
            currently_selected_contacts = new ArrayList<>();
            last_clicked_message_id = -1;
        }
        updateDateButtonText();
        updateTimeButtonText();

        ListView contactsLV = (ListView) findViewById(R.id.selected_contacts_list);

        // Ensure the listview's touches won't be stopped by the scrollview
        contactsLV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        contactAdapter = new ContactListAdapter(this, currently_selected_contacts);
        contactsLV.setAdapter(contactAdapter);
        adjustListHeight(contactsLV);

        preventEditTextTouchIntercept();
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
    protected void initializeScheduleButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG + "FAButton", "Message Send button pressed.");

                if (isEntryFieldsFilled()) {
                    if (currently_selected_contacts.size() >= 2) {
                        showGroupDialog();
                    } else {
                        scheduleMessage(last_clicked_message_id, get_message_text(), null, MessengerDatabaseHelper.NOT_GROUP_MESSAGE);
                        returnToMainActivity();
                    }
                }
            }
        });
    }

    /**
     * checks to see if entry fields were filled, and makes a Toast if something is not
     * also checks to see if the Date is set in the future
     * @return false if a field is missing or incorrect
     */
    protected boolean isEntryFieldsFilled() {
        boolean result = false;
        if (isNoContactEntered()) {
            Toast.makeText(getApplicationContext(),
                    R.string.no_contacts_entered, Toast.LENGTH_SHORT).show();
        } else if (isNoMessageEntered()) {
            Toast.makeText(getApplicationContext(),
                    R.string.no_message_entered, Toast.LENGTH_SHORT).show();
        } else if (isDateInPast()) {
            Toast.makeText(getApplicationContext(),
                    R.string.bad_date_entered, Toast.LENGTH_SHORT).show();
        } else {
            result = true;
        }
        return result;
    }

    protected final String getNumbersFromContactsSelected() {
        ArrayList<String> numbers = new ArrayList<String>();
        for (Contact thisContact : currently_selected_contacts) {
            numbers.add(thisContact.getPhoneNum());
        }
        return stringJoin(numbers, ";");
    }


    // Builds the selected contacts list from given names and numbers delimited by strings.
    private void buildContactListFromExisting(String recip_names, String recip_nums) {
        Log.d(TAG + "Build contact string", "Names: " + recip_names);
        Log.d(TAG + "Build contact string", "Numbers: " + recip_nums);

        String[] name_array = recip_names.split(";");
        String[] num_array = recip_nums.split(";");
        currently_selected_contacts = new ArrayList<>();
        if (name_array.length != num_array.length) {
            Log.d(TAG + "BuildContactsList", "Lengths of names and numbers are not equal.");
        }
        else {
            for (int i = 0; i < name_array.length; i++) {
                Contact new_contact = new Contact(name_array[i], num_array[i]);
                currently_selected_contacts.add(new_contact);
            }
        }
    }

    // The entire activity is in a ScrollView, so it intercepts other scrollable items.
    // This enables the message edittext to be scrolled.
    private void preventEditTextTouchIntercept() {
        EditText textContentInput = (EditText) findViewById(R.id.message_field);
        textContentInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
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
                case POPULATE_FROM_PRESET_REQUEST:
                    populateTextFieldFromPreset(data);
            }
        }
        else {
            Log.w(TAG + "CONTACT PICKER RESULT", "NOT OK");
        }
    }

    // Update the selected contacts list after a user selects a contact.
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
            Toast.makeText(this, R.string.edit_error, Toast.LENGTH_SHORT).show();
        else {
            Contact current_contact = new Contact(name, phoneNumber);
            addContactToRecipientList(current_contact);
        }
    }

    // Adds a new recipient to the recipient list. Ensures no duplicates are added.
    private void addContactToRecipientList (Contact new_contact) {
        ListView contactsLV = (ListView) findViewById(R.id.selected_contacts_list);
        // Only add this new contact if we haven't already added it.
        if (!currently_selected_contacts.contains(new_contact)) {
            currently_selected_contacts.add(new_contact);
            adjustListHeight(contactsLV);
            contactAdapter.notifyDataSetChanged();
            // Make sure the most recently added item is in view by scrolling to the bottom.
            contactsLV.setSelection(contactAdapter.getCount() - 1);
        }
        else {
            Toast.makeText(this, R.string.already_recipient, Toast.LENGTH_SHORT).show();
        }
    }

    /* If the size of the list is now greater than 3, restrict the ListView height
       This solution was found on:
       http://stackoverflow.com/questions/5487552/limit-height-of-listview-on-android
       http://stackoverflow.com/questions/14020859/change-height-of-a-listview-dynamicallyandroid */
    private void adjustListHeight(ListView contactsLV) {
        LinearLayout.LayoutParams list = (LinearLayout.LayoutParams) contactsLV.getLayoutParams();
        int numRows = contactAdapter.getCount();
        if (numRows > 3) {
            View item = contactAdapter.getView(0, null, contactsLV);
            item.measure(0,0);
            list.height = (int) (3.5 * item.getMeasuredHeight());
        }
        else {
            int sumHeight = 0;
            for (int i = 0; i < numRows; i++) {
                View item = contactAdapter.getView(i, null, contactsLV);
                item.measure(0, 0);
                sumHeight += item.getMeasuredHeight();
            }
            list.height = sumHeight;
        }
        contactsLV.setLayoutParams(list);
    }

    @Override
    public void onFinishEnterPhoneNum(String phoneNum) {
        addPhoneNumToRecipientList(phoneNum);
    }

    private void addPhoneNumToRecipientList(String phoneNum) {
        Contact new_contact = new Contact(" ", phoneNum);
        addContactToRecipientList(new_contact);
    }

    /**
     * set an alarm for when to send the message
     * @param id the id in the database with the message and numbers to send
     * @param when set to when the alarm is set
     */
    protected void setAlarm(long id, Calendar when) {
        //TODO: getContext() instead of this?
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        Log.d(TAG + "setAlarm", this.getClass().toString());
        Bundle bundle = new Bundle();
        bundle.putLong("message_id", id);
        alarmIntent.putExtras(bundle);

        PendingIntent pendingIntent = PendingIntent.getService(this,
                (int) id, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(), pendingIntent);
        Log.d(TAG + "setAlarm", "Message id = " + Long.toString(id));
    }

    /**
     * store the message in the database and set an alarm to send the message
     * @param id         database id of message; -1 if new message
     * @param message    message to be sent
     * @param image_path file path for an image
     * @param group_flag 0 == group message, 1 == individual, ??? == neither
     */
    protected void scheduleMessage(long id, String message, String image_path, int group_flag) {
        Log.d(TAG + "scheduleMsg", "scheduling message");
        long ret_database_id;
        if (id == -1) {
            ret_database_id = saveMessage(message, image_path, group_flag);
        } else {
            ret_database_id = updateMessage(message, image_path, group_flag);
        }
        Log.d(TAG + "scheduleMsg", "id: " + id);
        Log.d(TAG + "scheduleMsg", "numbers: " + getNumbersFromContactsSelected());
        Log.d(TAG + "scheduleMsg", "message: " + message);
        Log.d(TAG + "scheduleMsg", "image_path: " + image_path);
        Log.d(TAG + "scheduleMsg", "group_flag: " + group_flag);
        setAlarm(ret_database_id, _calendar);
    }

    /**
     * Save a new message into the database
     * @param message       the message to send
     * @param image_path    an image to send
     * @return the ID of the save message
     */
    protected long saveMessage(String message, String image_path, int group_flag) {
        long result = -1;
        try {
            Log.d(TAG + "saveSMS", message);

            //Save the message
            String dateTime = getDateTime();
            MessengerDatabaseHelper mDb = new MessengerDatabaseHelper(EditTextMessageActivity.this);
            result = mDb.storeNewMessage(currently_selected_contacts, dateTime, message, image_path,
                                         group_flag);
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
    private long updateMessage(String message, String image_path, int group_flag) {
        //cancel the previous alarm
        cancelAlarm(last_clicked_message_id);

        //Save the message
        String dateTime = getDateTime();
        MessengerDatabaseHelper mDb = new MessengerDatabaseHelper(EditTextMessageActivity.this);
        long result = mDb.updateExistingMessage(last_clicked_message_id, currently_selected_contacts,
                                            dateTime, message, image_path, group_flag);
        mDb.close();
        return result;
    }

    // Launch the preset selection activity
    public void launchPresetSelection(View v) {
        Intent intent = new Intent(this, SelectPresetActivity.class);
        startActivityForResult(intent, POPULATE_FROM_PRESET_REQUEST);
    }

    private void populateTextFieldFromPreset(Intent data) {
        String preset_content = data.getStringExtra("preset_content");
        EditText textField = (EditText) findViewById(R.id.message_field);
        textField.append(preset_content);
    }

    /**
     * cancel a scheduled message alarm
     * @param id the database id to cancel
     */
    private void cancelAlarm(long id){
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(),
                (int) id, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Log.d(TAG +"cancelAlarm", "Old alarm canceled");
    }

    private String getDateTime() {
        return DF_DATETIME.format(_calendar.getTime());
    }

    /**
     * Because this activity was started for a result, return to
     * the MainActivity and send it an "OK" result code.
     */
    protected void returnToMainActivity() {
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

    protected int showGroupDialog () {
        DialogFragment newFragment = new GroupDialogFragment();
        newFragment.show(getFragmentManager(), "groupDialog");
        return 0;
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
    }

    @Override
    public void onGroupSelected(int i) {
        Log.d(TAG + "GroupSelect", Integer.toString(i));
        // 0 == Group, 1 == Individual

        String message = get_message_text();
        scheduleMessage(last_clicked_message_id, message, null, (i==1)?MessengerDatabaseHelper.NOT_GROUP_MESSAGE:MessengerDatabaseHelper.IS_GROUP_MESSAGE);
        returnToMainActivity();
    }

    protected void updateTimeButtonText() {
        _time_button.setText(DF_TIME.format(_calendar.getTime()).toUpperCase());
    }

    protected void updateDateButtonText() {
        _date_button.setText(DF_DATE.format(_calendar.getTime()).toUpperCase());
    }

    protected String get_message_text() {
        return _message_field.getText().toString();
    }

    protected boolean isNoContactEntered() {
        return currently_selected_contacts.isEmpty();
    }

    protected boolean isNoMessageEntered() {
        String msg = get_message_text();
        return msg.equals("");
    }

    protected final boolean isDateInPast() {
        Calendar now = Calendar.getInstance();
        return _calendar.before(now);
    }

    public static String stringJoin(Collection<String> words, String delim) {
        if (words.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (String word : words) {
            result.append(delim).append(word);
        }
        return result.substring(1);
    }
}
