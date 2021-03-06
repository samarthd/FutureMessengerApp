package cs371m.hermes.futuremessenger.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.MessageRecipientJoin;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.repositories.MessageDao;
import cs371m.hermes.futuremessenger.persistence.repositories.MessageRecipientJoinDao;
import cs371m.hermes.futuremessenger.persistence.repositories.RecipientDao;
import cs371m.hermes.futuremessenger.persistence.typeconverters.CalendarConverter;
import cs371m.hermes.futuremessenger.persistence.typeconverters.StatusDetailsConverter;

/**
 * Database for the application.
 */
@Database(version = 1, entities = {Message.class, Recipient.class, MessageRecipientJoin.class},
        exportSchema = false)
@TypeConverters({CalendarConverter.class, StatusDetailsConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    public static final String APP_DATABASE_NAME = "future_messenger_db";

    abstract public MessageDao messageDao();

    abstract public RecipientDao recipientDao();

    abstract public MessageRecipientJoinDao messageRecipientJoinDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            // We will guarantee that we are always passing in the Application context
            INSTANCE = buildInstance(context.getApplicationContext());
        }
        return INSTANCE;
    }

    private static AppDatabase buildInstance(Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, AppDatabase.APP_DATABASE_NAME).build();
    }

}
