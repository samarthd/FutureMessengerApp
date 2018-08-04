package cs371m.hermes.futuremessenger.ui.main;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import cs371m.hermes.futuremessenger.R;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mModel = ViewModelProviders.of(this).get(MainViewModel.class);


    }
}
