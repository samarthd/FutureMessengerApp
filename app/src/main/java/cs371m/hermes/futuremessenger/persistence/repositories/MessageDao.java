package cs371m.hermes.futuremessenger.persistence.repositories;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import cs371m.hermes.futuremessenger.persistence.entities.Message;

@Dao
public interface MessageDao {

    /**
     * Will create a new message in the database if the database has no message with the same ID,
     * otherwise it will update the existing value with this ID.
     *
     * @param message
     * @return The row ID of the created or updated item
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long createOrUpdateMessage(Message message);

    /**
     * Finds the message with the given ID.
     *
     * @param messageID
     * @return The message object that was found (if any).
     */
    @Query("SELECT * FROM messages WHERE id = :messageID")
    Message findMessage(Long messageID);

    /**
     * Finds all the messages in the database.
     *
     * @return The message objects found in the database.
     */
    @Query("SELECT * FROM messages")
    List<Message> findAllMessages();

    /**
     * Finds all messages in the database with a particular status code (ex: "SCHEDULED")
     *
     * @return The message objects found in the database with that status code.
     */
    @Query("SELECT * FROM messages WHERE status_code = :statusCode ORDER BY scheduled_datetime ASC")
    List<Message> findAllMessagesWithStatusCode(String statusCode);

    /**
     * Deletes the row in the database which has a matching primary key.
     *
     * @param message
     * @return A count of the number of rows which were deleted.
     */
    @Delete
    int deleteMessage(Message message);

    /**
     * Deletes the message in the database with the given ID
     *
     * @param messageID the ID of the message to delete
     * @return A count of the number of rows which were deleted.
     */
    @Query("DELETE FROM messages WHERE id = :messageID")
    int deleteMessageByID(Long messageID);


    @Query("DELETE FROM messages WHERE messages.status_code = :statusCode")
    int deleteAllMessagesWithStatusCode(String statusCode);

}
