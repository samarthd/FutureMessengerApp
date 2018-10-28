package cs371m.hermes.futuremessenger.persistence.repositories;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import cs371m.hermes.futuremessenger.persistence.entities.Recipient;

/**
 * Repository to manage {@link Recipient}.
 */
@Dao
public interface RecipientDao {

    /**
     * Will create a new recipient in the database if the database has no recipient with the same ID.
     *
     * @param recipient
     * @return The row ID of the created item
     */
    @Insert
    Long createRecipient(Recipient recipient);

    /**
     * Will update a recipient in the database with the same ID.
     *
     * @param recipient
     * @return the number of updated rows
     */
    @Update
    int updateRecipient(Recipient recipient);

    /**
     * Finds the recipient with the given ID.
     *
     * @param recipientID
     * @return The recipient object that was found (if any).
     */
    @Query("SELECT * FROM recipients WHERE id = :recipientID")
    Recipient findRecipient(Long recipientID);

    /**
     * Finds recipients matching the given name and phone number.
     * <p>
     * Collation is set to NOCASE for the name column, so case sensitivity shouldn't matter
     * unless the contact name has non-ASCII characters...and in that case I think it's okay
     * to just have a mismatch and create a new entry.
     */
    @Query("SELECT * FROM recipients WHERE recipients.name = :name AND recipients.phone_number = :phoneNumber")
    Recipient findRecipientMatchingValues(String name, String phoneNumber);

    /**
     * Finds all the recipients in the database.
     *
     * @return The recipient objects found in the database.
     */
    @Query("SELECT * FROM recipients")
    List<Recipient> findAllRecipients();

    /**
     * Deletes the row in the database which has a matching primary key.
     *
     * @param recipient
     * @return A count of the number of rows which were deleted.
     */
    @Delete
    int deleteRecipient(Recipient recipient);


}
