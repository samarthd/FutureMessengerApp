package cs371m.hermes.futuremessenger;

import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;

//TODO: Have these activities extend an MessageActivity, to remove duplicate code
public class MultimediaMessageActivity extends AppCompatActivity implements DatePickerFragment.DatePickerListener, TimePickerFragment.TimePickerListener {

    private EditText _contact_field;
    private Button _date_button;
    private Button _time_button;
    private EditText _message_field;

    /* These variables hold the scheduling information that is
     * given to the button/taken from the button. */
    private int _hour = 0;
    private int _minute = 0;
    private int _year = 0;
    private int _month = 0; // January = 0, February = 1, ...
    private int _dayOfMonth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multimedia_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMMS("5554;5556", "Hello MMS!");
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void sendMMS(String phonenum, String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mmsto:" + phonenum));
        intent.putExtra("sms_body", message);
        File sdcard = Environment.getExternalStorageDirectory();
        File gif = new File (sdcard, "Download/dancing-banana.gif");
        Log.d("sendMMS", gif.getPath());
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(gif));

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Send MMS"));
        }

    }

    public void showTimePickerDialog (View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    @Override
    public void onTimeSelected (int hour, int minute) {

    }

    public void showDatePickerDialog (View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    @Override
    public void onDateSelected(int year, int month, int dayOfMonth) {

    }

}
