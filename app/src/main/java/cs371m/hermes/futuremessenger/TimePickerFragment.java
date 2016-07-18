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

    private Button _time_button;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        _time_button = (Button) getActivity().findViewById(R.id.button_time);

        //TODO: Figure out how to get time from button
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        if (DateFormat.is24HourFormat(getActivity())) {
            //TODO: Set string based on 24 hour format
        } else {
            String time = "" + (i % 12 == 0? "12" : Integer.toString(i%12) ) + ":"
                             + (i1 < 10 ? "0" : "") + Integer.toString(i1)
                             + (i < 12 ? " AM":" PM");
            Log.d("onTimeSet", time);
            _time_button.setText(time);
        }
    }
}
