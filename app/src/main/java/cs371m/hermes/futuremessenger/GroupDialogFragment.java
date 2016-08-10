package cs371m.hermes.futuremessenger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by dob on 8/3/2016.
 * Fragment that asks users whether they want to send messages as a group MMS.
 */
public class GroupDialogFragment extends DialogFragment {

    public interface GroupDialogListener {
        void onGroupSelected(int i);
    }

    GroupDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.group_dialog_title)
               .setItems(R.array.group_choice, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d("Group Fragment", Integer.toString(i));
                        mListener.onGroupSelected(i);
                    }
               });
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (GroupDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException((activity.toString() + "must implement GroupDialogListener"));
        }
    }
}
