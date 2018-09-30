package cs371m.hermes.futuremessenger.ui.main.screens.activities;


import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
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

import java.lang.ref.WeakReference;
import java.util.Calendar;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.MessageRecipientJoin;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.entities.embedded.Status;
import cs371m.hermes.futuremessenger.persistence.repositories.MessageDao;
import cs371m.hermes.futuremessenger.persistence.repositories.MessageRecipientJoinDao;
import cs371m.hermes.futuremessenger.persistence.repositories.RecipientDao;
import cs371m.hermes.futuremessenger.ui.edit.screens.activities.EditTextMessageActivity;
import cs371m.hermes.futuremessenger.ui.main.screens.fragments.FailedMessagesFragment;
import cs371m.hermes.futuremessenger.ui.main.screens.fragments.ScheduledMessagesFragment;
import cs371m.hermes.futuremessenger.ui.main.screens.fragments.SentMessagesFragment;
import cs371m.hermes.futuremessenger.ui.main.support.adapters.MainFragmentPagerAdapter;

import static android.Manifest.permission.SEND_SMS;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String NOTIFICATION_CHANNEL_ID =
            "cs371m.hermes.futuremessenger.sent_notification_channel";

    public static final int REQUEST_SEND_SMS_PERMISSION = 9000;

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
        mTabLayout.getTabAt(1).select(); // index of scheduled


        setUpFloatingActionMenu();
        setUpNotificationChannel();

        checkForPermissionsAndRequestIfNecessary();

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
        }
        else {
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
                new SpeedDialActionItem.Builder(R.id.schedule_new_message_floating_button, R.drawable.text_icon)
                        .setFabBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setLabel(getString(R.string.new_text_message_label))
                        .setLabelColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setLabelBackgroundColor(Color.WHITE)
                        .create());
        mSpeedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.floating_action_menu_message_button, R.drawable.text_icon)
                        .setFabBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setLabel("SCHEDULED")
                        .setLabelColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setLabelBackgroundColor(Color.WHITE)
                        .create());
        mSpeedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.floating_action_menu_message_button2, R.drawable.text_icon)
                        .setFabBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setLabel("FAILED")
                        .setLabelColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setLabelBackgroundColor(Color.WHITE)
                        .create());
        mSpeedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.floating_action_menu_message_button3, R.drawable.text_icon)
                        .setFabBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setLabel("SENT")
                        .setLabelColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setLabelBackgroundColor(Color.WHITE)
                        .create());
        mSpeedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.floating_action_menu_message_button4, R.drawable.text_icon)
                        .setFabBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setLabel("DELETE SCHEDULED")
                        .setLabelColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setLabelBackgroundColor(Color.WHITE)
                        .create());
        mSpeedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.floating_action_menu_message_button5, R.drawable.text_icon)
                        .setFabBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setLabel("DELETE FAILED")
                        .setLabelColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setLabelBackgroundColor(Color.WHITE)
                        .create());
        mSpeedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.floating_action_menu_message_button6, R.drawable.text_icon)
                        .setFabBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setLabel("DELETE SENT")
                        .setLabelColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setLabelBackgroundColor(Color.WHITE)
                        .create());
        mSpeedDialView.setOnActionSelectedListener(actionItem -> {
            switch (actionItem.getId()) {
                case R.id.schedule_new_message_floating_button:
                    Intent intent = new Intent(this, EditTextMessageActivity.class);
                    startActivity(intent);
                    return false;
                case R.id.floating_action_menu_message_button:
                    //TODO Delete
                    new TempInsertTask(this).execute(Status.SCHEDULED);
                    return false;
                case R.id.floating_action_menu_message_button2:
                    new TempInsertTask(this).execute(Status.FAILED);
                    return false;
                case R.id.floating_action_menu_message_button3:
                    new TempInsertTask(this).execute(Status.SENT);
                    return false;
                case R.id.floating_action_menu_message_button4:
                    //TODO Delete
                    new TempDeleteTask(this).execute(Status.SCHEDULED);
                    return false;
                case R.id.floating_action_menu_message_button5:
                    new TempDeleteTask(this).execute(Status.FAILED);
                    return false;
                case R.id.floating_action_menu_message_button6:
                    new TempDeleteTask(this).execute(Status.SENT);
                    return false;
                default:
                    return false;
            }
        });
    }

    //TODO delete
    private static class TempInsertTask extends AsyncTask<String, Void, Integer> {

        private WeakReference<Activity> weakActivity;

        public TempInsertTask(Activity activity) {
            this.weakActivity = new WeakReference<>(activity);
        }

        @Override
        protected Integer doInBackground(String... voids) {
            insertManyMessages(10L, voids[0]);
            return null;
        }

        //TODO delete this
        private void insertManyMessages(Long j, String messageStatus) {
            AppDatabase db = AppDatabase.getInstance(weakActivity.get());
            MessageDao mDao = db.messageDao();
            RecipientDao rDao = db.recipientDao();
            MessageRecipientJoinDao mrjDao = db.messageRecipientJoinDao();
            Runnable create =
                    () -> {
                        for (long i = 0; i < j; i++) {
                            Message message = createMessageWithVal(i, messageStatus);
                            Long messageId = mDao.createOrUpdateMessage(message);
                            Recipient recipient = createRecipientWithVal(i);
                            Long recipientId = rDao.createOrUpdateRecipient(recipient);
                            MessageRecipientJoin join = new MessageRecipientJoin();
                            join.setRecipientID(recipientId);
                            join.setMessageID(messageId);
                            mrjDao.insert(join);

                            if (i % 2 == 0) {
                                Recipient recipient2 = createRecipientWithVal(i * 100000000);
                                Long recipientId2 = rDao.createOrUpdateRecipient(recipient2);
                                MessageRecipientJoin join2 = new MessageRecipientJoin();
                                join2.setRecipientID(recipientId2);
                                join2.setMessageID(messageId);
                                mrjDao.insert(join2);
                            }
                        }
                    };
            db.runInTransaction(create);
        }

        private Message createMessageWithVal(Long val, String messageStatus) {
            Message message = new Message();
            message.setTextContent("Text content " + val);
            Calendar calendar = Calendar.getInstance();
            calendar.set(2009, Calendar.SEPTEMBER, 30, 12, 59); // longest possible date time
            calendar.setTimeInMillis(calendar.getTimeInMillis() + val * 1000000000);
            message.setScheduledDateTime(calendar);
            cs371m.hermes.futuremessenger.persistence.entities.embedded.Status status = new cs371m.hermes.futuremessenger.persistence.entities.embedded.Status();
            status.setCode(messageStatus);
            message.setStatus(status);
            return message;
        }

        private Recipient createRecipientWithVal(Long val) {
            Recipient recipient = new Recipient();
            recipient.setName("Recipient name " + val);
            recipient.setPhoneNumber("Phone number " + val);
            return recipient;
        }
    }

    private static class TempDeleteTask extends AsyncTask<String, Void, Integer> {

        private WeakReference<Activity> weakActivity;

        public TempDeleteTask(Activity activity) {
            this.weakActivity = new WeakReference<>(activity);
        }

        @Override
        protected Integer doInBackground(String... voids) {
            deleteManyMessages(10L, voids[0]);
            return null;
        }

        //TODO delete this
        private void deleteManyMessages(Long j, String messageStatus) {
            AppDatabase db = AppDatabase.getInstance(weakActivity.get());
            MessageDao mDao = db.messageDao();
            RecipientDao rDao = db.recipientDao();
            MessageRecipientJoinDao mrjDao = db.messageRecipientJoinDao();
            mDao.deleteAllMessagesWithStatusCode(messageStatus);
        }
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
