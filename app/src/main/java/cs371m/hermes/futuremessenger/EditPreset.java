package cs371m.hermes.futuremessenger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Editing activity for presets.
 * Allows for composition of new presets or editing of existing presets, and then saves them in
 * the database.
 */
public class EditPreset extends AppCompatActivity {

    private final String TAG = "EditPreset";
    // Stores the last clicked preset ID so that the edit fields can be populated correctly
    private long mLast_clicked_preset_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_preset);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        /* If we're editing an existing preset, the preset ID will be valid. If we're making a new
           preset, it will be -1. */
        if (extras != null) {
            mLast_clicked_preset_id = intent.getLongExtra("preset_id", -1);
            fillEditTextFields(intent.getStringExtra("name"), intent.getStringExtra("content"));
        }
        else {
            mLast_clicked_preset_id = -1;
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText name = (EditText) findViewById(R.id.edit_preset_name);
                EditText message = (EditText) findViewById(R.id.edit_preset_message);
                String preset_name = name.getText().toString();
                String preset_message = message.getText().toString();

                if (!isFieldsEmpty()) {
                    // If we aren't editing an existing preset, then store a new one.
                    if (mLast_clicked_preset_id == -1) {
                        savePreset(preset_name, preset_message);
                        Log.d(TAG, "Saved new preset with name " + preset_name + " and message " +
                                preset_message);
                    }
                    // We are editing an existing one, so just update its values.
                    else {
                        updatePreset(preset_name, preset_message);
                        Log.d(TAG, "Edited preset with new name " + preset_name + " and message " +
                                preset_message);
                    }
                    Log.d(TAG, preset_name);
                    Log.d(TAG, preset_message);
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /* Verify that fields are populated before allowing storage */
    private boolean isFieldsEmpty() {
        boolean result = true;
        EditText name = (EditText) findViewById(R.id.edit_preset_name);
        EditText message = (EditText) findViewById(R.id.edit_preset_message);
        if (name.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(),
                    "Preset needs a name", Toast.LENGTH_SHORT).show();
        } else if (message.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(),
                    "No message set", Toast.LENGTH_SHORT).show();
        } else {
            result = false;
        }
        return result;
    }

    /* Populate fields from existing values */
    private void fillEditTextFields(String name_text, String msg_text) {
        EditText name = (EditText) findViewById(R.id.edit_preset_name);
        EditText message = (EditText) findViewById(R.id.edit_preset_message);
        name.setText(name_text);
        message.setText(msg_text);
    }

    // Save a new preset in our database.
    private void savePreset(String name, String message) {
        MessengerDatabaseHelper mDb = MessengerDatabaseHelper.getInstance(this);
        long result = mDb.storeNewPreset(name, message);
        Log.d("SAVE NEW PRESET", "Preset stored under id: " + result);
        mDb.close();
        Intent ret = new Intent(this, ManagePresets.class);
        setResult(ManagePresets.RESULT_OK, ret);
        finish();
    }

    // Update an existing preset in our database.
    private void updatePreset(String name, String message) {
        MessengerDatabaseHelper mDb = MessengerDatabaseHelper.getInstance(this);
        mDb.editPreset(mLast_clicked_preset_id, name, message);
        Log.d("EDIT PRESET", "Edited preset with id " + mLast_clicked_preset_id);
        mDb.close();
        Intent ret = new Intent(this, ManagePresets.class);
        setResult(ManagePresets.RESULT_OK, ret);
        finish();
    }

    // Ensure that the user doesn't accidentally quit without saving their changes.
    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                showExitDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.exit_dialog_title)
                .setMessage(R.string.exit_confirmation_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
