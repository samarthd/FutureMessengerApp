package cs371m.hermes.futuremessenger.persistence.repositories;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import cs371m.hermes.futuremessenger.persistence.entities.Message;

/**
 * Repository to manage {@link Message Messages}.
 */
@Dao
public interface MessageDao {

    /**
     * Will create a new message in the database if the database has no message with the same ID.
     *
     * @param message
     * @return The row ID of the created item
     */
    @Insert
    Long createMessage(Message message);

    /**
     * Will update a message in the database with the same ID.
     *
     * @param message
     * @return the number of updated rows
     */
    @Update
    int updateMessage(Message message);

    /**
     * Finds the message with the given ID.
     *
     * @param messageID the ID of the message to find
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
     * @return The message objects found in the database with that status code, ordered by
     * scheduled datetime in ascending order.
     */
    @Query("SELECT * FROM messages WHERE status_code = :statusCode ORDER BY scheduled_datetime ASC")
    List<Message> findAllMessagesWithStatusCodeSortAscending(String statusCode);

    /**
     * Finds all messages in the database with a particular status code (ex: "SCHEDULED")
     *
     * @return The message objects found in the database with that status code, ordered by
     * scheduled datetime in descending order.
     */
    @Query("SELECT * FROM messages WHERE status_code = :statusCode ORDER BY scheduled_datetime DESC")
    List<Message> findAllMessagesWithStatusCodeSortDescending(String statusCode);

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

}
