package cs371m.hermes.futuremessenger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Samarth on 8/1/2016.
 * Code is based on http://android-developers.blogspot.com/2012/05/using-dialogfragments.html
 */

public class EnterPhoneNumberDialogFragment extends DialogFragment {

    public interface EnterPhoneNumberListener {
        void onFinishEnterPhoneNum (String phoneNum);
    }
    private EditText mPhoneNumInput;

    public EnterPhoneNumberDialogFragment() {
        //Empty Constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.enter_phone_num_fragment_layout, null);
        mPhoneNumInput = (EditText)  view.findViewById(R.id.enter_number_edittext);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.enter_phone_number);
        builder.setView(view);

        builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do nothing, functionality overridden in onStart
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });
        Dialog dialog = builder.create();
        //Format the input as a phone number as it is being typed.
        mPhoneNumInput.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        //Display keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mPhoneNumInput.requestFocus();
        return dialog;
    }

    /* This method needs to be overridden so that the OnClickListener of the positive dialog
       button is overridden as soon as the dialog is shown. This strategy is necessary to
       allow validation of input before the dialog closes. This strategy is based on
       the code found here:
       http://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked
     */
    @Override
    public void onStart() {
        super.onStart();
        final AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {

            Button add_button = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
            add_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EnterPhoneNumberListener activity = (EnterPhoneNumberListener) getActivity();
                    String phoneNum = mPhoneNumInput.getText().toString().trim();
                    if (phoneNum.equalsIgnoreCase("")) {
                        mPhoneNumInput.setError(getString(R.string.empty_number));
                        mPhoneNumInput.requestFocus();
                    }
                    else {
                        activity.onFinishEnterPhoneNum(phoneNum);
                        dismiss();
                    }
                }
            });
        }
    }
}
