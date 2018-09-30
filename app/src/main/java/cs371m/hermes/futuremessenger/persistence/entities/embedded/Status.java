package cs371m.hermes.futuremessenger.persistence.entities.embedded;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;
import android.support.annotation.NonNull;

import java.io.Serializable;

import cs371m.hermes.futuremessenger.persistence.pojo.StatusDetails;
import cs371m.hermes.futuremessenger.persistence.typeconverters.StatusDetailsConverter;
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
public class Status implements Serializable {

    private static final long serialVersionUID = 1L;

    @Ignore
    public static final String SCHEDULED = "SCHEDULED";

    @Ignore
    public static final String SENT = "SENT";

    @Ignore
    public static final String FAILED = "FAILED";


    @ColumnInfo(name = "code")
    @NonNull
    private String code;

    /**
     * @see StatusDetailsConverter
     */
    @ColumnInfo(name = "details", typeAffinity = ColumnInfo.BLOB)
    @NonNull
    private StatusDetails details;

}

