package cs371m.hermes.futuremessenger.persistence.repositories;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.entities.embedded.Status;
import cs371m.hermes.futuremessenger.persistence.entities.join.MessageRecipientJoin;
import cs371m.hermes.futuremessenger.persistence.repositories.isolated.MessageDao;
import cs371m.hermes.futuremessenger.persistence.repositories.isolated.RecipientDao;
import cs371m.hermes.futuremessenger.persistence.repositories.joined.MessageRecipientJoinDao;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DaoTests {

    private MessageDao mMessageDao;
    private RecipientDao mRecipientDao;
    private MessageRecipientJoinDao mMessageRecipientJoinDao;
    private AppDatabase mDb;

    private static final Logger LOG = Logger.getLogger("DAO TESTS");

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        mMessageDao = mDb.messageDao();
        mRecipientDao = mDb.recipientDao();
        mMessageRecipientJoinDao = mDb.messageRecipientJoinDao();
    }

    @After
    public void closeDb() throws IOException {
        mDb.close();
    }

    Calendar createAndInitializeCalendar(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        return calendar;
    }

    Status createAndInitializeStatus(String code, String description) {
        Status status = new Status();
        status.setCode(code);
        status.setDescription(description);
        return status;
    }

    Message createAndInitializeMessage(String textContent, Calendar scheduledDateTime, Status status) {
        Message message = new Message();
        message.setTextContent(textContent);
        message.setScheduledDateTime(scheduledDateTime);
        message.setStatus(status);
        return message;
    }

    Recipient createAndInitializeRecipient(String name, String phoneNumber) {
        Recipient recipient = new Recipient();
        recipient.setName(name);
        recipient.setPhoneNumber(phoneNumber);
        return recipient;
    }

    MessageRecipientJoin createAndInitializeMessageRecipientJoin(Long messageID, Long recipientID) {
        MessageRecipientJoin messageRecipientJoin = new MessageRecipientJoin();
        messageRecipientJoin.setMessageID(messageID);
        messageRecipientJoin.setRecipientID(recipientID);
        return messageRecipientJoin;
    }

    /**
     * Creates and inserts a message, a recipient, and their association in the database the given
     * number of times.
     * @param numberOfTriplesToGenerate
     */
    void generateAndInsertMessageAndRecipientRelationships(int numberOfTriplesToGenerate) {
        for (int i = 0; i < numberOfTriplesToGenerate; i++) {
            Status status = createAndInitializeStatus("Status Code " + i, "Status Description " + i);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(i);

            Message message = createAndInitializeMessage("Message Content " + i, calendar, status);
            Recipient recipient = createAndInitializeRecipient("Recipient " + i, "Phone number " + i);
            Long messageID = mMessageDao.createOrUpdateMessage(message);
            Long recipientID = mRecipientDao.createOrUpdateRecipient(recipient);

            MessageRecipientJoin messageRecipientJoin = createAndInitializeMessageRecipientJoin(messageID, recipientID);
            mMessageRecipientJoinDao.insert(messageRecipientJoin);
        }
    }

    @Test
    public void testMessageCrud() {

        // Test creating a message
        Calendar calendar = createAndInitializeCalendar(1);
        Status status = createAndInitializeStatus(Status.SCHEDULED, "Description of status.");
        Message message = createAndInitializeMessage("The message text.", calendar, status);

        Long id = mMessageDao.createOrUpdateMessage(message);
        Message returnedMessage = mMessageDao.findMessage(id);
        assertEquals(message, returnedMessage);
        assertEquals(id, returnedMessage.getId());
        assertEquals(message, mMessageDao.findAllMessages().get(0));

        // Test updating a message
        Status updatedStatus = createAndInitializeStatus(Status.FAILED, "Updated description");
        Calendar updatedCalendar = createAndInitializeCalendar(2);
        String updatedText = "Updated Text";

        Message updatedMessage = createAndInitializeMessage(updatedText, updatedCalendar, updatedStatus);
        updatedMessage.setId(id);

        Long updatedId = mMessageDao.createOrUpdateMessage(updatedMessage);
        assertEquals(id, updatedId);
        assertEquals(updatedId, updatedMessage.getId());
        assertEquals(updatedMessage, mMessageDao.findMessage(updatedId));
        assertEquals(updatedMessage, mMessageDao.findAllMessages().get(0));

        // Test deleting a message
        assertEquals(0, mMessageDao.deleteMessage(message));
        assertEquals(1, mMessageDao.deleteMessage(updatedMessage));
        assertEquals(0, mMessageDao.findAllMessages().size());

    }

    @Test
    public void testRecipientCrud() {
        Recipient recipient = createAndInitializeRecipient("Recipient Name", "phone number");
        Long id = mRecipientDao.createOrUpdateRecipient(recipient);
        Recipient returnedRecipient = mRecipientDao.findRecipient(id);
        assertEquals(recipient, returnedRecipient);
        assertEquals(id, returnedRecipient.getId());
        assertEquals(recipient, mRecipientDao.findAllRecipients().get(0));

        Recipient updatedRecipient = createAndInitializeRecipient("Updated Recipient Name", "Updated phone number");
        updatedRecipient.setId(id);

        Long updatedId = mRecipientDao.createOrUpdateRecipient(updatedRecipient);
        assertEquals(id, updatedId);
        assertEquals(updatedId, updatedRecipient.getId());
        assertEquals(updatedRecipient, mRecipientDao.findRecipient(updatedId));
        assertEquals(updatedRecipient, mRecipientDao.findAllRecipients().get(0));

        // Test deleting a message
        assertEquals(0, mRecipientDao.deleteRecipient(recipient));
        assertEquals(1, mRecipientDao.deleteRecipient(updatedRecipient));
        assertEquals(0, mRecipientDao.findAllRecipients().size());
    }


    @Test
    public void testMessageRecipientRelationship() {
        Calendar calendar = createAndInitializeCalendar(1);
        Status status = createAndInitializeStatus(Status.SCHEDULED, "Description");

        Message message = createAndInitializeMessage("Content", calendar, status);
        Recipient recipient = createAndInitializeRecipient("Name", "Phone");


        // insert message, recipient, and relationship (test if changing order matters)
        Long messageID = mMessageDao.createOrUpdateMessage(message);
        Long recipientID = mRecipientDao.createOrUpdateRecipient(recipient);

        // create relationship
        MessageRecipientJoin messageRecipientJoin =
                createAndInitializeMessageRecipientJoin(messageID,recipientID);

        // sanity check
        assertEquals(0, mMessageRecipientJoinDao.findRecipientsForMessage(messageID).size());
        assertEquals(0, mMessageRecipientJoinDao.findMessagesForRecipient(recipientID).size());

        mMessageRecipientJoinDao.insert(messageRecipientJoin);
        assertEquals(1, mMessageRecipientJoinDao.findRecipientsForMessage(messageID).size());
        assertEquals(1, mMessageRecipientJoinDao.findMessagesForRecipient(recipientID).size());

        assertEquals(recipient, mMessageRecipientJoinDao.findRecipientsForMessage(messageID).get(0));
        assertEquals(message, mMessageRecipientJoinDao.findMessagesForRecipient(recipientID).get(0));


        //update, delete
        // what happens if you delete a message only?
        // what happens if you delete a recipient only?
        // what happens if you delete both at once?
        // what happens if you delete the relationship but not the values?
        // confirm that mutating any non-ID values will not change the relationship
        generateAndInsertMessageAndRecipientRelationships(10);


    }


}
