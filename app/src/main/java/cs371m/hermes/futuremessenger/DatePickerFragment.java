package cs371m.hermes.futuremessenger;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by dob on 7/17/2016.
 * Allows users to pick the date to send a message.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    DatePickerListener mListener;
    public interface DatePickerListener {
        void onDateSelected (int year, int month, int dayOfMonth);
    }

    static DatePickerFragment newInstance(int year, int month, int day) {
        DatePickerFragment f = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putInt("year", year);
        args.putInt("month", month);
        args.putInt("day", day);
        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState) {
        int year = getArguments().getInt("year");
        int month = getArguments().getInt("month");
        int day = getArguments().getInt("day");

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog dpd = new DatePickerDialog(getActivity(), this, year, month, day);
        DatePicker picker = dpd.getDatePicker();
        Calendar minDate = Calendar.getInstance();
        minDate.set(Calendar.HOUR_OF_DAY, 0);
        minDate.set(Calendar.MINUTE, 0);
        minDate.set(Calendar.SECOND, 0);
        picker.setMinDate(minDate.getTimeInMillis());

        return dpd;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        mListener.onDateSelected(year, month, dayOfMonth);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (DatePickerListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException((activity.toString() + " must implement DatePickerListener"));
        }
    }
}
