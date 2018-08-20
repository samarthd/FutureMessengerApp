package cs371m.hermes.futuremessenger.persistence.repositories.joined;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.entities.join.MessageRecipientJoin;

@Dao
public interface MessageRecipientJoinDao {

    /**
     * Insert a row into this table and create an association between a Message and a Recipient.
     * @param messageRecipientJoin
     */
    @Insert
    public void insert(MessageRecipientJoin messageRecipientJoin);

    /**
     * Get all of the messages for a particular recipient.
     * @param recipientID
     */
    @Query("SELECT messages.* " +
            "FROM messages " +
            "INNER JOIN messages_recipients_join as m_r_join ON messages.id = m_r_join.message_id " +
            "WHERE m_r_join.recipient_id = :recipientID")
    public List<Message> findMessagesForRecipient(Long recipientID);

    /**
     * Returns a count of the number of messages a recipient is currently associated with.
     * @param recipientID
     */
    @Query("SELECT COUNT(*) FROM messages_recipients_join as m_r_join " +
            "WHERE m_r_join.recipient_id = :recipientID")
    public Long getCountOfMessagesForRecipient(Long recipientID);

    /**
     * Get all of the recipients for a particular message.
     * @param messageID
     */
    @Query("SELECT recipients.* " +
            "FROM recipients " +
            "INNER JOIN messages_recipients_join as m_r_join ON recipients.id = m_r_join.recipient_id " +
            "WHERE m_r_join.message_id = :messageID " +
            "ORDER BY recipients.name ASC")
    public List<Recipient> findRecipientsForMessage(Long messageID);

    /**
     * Observable version of the find recipients for message query.
     * Get all of the recipients for a particular message.
     * @param messageID
     */
    @Query("SELECT recipients.* " +
            "FROM recipients " +
            "INNER JOIN messages_recipients_join as m_r_join ON recipients.id = m_r_join.recipient_id " +
            "WHERE m_r_join.message_id = :messageID " +
            "ORDER BY recipients.name ASC")
    public LiveData<List<Recipient>> observableFindRecipientsForMessage(Long messageID);

    /**
     * Get all of the rows in the table.
     */
    @Query("SELECT * FROM messages_recipients_join")
    public List<MessageRecipientJoin> findAllRelationships();

    /**
     * Delete a particular row in the table.
     */
    @Delete
    public int deleteRelationship(MessageRecipientJoin messageRecipientJoin);

    /**
     * Delete all relationships for the given message.
     */
    @Query("DELETE FROM messages_recipients_join WHERE message_id = :messageId")
    public int deleteRelationshipsForMessage(Long messageId);


}
