package cs371m.hermes.futuremessenger;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

/**
 * Created by dob on 7/17/2016.
 * Time picker fragment that allows users to select a time to schedule a message for.
 */
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    TimePickerListener mListener;
    public interface TimePickerListener {
        void onTimeSelected (int hour, int minute);
    }

    static TimePickerFragment newInstance(int hour, int minute) {
        TimePickerFragment f = new TimePickerFragment();
        Bundle args = new Bundle();
        args.putInt("hour", hour);
        args.putInt("minute", minute);
        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int hour = getArguments().getInt("hour");
        int minute = getArguments().getInt("minute");

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    /**
     * Called when the user has chosen the time in the picker
     * @param timePicker
     * @param hour the hour selected
     * @param minute the minute selected
     */
    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        mListener.onTimeSelected(hour, minute);
    }

    @Override
    public void onAttach (Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (TimePickerListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException((activity.toString() + " must implement TimePickerListener"));
        }
    }
}
