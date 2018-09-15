package cs371m.hermes.futuremessenger.persistence.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = "id")
@Entity(tableName = "recipients", indices = {
        @Index(value = "id", unique = true),
        @Index(value = {"name", "phone_number"}, unique = true)
})

public class Recipient implements Serializable {

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

}
