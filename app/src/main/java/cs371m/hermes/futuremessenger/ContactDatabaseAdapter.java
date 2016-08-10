package cs371m.hermes.futuremessenger;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Samarth on 8/1/2016.
 * This adapter is used to build the listview on the MainActivity.
 * Guidance provided by:
 * https://coderwall.com/p/fmavhg/android-cursoradapter-with-custom-layout-and-how-to-use-it
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

        /* If the message content is blank or null, don't show the TextView */
        String message_content = cursor.getString(cursor.getColumnIndex(
                MessengerDatabaseHelper.MESSAGE_TXT_CONTENT));
        if (message_content == null || message_content.equals("")) {
            view.findViewById(R.id.message_layout).setVisibility(View.GONE);
        }
        else {
            view.findViewById(R.id.message_layout).setVisibility(View.VISIBLE);
        }
        messageContentTV.setText(message_content);

        // Set the formatted date and time
        dateTime.setText(cursor.getString(cursor.getColumnIndex(
                MessengerDatabaseHelper.MESSAGE_FORMATTED_DT)));

        // Choose the appropriate icon based on whether or not this is a picture or text message
        ImageView messThumbNail = (ImageView) view.findViewById(R.id.message_thumbnail);
        String img_path = cursor.getString(cursor.getColumnIndex(
                MessengerDatabaseHelper.MESSAGE_IMG_PATH));
        if (img_path == null)
            messThumbNail.setImageResource(R.drawable.text_icon);
        else
            messThumbNail.setImageResource(R.drawable.picture_icon);


        TextView recipientNames = (TextView) view.findViewById(R.id.recipient_names_tv);
        String unfilteredNames = cursor.getString(cursor.getColumnIndex("RECIPIENT_NAMES"));
        String unfilteredNums = cursor.getString(cursor.getColumnIndex("RECIPIENT_NUMBERS"));
        Log.d(TAG, "Unfiltered names: " + unfilteredNames);
        Log.d(TAG, "Unfiltered nums: " + unfilteredNums);

        String[] all_recip_names = unfilteredNames.split(";");
        String[] all_recip_nums = unfilteredNums.split(";");
        ArrayList<String> namesList = new ArrayList<>();
        int numRecipNames = all_recip_names.length;
        int numRecipNums = all_recip_nums.length;
        if (numRecipNames != numRecipNums) {
            Log.d(TAG, "Names and numbers lengths didn't match.");
            Log.d(TAG, "Names length: " + numRecipNames);
            Log.d(TAG, "Nums length: " + numRecipNums);
        }
        else {
            TextView recipientTV = (TextView) view.findViewById(R.id.recipient_label);
            if (numRecipNames == 1)
                recipientTV.setText(R.string.recipient_tv_singular);
            else
                recipientTV.setText(R.string.recipients_lv);

            /* For each recipient, if they don't have a given name (aren't a contact), display
               their phone number instead. */
            for (int i = 0; i < all_recip_names.length; i++) {
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
