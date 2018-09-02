package cs371m.hermes.futuremessenger.ui.draft.screens;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.pojo.MessageWithRecipients;
import cs371m.hermes.futuremessenger.ui.draft.adapters.RecipientAdapter;
import cs371m.hermes.futuremessenger.ui.draft.screens.dialogs.NewRecipientDialogFragment;
import cs371m.hermes.futuremessenger.ui.main.adapters.message.MessageAdapter;

public class EditTextMessageActivity extends AppCompatActivity implements
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener,
        NewRecipientDialogFragment.NewRecipientInfoSaveListener,
        RecipientAdapter.RecipientRemoveListener {

    private Toolbar mToolbar;

    private static final int REQUEST_CODE_PICK_CONTACT = 0;

    private Calendar mDateTime = Calendar.getInstance(); // TODO change this to use the calendar inside of the message

    private MessageWithRecipients mMessageWithRecipients;

    private RecipientAdapter mRecipientAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO load from the bundle if savedInstanceState is not null/empty
        mMessageWithRecipients = new MessageWithRecipients(new Message(), new ArrayList<>());

        setContentView(R.layout.activity_edit_text_message);

        mToolbar = findViewById(R.id.edit_toolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setUpButtons();
        setUpRecipientsRecyclerView();
    }

    private void setUpButtons() {
        setUpRecipientButtons();
        setUpDateAndTimeButtons();
    }
    private void setUpRecipientButtons() {

        setUpContactButton();
        setUpPhoneNumberButton();

    }

    private void setUpRecipientsRecyclerView() {
        mRecipientAdapter = new RecipientAdapter(this);
        RecyclerView recyclerView = findViewById(R.id.recipients_recyclerview);
        // TODO recyclerView.setItemAnimator();
        recyclerView.setAdapter(mRecipientAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            // need this to prevent nested scrolling
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
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


    private void setUpDateAndTimeButtons() {

        updateDateButtonText();

        Button dateButton = findViewById(R.id.date_button);
        dateButton.setOnClickListener(v -> {
            DatePickerDialog dialog =
                    new DatePickerDialog(this,
                            R.style.PickerDialogTheme,
                            this,
                            mDateTime.get(Calendar.YEAR),
                            mDateTime.get(Calendar.MONTH),
                            mDateTime.get(Calendar.DAY_OF_MONTH));
            dialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
            dialog.show();
        });

        updateTimeButtonText();
        Button timeButton = findViewById(R.id.time_button);
        timeButton.setOnClickListener(v -> {
            TimePickerDialog dialog =
                    new TimePickerDialog(this,
                            R.style.PickerDialogTheme,
                            this,
                            mDateTime.get(Calendar.HOUR_OF_DAY),
                            mDateTime.get(Calendar.MINUTE),
                            false);
            dialog.show();
        });
    }

    private String getDateButtonText() {
        StringBuilder builder = new StringBuilder();
        Date date = mDateTime.getTime();
        builder.append(MessageAdapter.DAY_FORMATTER.format(date).toUpperCase());
        builder.append("\n");
        builder.append(MessageAdapter.DATE_FORMATTER.format(date).toUpperCase());
        return builder.toString();

    }

    private String getTimeButtonText() {
        return MessageAdapter.TIME_FORMATTER.format(mDateTime.getTime()).toUpperCase();
    }

    private void updateDateButtonText() {
        Button dateButton = findViewById(R.id.date_button);
        dateButton.setText(getDateButtonText());
    }

    private void updateTimeButtonText() {
        Button timeButton = findViewById(R.id.time_button);
        timeButton.setText(getTimeButtonText());
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mDateTime.set(Calendar.YEAR, year);
        mDateTime.set(Calendar.MONTH, month);
        mDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);


        updateDateButtonText();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mDateTime.set(Calendar.MINUTE, minute);

        updateTimeButtonText();
    }

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
                Toast.makeText(this, R.string.error_no_number_for_contact, Toast.LENGTH_SHORT).show();
                cursor.close();
                return;
            }
            String contactName =
                    cursor.getString(
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String contactPhoneNumber =
                    cursor.getString(
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            Recipient recipient = new Recipient();
            recipient.setName(contactName);
            recipient.setPhoneNumber(contactPhoneNumber);
            addRecipientIfNotInCurrentListOrShowErrorToast(recipient);
        }
        cursor.close();
    }
    private void showExitDialog() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this,
                        R.style.GeneralDialogTheme);
        builder.setTitle(R.string.exit_dialog_title)
                .setMessage(R.string.exit_dialog_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
    }


    /**
     * Intercept an "Up" button press
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

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    /**
     * Called when the "Enter new recipient" dialog's save button is pressed.
     * @param name the name of the new recipient
     * @param phoneNumber the phone number of the new recipient
     */
    @Override
    public void onSaveNewRecipient(String name, String phoneNumber) {
        Recipient recipient = new Recipient();
        recipient.setName(name);
        recipient.setPhoneNumber(phoneNumber);

        addRecipientIfNotInCurrentListOrShowErrorToast(recipient);

    }

    private void addRecipientIfNotInCurrentListOrShowErrorToast(Recipient recipient) {
        if (isRecipientInCurrentRecipientList(recipient)) {
            Toast.makeText(this, R.string.error_duplicate_recipient, Toast.LENGTH_SHORT)
                    .show();
        }
        else {
            mMessageWithRecipients.getRecipients().add(recipient);
            // notify the adapter
            mRecipientAdapter.updateRecipientList(mMessageWithRecipients.getRecipients());
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

    @Override
    public void removeRecipient(Recipient recipientToRemove) {
        mMessageWithRecipients.getRecipients().remove(recipientToRemove);
        mRecipientAdapter.updateRecipientList(mMessageWithRecipients.getRecipients());
    }
}
