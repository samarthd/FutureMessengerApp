package cs371m.hermes.futuremessenger;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by dob on 7/17/2016.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState) {
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
        //TODO: Change date on Button
        ((EditTextMessageActivity)getActivity()).setDateButton(year, month, dayOfMonth);
    }
}
