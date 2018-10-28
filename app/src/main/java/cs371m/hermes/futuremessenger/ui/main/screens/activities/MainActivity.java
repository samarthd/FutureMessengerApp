package cs371m.hermes.futuremessenger.ui.main.screens.activities;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import org.apache.commons.lang3.ArrayUtils;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.ui.edit.screens.activities.EditTextMessageActivity;
import cs371m.hermes.futuremessenger.ui.main.screens.fragments.FailedMessagesFragment;
import cs371m.hermes.futuremessenger.ui.main.screens.fragments.ScheduledMessagesFragment;
import cs371m.hermes.futuremessenger.ui.main.screens.fragments.SentMessagesFragment;
import cs371m.hermes.futuremessenger.ui.main.support.adapters.MainFragmentPagerAdapter;

import static android.Manifest.permission.SEND_SMS;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String NOTIFICATION_CHANNEL_ID =
            "cs371m.hermes.futuremessenger.sent_notification_channel";

    public static final String BUNDLE_KEY_TAB_TO_SELECT = "tab_to_select";
    public static final int SCHEDULED_TAB_INDEX = 1;
    public static final int SENT_TAB_INDEX = 0;
    public static final int FAILED_TAB_INDEX = 2;


    public static final int REQUEST_SEND_SMS_PERMISSION = 9000;

    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private SpeedDialView mSpeedDialView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        mViewPager = findViewById(R.id.main_view_pager);
        setUpViewPager(mViewPager);

        mTabLayout = findViewById(R.id.main_tab_layout);
        mTabLayout.setupWithViewPager(mViewPager);
        int tabToSelect = getTabToSelect();
        mTabLayout.getTabAt(tabToSelect).select();


        setUpFloatingActionMenu();
        setUpNotificationChannel();

        checkForPermissionsAndRequestIfNecessary();

    }

    private int getTabToSelect() {
        int tabToSelect = SCHEDULED_TAB_INDEX;
        if (getIntent().getExtras() != null) {
            tabToSelect = getIntent().getIntExtra(BUNDLE_KEY_TAB_TO_SELECT, SCHEDULED_TAB_INDEX);
        }
        return tabToSelect;
    }

    private void checkForPermissionsAndRequestIfNecessary() {

        if (ContextCompat.checkSelfPermission(this, SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // TODO figure out a way to make some sort of dialog to explain why permission is needed, as toasts don't show up when permission dialog is up
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, SEND_SMS)) {
//                Toast.makeText(this, R.string.request_send_sms_explanation, Toast.LENGTH_LONG).show();
//            }
            ActivityCompat.requestPermissions(this, new String[]{SEND_SMS}, REQUEST_SEND_SMS_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_SEND_SMS_PERMISSION) {
            int indexOfSmsPermission = ArrayUtils.indexOf(grantResults, REQUEST_SEND_SMS_PERMISSION);
            if (indexOfSmsPermission == -1 || grantResults[indexOfSmsPermission] != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, SEND_SMS)) {
                    checkForPermissionsAndRequestIfNecessary();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void setUpNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel_name);
            String description = getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(ContextCompat.getColor(this, R.color.colorQuinary));
            // TODO set sound

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }

    private void setUpFloatingActionMenu() {
        mSpeedDialView = findViewById(R.id.floating_action_menu);
        // The "New Text Message" option
        mSpeedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.schedule_new_message_floating_button, R.drawable.ic_text)
                        .setFabBackgroundColor(ContextCompat.getColor(this, R.color.colorQuinary))
                        .setLabel(getString(R.string.new_text_message_label))
                        .setLabelColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setLabelBackgroundColor(Color.WHITE)
                        .create());
        mSpeedDialView.setOnActionSelectedListener(actionItem -> {
            switch (actionItem.getId()) {
                case R.id.schedule_new_message_floating_button:
                    Intent intent = new Intent(this, EditTextMessageActivity.class);
                    startActivity(intent);
                    return false;
                default:
                    return false;
            }
        });
    }

    private void setUpViewPager(ViewPager viewPager) {
        MainFragmentPagerAdapter fragmentPagerAdapter
                = new MainFragmentPagerAdapter(getSupportFragmentManager());
        fragmentPagerAdapter.addFragment(new SentMessagesFragment(),
                getString(R.string.sent_tab_title));
        fragmentPagerAdapter.addFragment(new ScheduledMessagesFragment(),
                getString(R.string.scheduled_tab_title));
        fragmentPagerAdapter.addFragment(new FailedMessagesFragment(),
                getString(R.string.failed_tab_title));
        mViewPager.setAdapter(fragmentPagerAdapter);
    }


}
