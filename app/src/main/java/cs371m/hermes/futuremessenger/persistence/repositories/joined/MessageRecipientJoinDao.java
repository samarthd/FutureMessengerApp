package cs371m.hermes.futuremessenger.persistence.repositories.joined;

import android.arch.persistence.room.Dao;
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
     * @return The number of rows that were inserted.
     */
    @Insert
    public Long insert(MessageRecipientJoin messageRecipientJoin);

    /**
     * Get all of the messages for a particular recipient.
     * @param recipientID
     */
    @Query("SELECT messages.id, messages.text_content, messages.scheduled_datetime, " +
                  "messages.status_code, messages.status_description " +
            "FROM messages " +
            "INNER JOIN messages_recipients_join as m_r_join ON messages.id = m_r_join.message_id " +
            "WHERE m_r_join.recipient_id = :recipientID")
    public List<Message> getMessagesForRecipient(Long recipientID);

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
    @Query("SELECT recipients.id, recipients.name, recipients.phone_number " +
            "FROM recipients " +
            "INNER JOIN messages_recipients_join as m_r_join ON recipients.id = m_r_join.recipient_id " +
            "WHERE m_r_join.message_id = :messageID")
    public List<Recipient> getRecipientsForMessage(Long messageID);



}
