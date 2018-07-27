package cs371m.hermes.futuremessenger.persistence.repositories;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Calendar;

import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.repositories.isolated.MessageDao;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MessageDaoTest {
    private MessageDao mMessageDao;
    private AppDatabase mDb;

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        mMessageDao = mDb.messageDao();
    }

    @After
    public void closeDb() throws IOException {
        mDb.close();
    }

    @Test
    public void writeMessageAndRead() {
        Message message = new Message();
        message.setTextContent("My message text content.");
        message.setScheduledDateTime(Calendar.getInstance());
        Long id = mMessageDao.createOrUpdateMessage(message);
        assertEquals(message, mMessageDao.findMessage(id));
    }
}
