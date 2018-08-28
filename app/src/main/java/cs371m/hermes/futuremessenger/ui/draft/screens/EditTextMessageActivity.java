package cs371m.hermes.futuremessenger.ui.draft.screens;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.ui.main.adapters.message.MessageAdapter;

public class EditTextMessageActivity extends AppCompatActivity implements
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private Toolbar mToolbar;

    private static final int RESULT_PICK_CONTACT = 0;

    private Calendar mDateTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text_message);

        mToolbar = findViewById(R.id.edit_toolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setUpButtons();
    }

    private void setUpButtons() {
        setUpRecipientButtons();
        setUpDateAndTimeButtons();
    }
    private void setUpRecipientButtons() {

        setUpContactButton();
        setUpPhoneNumberButton();

    }

    private void setUpContactButton() {
        Button contactButton = findViewById(R.id.contact_button);
        contactButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                                       ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(intent, RESULT_PICK_CONTACT);
        });
    }
    private void setUpPhoneNumberButton() {
        Button phoneNumberButton = findViewById(R.id.phone_number_button);
        phoneNumberButton.setOnClickListener(v -> {

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(30, 30, 30, 30);

            EditText phoneNumberInput = new EditText(this);
            phoneNumberInput.setInputType(InputType.TYPE_CLASS_PHONE);
            phoneNumberInput.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
            phoneNumberInput.setHint(R.string.phone_number_edittext_hint);
            layout.addView(phoneNumberInput, params);

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(this,
                                             R.style.PhoneNumberDialogTheme);
            builder.setTitle(R.string.phone_number_dialog_title)
                   .setView(layout)
                   .setPositiveButton(R.string.save, (dialog, which) -> {
                       // TODO create and add phone number recipient
                   })
                   .setNegativeButton(R.string.cancel, ((dialog, which) -> {
                       dialog.dismiss();
                   }))
                   .create()
                   .show();
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
                case RESULT_PICK_CONTACT:

            }
        }
    }

    private void showExitDialog() {
        Log.d("Edit Text Message", "Up click");
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
}
