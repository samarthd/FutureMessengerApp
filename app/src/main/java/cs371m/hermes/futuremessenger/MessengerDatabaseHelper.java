package cs371m.hermes.futuremessenger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Samarth on 7/11/2016.
 *
 * Database for storage of messages and other information for Future Messenger.
 *
 */
public class MessengerDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "futuremessenger.db";

    // Names of various columns in the Message Table in the database.
    public static final String MESSAGE_TABLE_NAME = "message_table";
    public static final String MESSAGE_ID = "ID";
    public static final String MESSAGE_RECIPIENTS = "RECIPIENTS";
    public static final String MESSAGE_TIME = "TIME";
    public static final String MESSAGE_DATE = "DATE";
    public static final String MESSAGE_TXT_CONTENT = "TEXT_CONTENT";
    public static final String MESSAGE_IMG_PATH = "IMAGE_PATH";

    // Names of various columns in the Preset table in the database.
    public static final String PRESET_TABLE_NAME = "preset_table";
    public static final String PRESET_ID = "ID";
    public static final String PRESET_NAME = "NAME";
    public static final String PRESET_CONTENT = "CONTENT";


    // Constructor.
    public MessengerDatabaseHelper(Context context, String name,
                                   SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        /* Normally, we would place the datatypes of the columns in this string, but
           it's unclear at the time what these types would be, so I'll leave it
           just plain text for all of them at the moment.

           Perhaps we also need a column to determine whether the message will be
           sent as a group or individually.

           SHOULD BE FIXED LATER

           */
        // Create the Message table that will hold our messages.
        db.execSQL("create table " + MESSAGE_TABLE_NAME + "(" +
                MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MESSAGE_TIME + " TEXT," +
                MESSAGE_DATE + " TEXT," +
                MESSAGE_RECIPIENTS + " TEXT," +
                MESSAGE_TXT_CONTENT + " TEXT," +
                MESSAGE_IMG_PATH + " TEXT)");

        // Create the Preset table that will hold our presets.
        db.execSQL("create table " + PRESET_TABLE_NAME + "(" +
                PRESET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PRESET_NAME + " TEXT," +
                PRESET_CONTENT + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PRESET_TABLE_NAME);
        onCreate(db);
    }
}
