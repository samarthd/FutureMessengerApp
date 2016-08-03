package cs371m.hermes.futuremessenger;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ManagePresets extends AppCompatActivity {

    private MessengerDatabaseHelper mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_presets);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDb = new MessengerDatabaseHelper(this);
        fillPresetList();

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

}
