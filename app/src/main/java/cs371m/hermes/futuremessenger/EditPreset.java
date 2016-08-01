package cs371m.hermes.futuremessenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class EditPreset extends AppCompatActivity {

    private final String TAG = "EditPreset";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_preset);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            fillEditTextFields(intent.getStringExtra("name"), intent.getStringExtra("message"));
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Save text into database
                EditText name = (EditText) findViewById(R.id.edit_preset_name);
                EditText message = (EditText) findViewById(R.id.edit_preset_message);
                Log.d(TAG, name.getText().toString());
                Log.d(TAG, message.getText().toString());
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void fillEditTextFields(String name_text, String msg_text) {
        EditText name = (EditText) findViewById(R.id.edit_preset_name);
        EditText message = (EditText) findViewById(R.id.edit_preset_message);
        name.setText(name_text);
        message.setText(msg_text);
    }

}
