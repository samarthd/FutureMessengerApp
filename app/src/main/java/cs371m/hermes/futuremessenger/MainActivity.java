package cs371m.hermes.futuremessenger;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    // Future Messenger's database.
    public MessengerDatabaseHelper mDb;

    //ID of the message in the ListView that was clicked last.
    private long last_clicked_message_id;

    /*
     *  Determine which context menu should be inflated: FAB's context menu, or
     *  the individual message edit/delete menu.
     *  */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();

        switch (v.getId()) {
            case R.id.scheduled_messages_list:
                inflater.inflate(R.menu.message_menu, menu);
                break;
            case R.id.fab:
                inflater.inflate(R.menu.creation_menu, menu);
                break;
        }
    }

    // Menu options.
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // The following cases apply to the message menu.
            case R.id.edit:
                editScheduledMessage(last_clicked_message_id);
                return true;
            case R.id.delete:
                deleteScheduledMessage(last_clicked_message_id);
                return true;
            // The following cases apply to the creation menu.
            case R.id.manage_presets:
                Toast.makeText(this, "Beta feature!", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.new_text_message:
                createTextMessage();
                return true;
            case R.id.new_picture_message:
                Toast.makeText(this, "Beta feature!", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create our database.
        mDb = new MessengerDatabaseHelper(MainActivity.this);

        // Populate the listview from the database.
        fillListView();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        registerForContextMenu(fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.showContextMenu();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, "Beta feature!", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the database connection.
        mDb.close();
    }


    private void createTextMessage() {
        Intent intent = new Intent(this, EditTextMessageActivity.class);
        startActivityForResult(intent, 1);
    }

    /* Edit a currently scheduled message. */
    private void editScheduledMessage(long message_id) {
        // Get the message's data.
        String[] message_info = mDb.getScheduledMessageData(message_id);
        String phonenums = message_info[0];
        String date = message_info[1];
        String time = message_info[2];
        String message = message_info[3];
        String dateTime = message_info[4];
        // Place the data in an intent.
        Intent intent  = new Intent(this, EditTextMessageActivity.class);
        intent.putExtra("num", phonenums);
        intent.putExtra("date", date);
        intent.putExtra("time", time);
        intent.putExtra("message", message);
        intent.putExtra("message_id", message_id);
        intent.putExtra("message_datetime", dateTime);
        // Start the edit message activity through this intent.
        startActivityForResult(intent, 1);
    }

    /* Delete a currently scheduled message. */
    private void deleteScheduledMessage(long last_clicked_message_id) {
        mDb.deleteMessage(last_clicked_message_id);
        // Force a refresh of the listView so that the changes will be reflected in the ListView.
        fillListView();
    }

    /* Populate the ListView from our database with all of the currently scheduled messages. */
    private void fillListView() {
        Cursor cursor = mDb.getAllScheduledMessages();
        String[] fromColumns = {mDb.MESSAGE_TXT_CONTENT,
                                mDb.MESSAGE_FORMATTED_DT,
                                "RECIPIENT_NUMBERS"};

        int[] toViews = new int[] {R.id.message_txt_tv, R.id.datetime_tv, R.id.recipient_nums_tv};
        SimpleCursorAdapter adapter =
                new SimpleCursorAdapter(getBaseContext(), R.layout.listed_message_layout, cursor,
                                        fromColumns, toViews, 0);
        ListView messagesListView = (ListView) findViewById(R.id.scheduled_messages_list);
        messagesListView.setAdapter(adapter);

        //Any time the message is long pressed, we should save its ID so it can be passed
        //to the editScheduledMessage() method.
        messagesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                last_clicked_message_id = id;
                Log.d("Long Click", "Last clicked message id just set to " + last_clicked_message_id);
                return false;
            }
        });
        registerForContextMenu(findViewById(R.id.scheduled_messages_list));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == MainActivity.RESULT_OK) {
                fillListView();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Force a refresh on the ListView
        fillListView();
    }
}
