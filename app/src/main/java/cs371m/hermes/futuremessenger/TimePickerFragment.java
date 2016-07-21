package cs371m.hermes.futuremessenger;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by dob on 7/17/2016.
 */
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        EditTextMessageActivity activity = (EditTextMessageActivity) getActivity();

        int hour = activity.get_hour();
        int minute = activity.get_minute();

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        if (DateFormat.is24HourFormat(getActivity())) {
            //TODO: Set string based on 24 hour format
            ((EditTextMessageActivity)getActivity()).setTimeButton(hour, minute);
        } else {
            //TODO: change this, feels hack-y
            ((EditTextMessageActivity)getActivity()).setTimeButton(hour, minute);
        }
    }
}
