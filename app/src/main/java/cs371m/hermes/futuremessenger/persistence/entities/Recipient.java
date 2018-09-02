package cs371m.hermes.futuremessenger.persistence.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
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
@Entity(tableName = "recipients")
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
     */
    @ColumnInfo(name = "name")
    private String name;

    // Maybe a contact photo URI?

}
