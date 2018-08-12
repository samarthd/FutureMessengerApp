package cs371m.hermes.futuremessenger.ui.main;


import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.os.AsyncTask;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.entities.embedded.Status;
import cs371m.hermes.futuremessenger.persistence.entities.join.MessageRecipientJoin;
import cs371m.hermes.futuremessenger.persistence.repositories.isolated.MessageDao;
import cs371m.hermes.futuremessenger.persistence.repositories.isolated.RecipientDao;
import cs371m.hermes.futuremessenger.persistence.repositories.joined.MessageRecipientJoinDao;

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
                    //TODO Delete
                    new TempInsertTask(this).execute();

                    return false;
                default:
                    return false;
            }
        });
    }

    //TODO delete
    private static class TempInsertTask extends AsyncTask<Void, Void, Integer> {

        private WeakReference<Activity> weakActivity;
        public TempInsertTask(Activity activity) {
            this.weakActivity = new WeakReference<>(activity);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            insertManyMessages(10L);
            return null;
        }

        //TODO delete this
        private void insertManyMessages(Long j) {
            AppDatabase db = AppDatabase.getInstance(weakActivity.get());
            MessageDao mDao = db.messageDao();
            RecipientDao rDao = db.recipientDao();
            MessageRecipientJoinDao mrjDao = db.messageRecipientJoinDao();
            for(long i = 0; i < j; i++) {
                Message message = createMessageWithVal(i);
                Long messageId = mDao.createOrUpdateMessage(message);
                Recipient recipient = createRecipientWithVal(i);
                Long recipientId = rDao.createOrUpdateRecipient(recipient);
                MessageRecipientJoin join = new MessageRecipientJoin();
                join.setRecipientID(recipientId);
                join.setMessageID(messageId);
                mrjDao.insert(join);

                if(i % 2 == 0) {
                    Recipient recipient2 = createRecipientWithVal(i * 10000);
                    Long recipientId2 = rDao.createOrUpdateRecipient(recipient2);
                    MessageRecipientJoin join2 = new MessageRecipientJoin();
                    join2.setRecipientID(recipientId2);
                    join2.setMessageID(messageId);
                    mrjDao.insert(join2);
                }
            }
        }
        private Message createMessageWithVal(Long val) {
            Message message = new Message();
            message.setTextContent("Text content " + val);
            Calendar calendar = Calendar.getInstance();
            calendar.set(2009, Calendar.SEPTEMBER, 30, 12, 59); // longest possible date time
            message.setScheduledDateTime(calendar);
            cs371m.hermes.futuremessenger.persistence.entities.embedded.Status status = new cs371m.hermes.futuremessenger.persistence.entities.embedded.Status();
            status.setCode(cs371m.hermes.futuremessenger.persistence.entities.embedded.Status.SCHEDULED);
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
