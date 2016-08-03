package cs371m.hermes.futuremessenger;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Samarth on 8/1/2016.
 * This adapter is used to build the listview on the MainActivity.
 * Guidance provided by: https://coderwall.com/p/fmavhg/android-cursoradapter-with-custom-layout-and-how-to-use-it
 *
 */
public class ContactDatabaseAdapter extends CursorAdapter {

    private final String TAG = "ContactDatabaseAdapter";
    private final LayoutInflater inflater;

    public ContactDatabaseAdapter(Context context, Cursor data, int flags) {
        super(context, data, flags);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView messageContentTV = (TextView) view.findViewById(R.id.message_txt_tv);
        TextView dateTime = (TextView) view.findViewById(R.id.datetime_tv);

        messageContentTV.setText(cursor.getString(cursor.getColumnIndex("TEXT_CONTENT")));
        dateTime.setText(cursor.getString(cursor.getColumnIndex("FORMATTED_DATETIME")));

        TextView recipientNames = (TextView) view.findViewById(R.id.recipient_names_tv);
        Log.d(TAG, "Unfiltered names: " + cursor.getString(cursor.getColumnIndex("RECIPIENT_NAMES")));
        Log.d(TAG, "Unfiltered nums: " + cursor.getString(cursor.getColumnIndex("RECIPIENT_NUMBERS")));
        String[] all_recip_names = cursor.getString(cursor.getColumnIndex("RECIPIENT_NAMES")).split(";");
        String[] all_recip_nums = cursor.getString(cursor.getColumnIndex("RECIPIENT_NUMBERS")).split(";");

        ArrayList<String> namesList = new ArrayList<>();
        if (all_recip_names.length != all_recip_nums.length) {
            Log.d(TAG, "Names and numbers lengths didn't match.");
            Log.d(TAG, "Names length: " + all_recip_names.length);
            Log.d(TAG, "Nums length: " + all_recip_nums.length);
        }
        else {
            for (int i = 0; i < all_recip_names.length; i++) {
                // If the recipient doesn't have a name, they weren't a contact. Use their
                // number as their "name" instead.
                if (all_recip_names[i].equals(" "))
                    namesList.add(all_recip_nums[i]);
                else
                    namesList.add(all_recip_names[i]);
            }
            String result = android.text.TextUtils.join(", ", namesList);
            recipientNames.setText(result);
        }

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return inflater.inflate(R.layout.listed_message_layout, viewGroup, false);
    }
}
