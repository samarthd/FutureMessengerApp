package cs371m.hermes.futuremessenger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        //Get the contact at this position.
        Contact current = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.selected_contact_layout,
                                                                    parent, false);
        }
        // Lookup view for data population
        TextView contactName = (TextView) convertView.findViewById(R.id.contact_name);
        TextView contactNumber = (TextView) convertView.findViewById(R.id.contact_num);
        // Populate the data into the template view using the data object
        contactName.setText(current.getName());
        contactNumber.setText(current.getPhoneNum());
        // Return the completed view to render on screen
        return convertView;
    }
}
