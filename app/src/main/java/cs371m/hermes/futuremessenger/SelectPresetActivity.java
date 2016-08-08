package cs371m.hermes.futuremessenger;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class SelectPresetActivity extends AppCompatActivity {

    private MessengerDatabaseHelper mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_preset);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDb = MessengerDatabaseHelper.getInstance(this);
        fillSelectedPresetList();

    }

    // Populate the preset list
    private void fillSelectedPresetList() {
        Cursor presetCursor = mDb.getAllPresets();
        ListView preset_lv = (ListView) findViewById(R.id.presets_lv);
        String[] fromCols = {MessengerDatabaseHelper.PRESET_NAME, MessengerDatabaseHelper.PRESET_CONTENT};
        int[] toViews = {R.id.preset_name_tv, R.id.preset_content_tv};
        SimpleCursorAdapter adapter =
                new SimpleCursorAdapter(getBaseContext(), R.layout.listed_preset_layout,
                        presetCursor, fromCols, toViews);
        preset_lv.setAdapter(adapter);
        preset_lv.setEmptyView(findViewById(R.id.no_selectable_tv));
        preset_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long preset_id) {
                String[] presetData = mDb.getPresetData(preset_id);
                String content = presetData[1];
                Intent ret = new Intent();
                ret.putExtra("preset_content", content);
                setResult(EditTextMessageActivity.RESULT_OK, ret);
                finish();
            }
        });
        mDb.close();
    }
}
