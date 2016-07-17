package cs371m.hermes.futuremessenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;

public class MainActivity extends AppCompatActivity {


    // Define menu creation.
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

    // Menu options for the message menu.
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            // The following cases apply to the message menu.
            case R.id.edit:
                editScheduledMessage();
                return true;
            case R.id.delete:
                return true;
            // The following cases apply to the creation menu.
            case R.id.manage_presets:
                return true;
            case R.id.new_text_message:
                sendSmsMessage();
                return true;
            case R.id.new_picture_message:
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

        // Create the ListView for all the scheduled messages
        ListView scheduled_messages_view = (ListView) findViewById(R.id.scheduled_messages_list);

        // This is a temporary adapter to get some messages. Change this to load entries from a database.
        String[] test_array = {"Hello1", "Hello2", "Hello3", "Hello1", "Hello2", "Hello3", "Hello1", "Hello2", "Hello3", "Hello1", "Hello2", "Hello3", "Hello1", "Hello2", "Hello3", "Hello1", "Hello2", "Hello3"};
        ArrayAdapter<String> test_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, test_array);
        scheduled_messages_view.setAdapter(test_adapter);

        // On each item click, open the context menu.
        registerForContextMenu(scheduled_messages_view);
        scheduled_messages_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                view.showContextMenu();
            }
        });

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendSmsMessage() {
        /* if I intend to edit a message, I should send that info into the intent
         * and the Activity can grab that info to fill in their data
         */
        //TODO: make use of EditTextMessageActivity,
        Intent intent = new Intent(this, TextMessage.class);
        startActivity(intent);
    }

    private void editScheduledMessage() {
        //TODO: get actual data from scheduled messages
        String phonenum = "0123456789";
        String date = "01-01-1991";
        String time = "12:00 PM";
        String message = "This is a prefixed message";

        Intent intent  = new Intent(this, EditTextMessageActivity.class);
        intent.putExtra("num", phonenum);
        intent.putExtra("date", date);
        intent.putExtra("time", time);
        intent.putExtra("message", message);

        startActivity(intent);
    }
}
