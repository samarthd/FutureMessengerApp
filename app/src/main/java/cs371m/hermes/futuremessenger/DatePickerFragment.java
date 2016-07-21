package cs371m.hermes.futuremessenger;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

/**
 * Created by dob on 7/17/2016.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    DatePickerListener mListener;
    public interface DatePickerListener {
        void onDateSelected (int year, int month, int dayOfMonth);
    }

    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState) {
        // TODO: Find better way to get currently set stuff
        EditTextMessageActivity activity = (EditTextMessageActivity) getActivity();
        // Use the current date as the default date in the picker
        int year = activity.get_year();
        int month = activity.get_month();
        int day = activity.get_dayOfMonth();

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
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
