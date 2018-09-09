package cs371m.hermes.futuremessenger.persistence.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Calendar;

import cs371m.hermes.futuremessenger.persistence.entities.embedded.Status;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = "id")
@Entity(tableName = "messages",
        indices = {
            @Index(value = "id", unique = true),
            @Index("status_code")
        })
public class Message implements Serializable{

    private static final long serialVersionUID = 1L;

    @PrimaryKey(autoGenerate = true)
    private Long id;

    /**
     * The scheduled date and time to send the message.
     */
    @ColumnInfo(name = "scheduled_datetime")
    @NonNull
    private Calendar scheduledDateTime = Calendar.getInstance();

    /**
     * The actual text content of the message.
     */
    @ColumnInfo(name = "text_content")
    @NonNull
    private String textContent;


    /**
     * Status information of a message.
     */
    @Embedded(prefix = "status_")
    private Status status;


}
