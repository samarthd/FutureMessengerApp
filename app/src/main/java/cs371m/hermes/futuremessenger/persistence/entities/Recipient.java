package cs371m.hermes.futuremessenger.persistence.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Holds information about a particular recipient.
 * <p>
 * Recipients remain in the database so long as there is at least one message
 * associated with them. Once the last message is deleted, the recipient is
 * cleared along with it.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = "id")
@Entity(tableName = "recipients", indices = {
        @Index(value = "id", unique = true),
        @Index(value = {"name", "phone_number"}, unique = true)
})
public class Recipient implements Serializable, Comparable {

    private static final long serialVersionUID = 1L;

    @PrimaryKey(autoGenerate = true)
    private Long id;

    /**
     * The phone number of the recipient.
     */
    @ColumnInfo(name = "phone_number")
    @NonNull
    private String phoneNumber;

    /**
     * The name of the recipient.
     * <p>
     * Collation is set to NOCASE to ignore case when comparing strings. The index will have the
     * same collation setting as is defined on the column.
     */
    @ColumnInfo(name = "name", collate = ColumnInfo.NOCASE)
    private String name;

    @Override
    public int compareTo(@NonNull Object o) {
        // Determine if these two items are the same already
        if (this.equals(o)) {
            return 0;
        }
        // If these items aren't the same, first sort by name, and then sort by phone number
        else {
            // If you enter this block, guaranteed not to return 0
            if (o instanceof Recipient) {
                int result = StringUtils.compareIgnoreCase(((Recipient) o).getName(), this.getName()) * -1;
                if (result == 0) {
                    result = StringUtils.compareIgnoreCase(((Recipient) o).getPhoneNumber(),
                            this.getPhoneNumber());
                }
                return result;
            } else {
                return -1;
            }
        }
    }
}
