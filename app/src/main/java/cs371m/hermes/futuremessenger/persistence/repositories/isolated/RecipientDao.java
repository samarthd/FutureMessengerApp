package cs371m.hermes.futuremessenger.persistence.repositories.isolated;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import cs371m.hermes.futuremessenger.persistence.entities.Recipient;

@Dao
public interface RecipientDao {

    /**
     * Will create a new recipient in the database if the database has no recipient with the same ID,
     * otherwise it will update the existing value with this ID.
     * @param recipient
     * @return The row ID of the created or updated item
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public Long createOrUpdateRecipient(Recipient recipient);

    /**
     * Finds the recipient with the given ID.
     * @param recipientID
     * @return The recipient object that was found (if any).
     */
    @Query("SELECT * FROM recipients WHERE id = :recipientID")
    public Recipient findRecipient(Long recipientID);

    /**
     * Finds all the recipients in the database.
     * @return The recipient objects found in the database.
     */
    @Query("SELECT * FROM recipients")
    public List<Recipient> findAllRecipients();

    /**
     * Deletes the row in the database which has a matching primary key.
     * @param recipient
     * @return A count of the number of rows which were deleted.
     */
    @Delete
    public int deleteRecipient(Recipient recipient);


}
