package cs371m.hermes.futuremessenger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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

import com.getbase.floatingactionbutton.FloatingActionsMenu;


public class MainActivity extends AppCompatActivity {

    // Future Messenger's database.
    public MessengerDatabaseHelper mDb;

    // ID of the message in the ListView that was clicked last.
    private long last_clicked_message_id;

    // When a message gets sent, the alarm receiver will broadcast this action to refresh
    // the ListView.
    private static final String REFRESH_LV_ACTION = "cs371m.hermes.futuremessenger.refreshlv";

    // Receiver for refresh list view broadcasts
    BroadcastReceiver refreshLVReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            fillListView();
        }
    };

    /*
     *  Inflate the individual message edit/delete menu.
     *  */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.message_menu, menu);
    }

    // Message context menu options.
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                editScheduledMessage(last_clicked_message_id);
                return true;
            case R.id.delete:
                deleteScheduledMessage(last_clicked_message_id);
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
        getSupportActionBar().setTitle(R.string.currently_scheduled_tv);

        // Create our database.
        mDb = new MessengerDatabaseHelper(MainActivity.this);

        // Populate the listview from the database.
        fillListView();

        initializeFloatingMenu();
        registerRefreshReceiver();


    }

    // Registers a receiver to update the list view if a message gets sent off and deleted
    // while the activity is open.
    private void registerRefreshReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                refreshLVReceiver, new IntentFilter (REFRESH_LV_ACTION));
    }

    // Unregister the refreshLV receiver
    private void unregisterRefreshReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshLVReceiver);
    }

    // Initalize the floating actions menu.
    private void initializeFloatingMenu() {
        final FloatingActionsMenu main_menu = (FloatingActionsMenu) findViewById(R.id.main_menu);

        com.getbase.floatingactionbutton.FloatingActionButton preset_button =
                (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.create_preset_button);
        preset_button.setIconDrawable(getResources().getDrawable(R.drawable.preset_icon));
        preset_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Beta feature!", Toast.LENGTH_SHORT).show();
                main_menu.collapse();
            }
        });

        com.getbase.floatingactionbutton.FloatingActionButton text_button =
                (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.new_text_button);
        text_button.setIconDrawable(getResources().getDrawable(R.drawable.text_icon));
        text_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main_menu.collapse();
                createTextMessage();
            }
        });

        com.getbase.floatingactionbutton.FloatingActionButton pic_button =
                (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.new_pic_button);
        pic_button.setIconDrawable(getResources().getDrawable(R.drawable.picture_icon));
        pic_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main_menu.collapse();
                Toast.makeText(MainActivity.this, "Beta feature!", Toast.LENGTH_SHORT).show();
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
        unregisterRefreshReceiver();
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
        stopAlarm(last_clicked_message_id);
        mDb.deleteMessage(last_clicked_message_id);
        // Force a refresh of the listView so that the changes will be reflected in the ListView.
        fillListView();
    }

    public void stopAlarm(long message_id){
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(),
                (int) message_id, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Log.d("Alarm", "Alarm canceled");
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




        /* Make the list items clickable for their context menu */
        registerForContextMenu(findViewById(R.id.scheduled_messages_list));

        // Allow short clicks to open the context menu
        messagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                last_clicked_message_id = id;
                openContextMenu(findViewById(R.id.scheduled_messages_list));
                Log.d("Short Click", "Last clicked message id just set to " + last_clicked_message_id);
            }
        });


        // Allow long clicks to open the context menu.
        messagesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                last_clicked_message_id = id;
                Log.d("Long Click", "Last clicked message id just set to " + last_clicked_message_id);
                return false;
            }
        });
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
        registerRefreshReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterRefreshReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterRefreshReceiver();
    }
}
