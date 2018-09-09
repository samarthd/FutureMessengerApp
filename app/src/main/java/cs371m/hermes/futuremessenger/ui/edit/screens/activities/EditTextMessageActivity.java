package cs371m.hermes.futuremessenger.ui.edit.screens.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.arch.persistence.room.InvalidationTracker;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.transition.ChangeBounds;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.pojo.MessageWithRecipients;
import cs371m.hermes.futuremessenger.support.MessageDetailsViewBindingSupport;
import cs371m.hermes.futuremessenger.tasks.CloseEditActivityIfScheduledMessageInvalidated;
import cs371m.hermes.futuremessenger.tasks.SaveAndScheduleMessage;
import cs371m.hermes.futuremessenger.ui.edit.screens.dialogs.ExitConfirmationDialog;
import cs371m.hermes.futuremessenger.ui.edit.screens.dialogs.NewRecipientDialogFragment;
import cs371m.hermes.futuremessenger.ui.edit.support.adapters.RecipientAdapter;

import static cs371m.hermes.futuremessenger.persistence.pojo.MessageWithRecipients.BUNDLE_KEY_MESSAGE_WITH_RECIPIENTS;

public class EditTextMessageActivity extends AppCompatActivity implements
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener,
        NewRecipientDialogFragment.NewRecipientInfoSaveListener,
        RecipientAdapter.RecipientRemoveListener {

    private Toolbar mToolbar;

    private FloatingActionButton mFloatingActionButton;

    private static final String TAG = EditTextMessageActivity.class.getName();

    private static final int REQUEST_CODE_PICK_CONTACT = 0;

    private MessageWithRecipients mMessageWithRecipients;

    private RecipientAdapter mRecipientAdapter;

    private AppDatabase mDb;
    // need this to listen for this message being deleted/sent while the activity is open
    private InvalidationTracker mTableChangeTracker;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getMessageContentFromInputAndSetValue();
        outState.putSerializable(MessageWithRecipients.BUNDLE_KEY_MESSAGE_WITH_RECIPIENTS,
                                 mMessageWithRecipients);
    }

    private void getMessageContentFromInputAndSetValue() {
        EditText messageContentInput = findViewById(R.id.message_content_edittext);
        mMessageWithRecipients.getMessage()
                .setTextContent(messageContentInput.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState() restoring data");

        MessageWithRecipients savedInstanceMessageWithRecipients =
                (MessageWithRecipients) savedInstanceState
                        .getSerializable(BUNDLE_KEY_MESSAGE_WITH_RECIPIENTS);
        mMessageWithRecipients = savedInstanceMessageWithRecipients;
        updateViewsFromData();
        setUpAllButtons();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_text_message);

        mToolbar = findViewById(R.id.edit_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setUpRecipientsRecyclerView();

        initializeDataAndUpdateViews(savedInstanceState);

        //TODO remove this tracker in an onstop or ondestroy
        setUpInvalidationTracker();
    }

    /*
      We want to make sure if this message gets sent/deleted while the activity is open,
      we close the activity and prevent the user from interacting with a now-invalid message.
     */
    private void setUpInvalidationTracker() {
        mDb = AppDatabase.getInstance(this);
        String[] tablesToTrack = {"messages"};
        mTableChangeTracker = mDb.getInvalidationTracker();
        AppCompatActivity currentActivity = this;
        mTableChangeTracker.addObserver(new InvalidationTracker.Observer(tablesToTrack) {
            /**
             * When the table is invalidated, close the activity.
             * @param tables the tables that were invalidated
             */
            @Override
            public void onInvalidated(@NonNull Set<String> tables) {
                Log.d(this.getClass().getName(), "Tables invalidated: " + tables.toString());
                CloseEditActivityIfScheduledMessageInvalidated checkIfMessageInvalidatedTask =
                        new CloseEditActivityIfScheduledMessageInvalidated();
                checkIfMessageInvalidatedTask.setArguments(mDb,
                        mMessageWithRecipients.getMessage().getId(),
                        currentActivity);
                checkIfMessageInvalidatedTask.execute();
            }
        });
    }

    private void initializeDataAndUpdateViews(Bundle savedInstanceState) {
        // if the saved instance state is not null, then onRestoreInstanceState() will be called anyway,
        // so don't bother restoring state in here
        if (savedInstanceState == null) {
            // case where user launched this activity to edit an existing message
            if (getIntent().getExtras().containsKey(BUNDLE_KEY_MESSAGE_WITH_RECIPIENTS)) {
                Log.d(TAG, "Using intent extras to load data");
                mMessageWithRecipients = (MessageWithRecipients) getIntent().getExtras()
                        .getSerializable(BUNDLE_KEY_MESSAGE_WITH_RECIPIENTS);
            }
            // case where user launched this activity to create a new message
            else {
                Log.d(TAG, "No saved instance state or intent extras, so initializing new");
                // no initial values, so initialize new values
                mMessageWithRecipients = new MessageWithRecipients(new Message(), new ArrayList<>());
            }
            updateViewsFromData();
            setUpAllButtons();
        }
    }

    private void updateRecipientAdapterAndAnimateChange() {
        TransitionManager.beginDelayedTransition(findViewById(R.id.edit_scrollview), new ChangeBounds());
        mRecipientAdapter.updateRecipientList(mMessageWithRecipients.getRecipients());
    }

    private void updateViewsFromData() {
        updateRecipientAdapterAndAnimateChange();
        updateRecipientListViewVisibility();
        EditText messageContentInput = findViewById(R.id.message_content_edittext);
        messageContentInput.setText(mMessageWithRecipients.getMessage().getTextContent());
        messageContentInput.requestFocus();

        updateDateButtonText();
        updateTimeButtonText();
    }

    private void updateRecipientListViewVisibility() {
        if (mMessageWithRecipients.getRecipients().isEmpty()) {
            findViewById(R.id.recipients_recyclerview).setVisibility(View.GONE);
        }
        else {
            findViewById(R.id.recipients_recyclerview).setVisibility(View.VISIBLE);
        }
    }

    private void setUpAllButtons() {
        setUpRecipientButtons();
        setUpDateAndTimeButtons();
        setUpScheduleButton();
    }

    private void setUpScheduleButton() {
        mFloatingActionButton = findViewById(R.id.schedule_button);
        mFloatingActionButton.setOnClickListener(view -> {
            getMessageContentFromInputAndSetValue(); // this is the only data that might not be latest, so update it
            validateFormAndPerformAppropriateAction();
        });
    }

    private void validateFormAndPerformAppropriateAction() {
        if (areRecipientsValid() && isMessageContentValid() && areDateAndTimeValid()) {
            // TODO show confirmation dialog
            SaveAndScheduleMessage scheduleMessageTask = new SaveAndScheduleMessage();
            scheduleMessageTask.setArguments(AppDatabase.getInstance(this), mMessageWithRecipients);
            scheduleMessageTask.execute();
            this.finish();
        }
    }

    private boolean areRecipientsValid() {
        if (mMessageWithRecipients.getRecipients().isEmpty()) {
            Snackbar.make(findViewById(R.id.schedule_button), R.string.error_no_recipients, Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private boolean isMessageContentValid() {
        String textContent = mMessageWithRecipients.getMessage().getTextContent();
        if (StringUtils.isEmpty(textContent.trim())) {
            Snackbar.make(findViewById(R.id.schedule_button), R.string.error_no_message, Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private boolean areDateAndTimeValid()  {
        Calendar scheduledDateTime = mMessageWithRecipients.getMessage().getScheduledDateTime();
        Calendar minimumDateTime = Calendar.getInstance();
        minimumDateTime.add(Calendar.MINUTE, 1); // messages must be scheduled for at least 1 minute into the future

        if (scheduledDateTime.before(minimumDateTime)) {
            Snackbar.make(findViewById(R.id.schedule_button), R.string.error_datetime_not_future, Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    private void setUpRecipientsRecyclerView() {
        mRecipientAdapter = new RecipientAdapter(this);
        RecyclerView recyclerView = findViewById(R.id.recipients_recyclerview);
        recyclerView.setAdapter(mRecipientAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            // need this to prevent nested scrolling
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
    }

    private void setUpDateAndTimeButtons() {

        Calendar scheduledDateTime = mMessageWithRecipients.getMessage().getScheduledDateTime();

        Button dateButton = findViewById(R.id.date_button);
        dateButton.setOnClickListener(v -> {
            DatePickerDialog dialog =
                    new DatePickerDialog(this,
                            R.style.PickerDialogTheme,
                            this,
                            scheduledDateTime.get(Calendar.YEAR),
                            scheduledDateTime.get(Calendar.MONTH),
                            scheduledDateTime.get(Calendar.DAY_OF_MONTH));
            dialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
            dialog.show();
        });

        Button timeButton = findViewById(R.id.time_button);
        timeButton.setOnClickListener(v -> {
            TimePickerDialog dialog =
                    new TimePickerDialog(this,
                            R.style.PickerDialogTheme,
                            this,
                            scheduledDateTime.get(Calendar.HOUR_OF_DAY),
                            scheduledDateTime.get(Calendar.MINUTE),
                            false);
            dialog.show();
        });
    }
    private void setUpRecipientButtons() {
        setUpContactButton();
        setUpPhoneNumberButton();

    }

    private void setUpContactButton() {
        Button contactButton = findViewById(R.id.contact_button);
        contactButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_PICK_CONTACT);
        });
    }

    private void setUpPhoneNumberButton() {
        Button phoneNumberButton = findViewById(R.id.phone_number_button);
        phoneNumberButton.setOnClickListener(v -> {
            NewRecipientDialogFragment newRecipientDialogFragment =  new NewRecipientDialogFragment();
            newRecipientDialogFragment.show(getSupportFragmentManager(),
                    NewRecipientDialogFragment.class.getName());
        });
    }


    private void updateDateButtonText() {
        Button dateButton = findViewById(R.id.date_button);
        dateButton.setText(generateDateButtonText());
    }

    private void updateTimeButtonText() {
        Button timeButton = findViewById(R.id.time_button);
        timeButton.setText(generateTimeButtonText());
    }

    private String generateDateButtonText() {
        StringBuilder builder = new StringBuilder();
        Date scheduledDateTime = mMessageWithRecipients.getMessage().getScheduledDateTime().getTime();
        builder.append(MessageDetailsViewBindingSupport.DAY_FORMATTER.format(scheduledDateTime).toUpperCase());
        builder.append("\n");
        builder.append(MessageDetailsViewBindingSupport.DATE_FORMATTER.format(scheduledDateTime).toUpperCase());
        return builder.toString();

    }

    private String generateTimeButtonText() {
        Date scheduledDateTime = mMessageWithRecipients.getMessage().getScheduledDateTime().getTime();
        return MessageDetailsViewBindingSupport.TIME_FORMATTER.format(scheduledDateTime).toUpperCase();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar scheduledDateTime = mMessageWithRecipients.getMessage().getScheduledDateTime();
        scheduledDateTime.set(Calendar.YEAR, year);
        scheduledDateTime.set(Calendar.MONTH, month);
        scheduledDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        updateDateButtonText();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar scheduledDateTime = mMessageWithRecipients.getMessage().getScheduledDateTime();
        scheduledDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        scheduledDateTime.set(Calendar.MINUTE, minute);
        updateTimeButtonText();
    }

    /**
     * Called when user has finished picking a contact from the contact picker.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_PICK_CONTACT:
                    getContactAndAddToRecipientsList(data);
            }
        }
    }

    private void getContactAndAddToRecipientsList(Intent data) {
        Uri contactData = data.getData();
        Cursor cursor =
                getContentResolver()
                        .query(contactData, null, null, null, null);
        if (cursor.moveToFirst()) {
            boolean hasPhoneNumber =
                    cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) == 1;
            if (!hasPhoneNumber) {
                Snackbar.make(findViewById(R.id.schedule_button), R.string.error_no_number_for_contact, Snackbar.LENGTH_LONG).show();
                cursor.close();
                return;
            }
            String contactName =
                    cursor.getString(
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String contactPhoneNumber =
                    cursor.getString(
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String formattedPhoneNumber =
                    PhoneNumberUtils.formatNumber(contactPhoneNumber, Locale.getDefault().getCountry());
            Recipient recipient = new Recipient();
            recipient.setName(contactName);
            recipient.setPhoneNumber(formattedPhoneNumber);
            addRecipientIfNotInCurrentListOrShowErrorToast(recipient);
        }
        else {
            Snackbar.make(findViewById(R.id.schedule_button), R.string.error_no_contact, Snackbar.LENGTH_LONG).show();
        }
        cursor.close();
    }

    /**
     * Called when the "Enter new recipient" dialog's save button is pressed.
     * @param name the name of the new recipient
     * @param phoneNumber the phone number of the new recipient
     */
    @Override
    public void onSaveNewManualRecipient(String name, String phoneNumber) {
        Recipient recipient = new Recipient();
        recipient.setName(name);
        recipient.setPhoneNumber(phoneNumber);

        addRecipientIfNotInCurrentListOrShowErrorToast(recipient);

    }

    private void addRecipientIfNotInCurrentListOrShowErrorToast(Recipient recipient) {
        if (isRecipientInCurrentRecipientList(recipient)) {
            Snackbar.make(findViewById(R.id.schedule_button), R.string.error_duplicate_recipient, Snackbar.LENGTH_LONG).show();
        }
        else {
            mMessageWithRecipients.getRecipients().add(recipient);
            // notify the adapter
            updateRecipientAdapterAndAnimateChange();
            updateRecipientListViewVisibility();
        }
    }

    /**
     * This method goes through the current list of recipients for this message, and
     * checks if the new recipient's phone number is already in the list.
     */
    private boolean isRecipientInCurrentRecipientList(Recipient recipient) {
        String newPhoneNumber = stripPunctuationAndWhiteSpace(recipient.getPhoneNumber());
        for (Recipient listRecipient : mMessageWithRecipients.getRecipients()) {
            String listRecipPhoneNum = stripPunctuationAndWhiteSpace(listRecipient.getPhoneNumber());
            if (StringUtils.equals(newPhoneNumber, listRecipPhoneNum)) {
                return true;
            }
        }
        return false;
    }

    private String stripPunctuationAndWhiteSpace(String string) {
        return string.replaceAll("\\s+","")
                .replaceAll("[^A-Za-z0-9]+", "");
    }

    private void showExitDialog() {
        ExitConfirmationDialog exitConfirmationDialog =  new ExitConfirmationDialog();
        exitConfirmationDialog.show(getSupportFragmentManager(),
                ExitConfirmationDialog.class.getName());
    }


    /**
     * Intercept an "Up" button press to show the exit warning dialog
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showExitDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Intercept a back button press to show the exit warning dialog
     */
    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    @Override
    public void removeRecipient(Recipient recipientToRemove) {
        mMessageWithRecipients.getRecipients().remove(recipientToRemove);
        updateRecipientAdapterAndAnimateChange();
        updateRecipientListViewVisibility();
    }
}
