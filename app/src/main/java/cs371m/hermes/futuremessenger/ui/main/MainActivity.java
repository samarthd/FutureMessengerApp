package cs371m.hermes.futuremessenger.ui.main;


import android.arch.lifecycle.ViewModelProviders;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.ArrayList;
import java.util.List;

import cs371m.hermes.futuremessenger.R;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private SpeedDialView mSpeedDialView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        mViewPager = findViewById(R.id.main_view_pager);
        setUpViewPager(mViewPager);

        mTabLayout = findViewById(R.id.main_tab_layout);
        mTabLayout.setupWithViewPager(mViewPager);

        setUpFloatingActionMenu();
    }

    private void setUpFloatingActionMenu() {
        mSpeedDialView = findViewById(R.id.floating_action_menu);
        // The "New Text Message" option
        mSpeedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.floating_action_menu_message_button, R.drawable.text_icon)
                        .setFabBackgroundColor(getResources().getColor(R.color.colorPrimary))
                        .setLabel(getResources().getString(R.string.new_text_message_label))
                        .setLabelColor(getResources().getColor(R.color.colorPrimary))
                        .setLabelBackgroundColor(getResources().getColor(R.color.plain_white))
                .create());
        mSpeedDialView.setOnActionSelectedListener(actionItem -> {
            switch(actionItem.getId()) {
                case R.id.floating_action_menu_message_button:
                    Toast.makeText(MainActivity.this, "Begetabo", Toast.LENGTH_SHORT).show();
                    return false;
                default:
                    return false;
            }
        });
    }

    private void setUpViewPager(ViewPager viewPager) {
        MainFragmentPagerAdapter fragmentPagerAdapter
                = new MainFragmentPagerAdapter(getSupportFragmentManager());
        fragmentPagerAdapter.addFragment(new ScheduledMessagesFragment(),
                                         getString(R.string.scheduled_tab_title));
//        // TODO add Sent and Failed fragments
//        fragmentPagerAdapter.addFragment();
//        fragmentPagerAdapter.addFragment();
        mViewPager.setAdapter(fragmentPagerAdapter);
    }


}
