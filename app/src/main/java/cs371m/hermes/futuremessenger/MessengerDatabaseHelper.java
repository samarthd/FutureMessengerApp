package cs371m.hermes.futuremessenger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Samarth on 7/11/2016.
 *
 * Database for storage of messages and other information for Future Messenger.
 *
 */
public class MessengerDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "futuremessenger.db";

    //Names of columns in the Recipient table in the database.
    public static final String RECIPIENT_TABLE_NAME = "recipient_table";
    public static final String RECIPIENT_ID = "RID";
    public static final String RECIPIENT_PHONE_NUMBER = "PHONE_NUMBER";
    public static final String RECIPIENT_NAME = "NAME";

    // Names of various columns in the Message Table in the database.
    public static final String MESSAGE_TABLE_NAME = "message_table";
    public static final String MESSAGE_ID = "MID";
    public static final String MESSAGE_DATETIME = "DATETIME";
    public static final String MESSAGE_TXT_CONTENT = "TEXT_CONTENT";
    public static final String MESSAGE_IMG_PATH = "IMAGE_PATH";

    // Names of various columns in the Recipient-Message association
    // table in the database.
    public static final String REC_MESS_TABLE_NAME = "recipient_message_table";
    public static final String RECEP_ID = "RECIPIENT_ID";
    public static final String MESS_ID = "MESSAGE_ID";

 /*   // Names of various columns in the Preset table in the database.
    public static final String PRESET_TABLE_NAME = "preset_table";
    public static final String PRESET_ID = "ID";
    public static final String PRESET_NAME = "NAME";
    public static final String PRESET_CONTENT = "CONTENT";*/


    // Constructor.
    public MessengerDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        /*

           Perhaps we also need a column to determine whether the message will be
           sent as a group or individually.

           SHOULD BE FIXED LATER

           */

        // Create the Recipients table that will hold our recipients.
        db.execSQL("create table " + RECIPIENT_TABLE_NAME + "(" +
                    RECIPIENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    RECIPIENT_PHONE_NUMBER + " TEXT," +
                    RECIPIENT_NAME + " TEXT)");

        // Create the Message table that will hold our messages.
        db.execSQL("create table " + MESSAGE_TABLE_NAME + "(" +
                MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MESSAGE_DATETIME + " TEXT," +
                MESSAGE_TXT_CONTENT + " TEXT," +
                MESSAGE_IMG_PATH + " TEXT)");

         /* Create the Recipients_Messages table that will hold associations
            between Messages and Recipients. (A message can have many recipients
            and a recipient could have many associated messages.) */
        db.execSQL("create table " + REC_MESS_TABLE_NAME + "(" +
                    RECEP_ID + " INTEGER NOT NULL," +
                    MESS_ID + " INTEGER NOT NULL," +
                    "PRIMARY KEY(" + RECEP_ID + ", " + MESS_ID + "))");

/*        // Create the Preset table that will hold our presets.
        db.execSQL("create table " + PRESET_TABLE_NAME + "(" +
                PRESET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PRESET_NAME + " TEXT," +
                PRESET_CONTENT + " TEXT)");*/
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + RECIPIENT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + REC_MESS_TABLE_NAME);
        //db.execSQL("DROP TABLE IF EXISTS " + PRESET_TABLE_NAME);
        onCreate(db);
    }

    /* Store a new SMS message to be sent to one or more recipient phone numbers.
     * Returns true on success, false on failure. */
    public boolean storeNewSMS(String[] phoneNumbers, String dateTime, String message) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Store the message in the database.
        long message_id = storeNewMessage(dateTime, message);
        if (message_id == -1)
            return false;

        for (String phoneNumber : phoneNumbers) {
            //Store this recipient in the database.
            long recipient_id = storeRecipient(phoneNumber);

            //Store an association between this recipient and the message.
            ContentValues assocContentValues = new ContentValues();
            assocContentValues.put(RECEP_ID, recipient_id);
            assocContentValues.put(MESS_ID, message_id);
            long assoc_id = db.insert(REC_MESS_TABLE_NAME, null, assocContentValues);
            if (assoc_id == -1) {
                return false;
            }
        }
        return true;
    }

    /* This method takes data for a recipient, and attempts to insert that
       recipient into the database. If the phone number for that recipient
       already exists in a record, returns the existing recipient ID. Otherwise
       it will store a new recipient in the database and return the newly
       created recipient ID. If an error occurs, it will return -1.
     */
    private long storeRecipient(String phoneNumber) {
        SQLiteDatabase db = getWritableDatabase();

        // Search for this phone number in our database.
        Cursor cursor = db.query(RECIPIENT_TABLE_NAME, new String[] {RECIPIENT_PHONE_NUMBER},
                RECIPIENT_PHONE_NUMBER + "=?", new String[] {phoneNumber},null, null, null);

        // Variable used to store the current recipient id
        long recipient_id;
            /* If no matching phone number was found in our database, make a new entry
             * in the recipient table. */
        if (cursor.getCount() == 0) {
            // make new recipient
            ContentValues recipContentValues = new ContentValues();
            recipContentValues.put(RECIPIENT_PHONE_NUMBER, phoneNumber);
            recipient_id = db.insert(RECIPIENT_TABLE_NAME, null, recipContentValues);
        }
        else {
            // The recipient exists, so we can just pull his id from the database
            cursor.moveToFirst();
            recipient_id = cursor.getLong(0);
        }
        return recipient_id;
    }
    /* Stores a new message in the database, and returns its message id. If
       there was an error in storing the message, returns -1.
     */
    private long storeNewMessage(String dateTime, String message) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues msgContentValues = new ContentValues();
        msgContentValues.put(MESSAGE_TXT_CONTENT, message);
        msgContentValues.put(MESSAGE_DATETIME, dateTime);
        return db.insert(MESSAGE_TABLE_NAME, null, msgContentValues);
    }


    public Cursor getAllScheduledMessages() {
        SQLiteDatabase db = getWritableDatabase();


        // This will join the tables together and get many rows of messages, each that have a column
        // that has all of the phone numbers separated by commas in one big string.
        /*SELECT M.MID, M.MESSAGE_TXT_CONTENT, M.DATETIME, GROUP_CONCAT(R.RID) AS RECIPIENT_IDS,
        GROUP_CONCAT(R.RECIPIENT_PHONENUMBER) AS RECIPIENT_NUMBERS
        FROM MESSAGE_TABLE_NAME AS M, RECIPIENT_TABLE_NAME AS R, REC_MESS_TABLE_NAME AS RM
        LEFT OUTER JOIN RM ON RM.MESSAGE_ID=M.MID
        LEFT OUTER JOIN R ON RM.RECIPIENT_ID=R.RID
        GROUP BY M.MID, M.MESSAGE_TXT_CONTENT, M.DATETIME
        ORDER BY M.DATETIME*/

        String sql_select = "SELECT M." + MESSAGE_ID + " AS _ID, "+
                            "M." + MESSAGE_TXT_CONTENT + ", " +
                            "M." + MESSAGE_DATETIME + ", " +
                            "GROUP_CONCAT(R." + RECIPIENT_ID + ") AS RECIPIENT_IDS, " +
                            "GROUP_CONCAT(R." + RECIPIENT_PHONE_NUMBER + ") AS RECIPIENT_NUMBERS " +
                            "FROM " + MESSAGE_TABLE_NAME + " AS M, " + RECIPIENT_TABLE_NAME + " AS R, " +
                            REC_MESS_TABLE_NAME +
                            " LEFT OUTER JOIN " + REC_MESS_TABLE_NAME + " ON " + REC_MESS_TABLE_NAME + "." + MESS_ID + "=_ID" +
                            " LEFT OUTER JOIN R ON " + REC_MESS_TABLE_NAME + "." + RECEP_ID + "=R." + RECIPIENT_ID +
                            " GROUP BY _ID" + MESSAGE_ID + ", M." + MESSAGE_TXT_CONTENT + ", " +
                            "M." + MESSAGE_DATETIME + " ORDER BY M." + MESSAGE_DATETIME;
        Log.d("IN DB HELPER", sql_select);

        return db.rawQuery(sql_select, null);
    }


}
