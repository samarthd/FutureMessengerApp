package cs371m.hermes.futuremessenger.persistence.entities.join;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.support.annotation.NonNull;

import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity(tableName = "messages_recipients_join",
        primaryKeys =  {"message_id", "recipient_id"},
        foreignKeys = {
            @ForeignKey(entity = Message.class,
                        parentColumns = "id",
                        childColumns = "message_id",
                        onUpdate = ForeignKey.CASCADE,
                        onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = Recipient.class,
                        parentColumns = "id",
                        childColumns = "recipient_id",
                        onUpdate = ForeignKey.CASCADE,
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
            @Index(value = "message_id"),
            @Index(value = "recipient_id")
        })
public class MessageRecipientJoin {

    @ColumnInfo(name = "message_id")
    @NonNull
    private Long messageID;

    @ColumnInfo(name = "recipient_id")
    @NonNull
    private Long recipientID;
}
