package cs371m.hermes.futuremessenger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Samarth on 7/30/2016.
 * This adapter is used to populate the selected contacts list in the edit message activities.
 *
 * Code based on this reference:
 * https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView
 *
 */
public class ContactListAdapter extends ArrayAdapter<Contact> {
    public ContactListAdapter(Context context, ArrayList<Contact> contacts) {
        super(context, 0, contacts);
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        //Get the contact at this position.
        Contact current = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.selected_contact_layout,
                                                                    parent, false);
        }

        // Get the views that we will be populating data into
        TextView contactName = (TextView) convertView.findViewById(R.id.contact_name);
        TextView contactNumber = (TextView) convertView.findViewById(R.id.contact_num);

        // Populate the data into the views
        contactName.setText(current.getName());
        contactNumber.setText(current.getPhoneNum());

        // Allow this row to be deleted by the delete selected contact button.
        // Code based on:
        // http://stackoverflow.com/questions/7831395/android-how-to-delete-a-row-from-a-listview-with-a-delete-button-in-the-row
        ImageButton deleteSelectedContact =
                (ImageButton) convertView.findViewById(R.id.remove_selected_contact_button);
        deleteSelectedContact.setTag(position);
        deleteSelectedContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer index = (Integer) view.getTag();
                int sizeBeforeDelete = getCount();
                remove(getItem(index.intValue()));
                // Resize the list view height if the size has just decreased below the
                // threshold of 4.
                if (sizeBeforeDelete <= 4) {
                    LinearLayout.LayoutParams param =
                            (LinearLayout.LayoutParams) parent.getLayoutParams();

                    int numRows = getCount();
                    int sumHeight = 0;
                    for (int i = 0; i < numRows; i++) {
                        View item = getView(i, null, parent);
                        item.measure(0, 0);
                        sumHeight += item.getMeasuredHeight();
                    }
                    param.height = sumHeight;
                    parent.setLayoutParams(param);
                }
            }
        });
        return convertView;
    }
}
