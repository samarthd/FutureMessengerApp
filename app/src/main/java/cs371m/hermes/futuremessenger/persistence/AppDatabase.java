package cs371m.hermes.futuremessenger.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.entities.embedded.Status;
import cs371m.hermes.futuremessenger.persistence.repositories.isolated.MessageDao;
import cs371m.hermes.futuremessenger.persistence.repositories.isolated.RecipientDao;
import cs371m.hermes.futuremessenger.persistence.repositories.joined.MessageRecipientJoinDao;
import cs371m.hermes.futuremessenger.persistence.typeconverters.CalendarConverter;

/**
 * @author samarthd
 */
@Database(version = 1, entities = {Message.class, Recipient.class, Status.class})
@TypeConverters({CalendarConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    abstract public MessageDao messageDao();

    abstract public RecipientDao recipientDao();

    abstract public MessageRecipientJoinDao messageRecipientJoinDao();
}
