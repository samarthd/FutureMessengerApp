package cs371m.hermes.futuremessenger.persistence.entities.embedded;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Object representing the status information of a message. These columns are embedded into
 * the messages table. See {@link cs371m.hermes.futuremessenger.persistence.entities.Message}.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Status {

    @Ignore
    public static final String SCHEDULED = "SCHEDULED";

    @Ignore
    public static final String SENT = "SENT";

    @Ignore
    public static final String FAILED = "FAILED";


    @ColumnInfo(name = "code")
    @NonNull
    private String code;

    @ColumnInfo(name = "description")
    @NonNull
    private String description;

}

