package cs371m.hermes.futuremessenger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
    public static final String MESSAGE_FORMATTED_DT = "FORMATTED_DATETIME";
    public static final String MESSAGE_TXT_CONTENT = "TEXT_CONTENT";
    public static final String MESSAGE_IMG_PATH = "IMAGE_PATH";
    public static final String MESSAGE_GROUP = "GROUP_FLAG";

    public static final int NOT_GROUP_MESSAGE = 0;
    public static final int IS_GROUP_MESSAGE = 1;

    // Names of various columns in the Recipient-Message association
    // table in the database.
    public static final String REC_MESS_TABLE_NAME = "recipient_message_table";
    public static final String RECIP_ID = "RECIPIENT_ID";
    public static final String MESS_ID = "MESSAGE_ID";



    // Names of various columns in the Preset table in the database.
    public static final String PRESET_TABLE_NAME = "preset_table";
    public static final String PRESET_ID = "ID";
    public static final String PRESET_NAME = "NAME";
    public static final String PRESET_CONTENT = "CONTENT";


    private static final String TAG = "IN DATABASE HELPER";

    // Constructor.
    public MessengerDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        /* TODO:
           For beta, we also need a column to determine whether the message will be
           sent as a group or individually.*/

        // Create the Recipients table that will hold our recipients.
        final String createRecipTable =
                "create table " + RECIPIENT_TABLE_NAME + "(" +
                RECIPIENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                RECIPIENT_PHONE_NUMBER + " TEXT," +
                RECIPIENT_NAME + " TEXT)";
        Log.d(TAG, "CREATING RECIP TABLE: " + createRecipTable);
        db.execSQL(createRecipTable);


        // Create the Message table that will hold our messages.
        final String createMessTable =
                "create table " + MESSAGE_TABLE_NAME + "(" +
                MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MESSAGE_DATETIME + " TEXT," +
                MESSAGE_FORMATTED_DT + " TEXT," +
                MESSAGE_TXT_CONTENT + " TEXT," +
                MESSAGE_IMG_PATH + " TEXT," +
                MESSAGE_GROUP + " INTEGER)";
        Log.d(TAG, "CREATING MESSAGE TABLE: " + createMessTable);
        db.execSQL(createMessTable);

         /* Create the Recipients_Messages table that will hold associations
            between Messages and Recipients. (A message can have many recipients
            and a recipient could have many associated messages.) */
        final String createRecMessTable =
                "create table " + REC_MESS_TABLE_NAME + "(" +
                RECIP_ID + " INTEGER NOT NULL," +
                MESS_ID + " INTEGER NOT NULL," +
                "PRIMARY KEY(" + RECIP_ID + ", " + MESS_ID + "))";

        Log.d(TAG, "CREATING ASSOC TABLE: " + createRecMessTable);
        db.execSQL(createRecMessTable);

       // Create the Preset table that will hold our presets.
        db.execSQL("create table " + PRESET_TABLE_NAME + "(" +
                PRESET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PRESET_NAME + " TEXT," +
                PRESET_CONTENT + " TEXT)");
        Log.d(TAG, "CREATING PRESET TABLE");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + RECIPIENT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + REC_MESS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PRESET_TABLE_NAME);
        onCreate(db);
    }

    /* Store a new message to be sent to one or more recipient phone numbers.
     * Returns ID of the message on success, -1 on failure. */
    public long storeNewMessage(ArrayList<Contact> recipients_list, String dateTime, String message,
                                 String image_path, int group_message) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Store the message in the database.
        long message_id = storeMessageData(dateTime, message, image_path, group_message);
        if (message_id == -1)
            return -1;

        for (Contact recipient : recipients_list) {
            //Store this recipient in the database.
            long recipient_id = storeRecipient(recipient);

            //Store an association between this recipient and the message.
            ContentValues assocContentValues = new ContentValues();
            assocContentValues.put(RECIP_ID, recipient_id);
            assocContentValues.put(MESS_ID, message_id);
            long assoc_id = db.insert(REC_MESS_TABLE_NAME, null, assocContentValues);
            if (assoc_id == -1) {
                return -1;
            }
        }
        return message_id;
    }

    /* This method takes data for a recipient, and attempts to insert that
       recipient into the database. If the phone number for that recipient
       already exists in a record, returns the existing recipient ID. Otherwise
       it will store a new recipient in the database and return the newly
       created recipient ID. If an error occurs, it will return -1.
     */
    private long storeRecipient(Contact recipient) {
        SQLiteDatabase db = getWritableDatabase();

        String phoneNumber = recipient.getPhoneNum();
        String name = recipient.getName();
        // Search for this phone number in our database.
        Cursor cursor = db.query(RECIPIENT_TABLE_NAME, new String[] {RECIPIENT_ID},
                RECIPIENT_PHONE_NUMBER + "=?", new String[] {phoneNumber},null, null, null);

        // Variable used to store the current recipient id
        long recipient_id;
            /* If no matching phone number was found in our database, make a new entry
             * in the recipient table. */
        if (cursor.getCount() == 0) {
            // make new recipient
            ContentValues recipContentValues = new ContentValues();
            Log.d("Store Recipient", "Name = " + name);
            Log.d("Store Recipient", "Number = " + phoneNumber);
            recipContentValues.put(RECIPIENT_NAME, name);
            recipContentValues.put(RECIPIENT_PHONE_NUMBER, phoneNumber);
            recipient_id = db.insert(RECIPIENT_TABLE_NAME, null, recipContentValues);
        }
        else {
            // The recipient exists, so we can just pull his id from the database
            cursor.moveToFirst();
            recipient_id = cursor.getLong(0);
            Log.d(TAG, "RECIPIENT EXISTS, ID is: " + recipient_id);
        }
        return recipient_id;
    }
    /* Stores data about a message in the database, and returns its message id. If
       there was an error in storing the message, returns -1.
     */
    private long storeMessageData(String dateTime, String message, String image_path,
                                  int group_flag) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues msgContentValues = new ContentValues();
        msgContentValues.put(MESSAGE_TXT_CONTENT, message);
        msgContentValues.put(MESSAGE_DATETIME, dateTime);
        msgContentValues.put(MESSAGE_IMG_PATH, image_path);
        msgContentValues.put(MESSAGE_GROUP, group_flag);

        //Format the datetime in a human-friendly manner.
        SimpleDateFormat sourceDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateTime_obj;
        String formattedString = "";
        try {
            dateTime_obj = sourceDF.parse(dateTime);
            DateFormat resultDF = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
            formattedString = resultDF.format(dateTime_obj);
        }
        catch (ParseException e) {
            Log.d(TAG, "Parsing datetime EXCEPTION");
        }

        Log.d(TAG, "Formatted Datetime: " + formattedString);
        msgContentValues.put(MESSAGE_FORMATTED_DT, formattedString);
        return db.insert(MESSAGE_TABLE_NAME, null, msgContentValues);
    }

    /* Retrieve all scheduled messages and some associated data in a Cursor. This will be
     * passed to an adapter that will then populate the ListView with this data. */
    public Cursor getAllScheduledMessages() {
        SQLiteDatabase db = getWritableDatabase();
        /* This will join the tables together and get many rows of messages. Each row will have columns
         * for IDs, names, and numbers. Each of those columns holds one big string delimited by
         * semicolons that has data for all of the message's associated recipients. There is also a
         * column holding the formatted datetime of the message. */
        String sql_select = "SELECT M." + MESSAGE_ID + " AS _id, "+
                            "M." + MESSAGE_TXT_CONTENT + ", " +
                            "M." + MESSAGE_FORMATTED_DT + ", " +
                            "M." + MESSAGE_DATETIME + ", " +
                            "GROUP_CONCAT(" + "R." + RECIPIENT_ID + ", ';') AS RECIPIENT_IDS, " +
                            "GROUP_CONCAT(" + "R." + RECIPIENT_NAME + ", ';') AS RECIPIENT_NAMES, " +
                            "GROUP_CONCAT(" + "R." + RECIPIENT_PHONE_NUMBER + ", ';') AS RECIPIENT_NUMBERS " +
                            "FROM " + MESSAGE_TABLE_NAME + " AS M" +
                            " LEFT JOIN " + REC_MESS_TABLE_NAME + " AS RM ON RM." + MESS_ID + "=_id" +
                            " LEFT JOIN " + RECIPIENT_TABLE_NAME + " AS R ON RM." + RECIP_ID + "=R." + RECIPIENT_ID +
                            " GROUP BY _id, M." + MESSAGE_TXT_CONTENT + ", " +
                            "M." + MESSAGE_DATETIME +
                            " ORDER BY M." + MESSAGE_DATETIME;

        Log.d(TAG, "Getting all scheduled messages.");
        return db.rawQuery(sql_select, null);
    }

    /* Get some basic data about only one message. Used to populate fields in the
     * message editing activities. */
    public Bundle getScheduledMessageData(long message_id){

        SQLiteDatabase db = getWritableDatabase();

        //Make a query for that message and its basic information.
        String sql_select = "SELECT M."+ MESSAGE_ID + ", M." + MESSAGE_DATETIME + ", " +
                "M." + MESSAGE_TXT_CONTENT + ", " +
                "M." + MESSAGE_IMG_PATH + ", " +
                "M." + MESSAGE_GROUP + ", " +
                "GROUP_CONCAT(" + "R." + RECIPIENT_ID + ", ';') AS RECIPIENT_IDS, " +
                "GROUP_CONCAT(" + "R." + RECIPIENT_NAME + ", ';') AS RECIPIENT_NAMES, " +
                "GROUP_CONCAT(" + "R." + RECIPIENT_PHONE_NUMBER + ", ';') AS RECIPIENT_NUMBERS " +
                "FROM " + MESSAGE_TABLE_NAME + " AS M" +
                " LEFT JOIN " + REC_MESS_TABLE_NAME + " AS RM ON RM." + MESS_ID + "=M." + MESSAGE_ID +
                " LEFT JOIN " + RECIPIENT_TABLE_NAME + " AS R ON RM." + RECIP_ID + "=R." + RECIPIENT_ID +
                " WHERE M." + MESSAGE_ID + "=?" +
                " GROUP BY M." + MESSAGE_ID;
        Cursor resultCursor = db.rawQuery(sql_select, new String[] {"" + message_id});
        if (resultCursor.getCount() != 1) {
            Log.d(TAG, "Edit Text Message error, couldn't find that message");
            return null;
        }
        else {
            resultCursor.moveToFirst();
            assert(resultCursor.getLong(0) == message_id);
            String recip_names = resultCursor.getString(resultCursor.getColumnIndex("RECIPIENT_NAMES"));
            String recip_nums = resultCursor.getString(resultCursor.getColumnIndex("RECIPIENT_NUMBERS"));
            String message = resultCursor.getString(resultCursor.getColumnIndex(MESSAGE_TXT_CONTENT));
            String image_path = resultCursor.getString(resultCursor.getColumnIndex(MESSAGE_IMG_PATH));
            int group_flag = resultCursor.getInt(resultCursor.getColumnIndex(MESSAGE_GROUP));
            String dateTime = resultCursor.getString(resultCursor.getColumnIndex(MESSAGE_DATETIME));
            String[] dateTimeSplit = dateTime.split(" ");
            String date = dateTimeSplit[0];
            String time = dateTimeSplit[1];
            Bundle result = new Bundle();
            result.putString("recip_names", recip_names);
            result.putString("recip_nums", recip_nums);
            result.putString("message", message);
            result.putString("image_path", image_path);
            result.putInt("group_flag", group_flag);
            result.putString("date", date);
            result.putString("time", time);
            result.putString("dateTime", dateTime);
           // String[] result = new String[] {recip_names, recip_nums, message, date, time, dateTime};
            resultCursor.close();
            return result;
        }

    }


    /* Delete a message from the database. Delete the associations between this message and its
    *  recipients, and if the recipients have no other scheduled messages, then delete them
    *  from the database. */
    public boolean deleteMessage(long message_id) {
        SQLiteDatabase db = getWritableDatabase();
        //Get all of the recipients of the message.
        String sql_select = "SELECT R." + RECIPIENT_ID + " FROM " + RECIPIENT_TABLE_NAME + " AS R" +
                " LEFT JOIN " + REC_MESS_TABLE_NAME + " AS RM ON RM." + RECIP_ID + "=R." + RECIPIENT_ID +
                " LEFT JOIN " + MESSAGE_TABLE_NAME + " AS M ON RM." + MESS_ID + "=M." + MESSAGE_ID +
                " WHERE M." + MESSAGE_ID + "=?";
        Cursor recipientCursor = db.rawQuery(sql_select, new String[] {"" + message_id});
        if (recipientCursor.getCount() < 1) {
            Log.d(TAG, "Delete Error, couldn't find that message");
            return false;
        }
        else {
            Log.d(TAG, "About to process deletion for message: " + message_id);
            // Loop through the recipients of this message.
            while(recipientCursor.moveToNext()) {
                // Get this recipient's ID
                String thisRecipient = "" + recipientCursor.getLong(0);
                Log.d(TAG, "Checking recipient: " + thisRecipient);

                // Get a count of the number of associations the recipient has.
                String subQuery = "SELECT COUNT(*) as Count FROM " + RECIPIENT_TABLE_NAME + " AS R" +
                        " LEFT JOIN " + REC_MESS_TABLE_NAME + " AS RM ON RM." + RECIP_ID + "=R." + RECIPIENT_ID +
                        " WHERE R." + RECIPIENT_ID + "=?";
                Cursor countRecip = db.rawQuery(subQuery, new String[] {thisRecipient});
                countRecip.moveToFirst();
                // how many messages is this recipient attached to?
                int numMessagesAttachedTo = countRecip.getInt(0);
                Log.d(TAG, "Count of recip's messages" + numMessagesAttachedTo);
                /* If the number is 1, then this message is the only one that recipient
                 * is attached to. Delete the recipient from our records. */
                if (numMessagesAttachedTo == 1) {
                    db.delete(RECIPIENT_TABLE_NAME, RECIPIENT_ID + "=?", new String[] {thisRecipient});
                    Log.d(TAG, "Deleting recipient: " + thisRecipient);
                }
            }
            // Delete all the association entries for this message (for all recipients).
            int numDelAssoc = db.delete(REC_MESS_TABLE_NAME, MESS_ID + "=?", new String[] {"" + message_id});
            Log.d(TAG, "Deleted assocs count: " + numDelAssoc);
            // Delete this message from the message table.
            db.delete(MESSAGE_TABLE_NAME, MESSAGE_ID + "=?", new String[] {"" + message_id});
            Log.d(TAG, "Deleted message: " + message_id);
            return true;
        }
    }

    /* This method will delete the message with the given ID and create a new one with the
    *  new values and return its ID. Before calling this method, make sure you've updated/cancelled
    *  the existing alarm used to schedule it. */
    public long updateExistingMessage(long message_id, ArrayList<Contact> recipients_list,
                                      String dateTime, String message, String image_path,
                                      int group_flag) {
        deleteMessage(message_id);
        return storeNewMessage(recipients_list, dateTime, message, image_path, group_flag);
    }

    // Store a new preset
    public long storeNewPreset(String name, String content) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues presetContentValues = new ContentValues();
        presetContentValues.put(PRESET_NAME, name);
        presetContentValues.put(PRESET_CONTENT, content);
        return db.insert(PRESET_TABLE_NAME, null, presetContentValues);
    }

    // Delete a preset
    public void deletePreset(long preset_id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(PRESET_TABLE_NAME, PRESET_ID + "=?", new String[] {"" + preset_id});
    }

    // Returns the name and content of a preset.
    public String[] getPresetData(long preset_id) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor resultCursor = db.query(PRESET_TABLE_NAME, new String[] {PRESET_NAME, PRESET_CONTENT},
                                       PRESET_ID + "=?", new String[] {"" + preset_id}, null, null,
                                       null, null);
        String name = "";
        String content = "";
        if (resultCursor.moveToFirst()) {
            name = resultCursor.getString(resultCursor.getColumnIndex(PRESET_NAME));
            content = resultCursor.getString(resultCursor.getColumnIndex(PRESET_CONTENT));
        }
        return new String[] {name, content};
    }

    // Return a cursor over all the presets.
    public Cursor getAllPresets() {
        SQLiteDatabase db = getWritableDatabase();
        String sql_select = "SELECT " + PRESET_ID + " AS _id, " +
                            PRESET_NAME + ", " +
                            PRESET_CONTENT +
                            " FROM " + PRESET_TABLE_NAME +
                            " ORDER BY _id DESC";
        return db.rawQuery(sql_select, null);
    }

    public void editPreset(long preset_id, String new_name, String new_content){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues newVals = new ContentValues();
        newVals.put(PRESET_NAME, new_name);
        newVals.put(PRESET_CONTENT, new_content);
        db.update(PRESET_TABLE_NAME, newVals, PRESET_ID + "=" + preset_id, null);
    }

}
