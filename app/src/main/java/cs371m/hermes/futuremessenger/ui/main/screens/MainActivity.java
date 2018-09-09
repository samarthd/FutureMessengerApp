package cs371m.hermes.futuremessenger.ui.main.screens;


import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.lang.ref.WeakReference;
import java.util.Calendar;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.entities.embedded.Status;
import cs371m.hermes.futuremessenger.persistence.entities.join.MessageRecipientJoin;
import cs371m.hermes.futuremessenger.persistence.repositories.isolated.MessageDao;
import cs371m.hermes.futuremessenger.persistence.repositories.isolated.RecipientDao;
import cs371m.hermes.futuremessenger.persistence.repositories.joined.MessageRecipientJoinDao;
import cs371m.hermes.futuremessenger.ui.main.adapters.MainFragmentPagerAdapter;
import cs371m.hermes.futuremessenger.ui.main.screens.fragments.FailedMessagesFragment;
import cs371m.hermes.futuremessenger.ui.main.screens.fragments.ScheduledMessagesFragment;
import cs371m.hermes.futuremessenger.ui.main.screens.fragments.SentMessagesFragment;

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
        mTabLayout.getTabAt(1).select(); // index of scheduled


        setUpFloatingActionMenu();
    }

    private void setUpFloatingActionMenu() {
        mSpeedDialView = findViewById(R.id.floating_action_menu);
        // The "New Text Message" option
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
            switch(actionItem.getId()) {
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
                        for(long i = 0; i < j; i++) {
                            Message message = createMessageWithVal(i, messageStatus);
                            Long messageId = mDao.createOrUpdateMessage(message);
                            Recipient recipient = createRecipientWithVal(i);
                            Long recipientId = rDao.createOrUpdateRecipient(recipient);
                            MessageRecipientJoin join = new MessageRecipientJoin();
                            join.setRecipientID(recipientId);
                            join.setMessageID(messageId);
                            mrjDao.insert(join);

                            if(i % 2 == 0) {
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
            status.setDescription("Status description " + val);
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
