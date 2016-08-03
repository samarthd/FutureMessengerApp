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
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ManagePresets extends AppCompatActivity {

    private MessengerDatabaseHelper mDb;

    private long last_clicked_preset_id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_presets);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDb = new MessengerDatabaseHelper(this);
        fillPresetList();

        setUpContextMenu();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewPreset();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void createNewPreset() {
        Intent intent = new Intent(this, EditPreset.class);
        startActivityForResult(intent, 1);
    }

    private void setUpContextMenu(){

        /* Make the list items clickable for their context menu */
        final ListView presetListView = (ListView) findViewById(R.id.presets_lv);
        registerForContextMenu(presetListView);

        // Allow short clicks to open the context menu
        presetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                last_clicked_preset_id = id;
                openContextMenu(presetListView);
                Log.d("Short Click", "Last clicked preset id just set to " + last_clicked_preset_id);
            }
        });


        // Allow long clicks to open the context menu.
        presetListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                last_clicked_preset_id = id;
                Log.d("Long Click", "Last clicked preset id just set to " + last_clicked_preset_id);
                return false;
            }
        });
    }

    /*
     *  Inflate the individual preset edit/delete menu. */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.preset_menu, menu);
    }

    // Message context menu options.
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_preset:
                editPreset();
                return true;
            case R.id.delete_preset:
                deletePreset();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == this.RESULT_OK) {
                fillPresetList();
            }
        }
    }

    // Populate the preset list
    private void fillPresetList() {
        Cursor presetCursor = mDb.getAllPresets();
        ListView preset_lv = (ListView) findViewById(R.id.presets_lv);

        String[] fromCols = {mDb.PRESET_NAME, mDb.PRESET_CONTENT};
        int[] toViews = {R.id.preset_name_tv, R.id.preset_content_tv};
        SimpleCursorAdapter adapter =
                new SimpleCursorAdapter(getBaseContext(), R.layout.listed_preset_layout,
                                        presetCursor, fromCols, toViews);
        preset_lv.setAdapter(adapter);
        preset_lv.setEmptyView(findViewById(R.id.empty_preset_tv));
    }

    private void editPreset() {
        // Get the message's data.
        String[] preset_info = mDb.getPresetData(last_clicked_preset_id);
        if (preset_info != null) {

            String name = preset_info[0];
            String content = preset_info[1];

            // Place the data in an intent
            Intent intent = new Intent(this, EditPreset.class);
            intent.putExtra("name", name);
            intent.putExtra("preset_id", last_clicked_preset_id);
            intent.putExtra("content", content);

            // Start the edit message activity through this intent.
            startActivityForResult(intent, 1);
        }
        else {
            Toast.makeText(ManagePresets.this, "That preset can't be edited.", Toast.LENGTH_SHORT).show();
        }

    }

    private void deletePreset() {
        mDb.deletePreset(last_clicked_preset_id);
        // Force a refresh of the listView so that the changes will be reflected in the ListView.
        fillPresetList();
    }

}
