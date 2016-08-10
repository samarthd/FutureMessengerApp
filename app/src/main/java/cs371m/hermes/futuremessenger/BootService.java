package cs371m.hermes.futuremessenger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import java.util.Calendar;

/**
 * Created by Drew on 8/3/2016.
 */
public class BootService extends Service {

    @Override
    public void onCreate(){
        Log.d("BootService: onCreate", "Creating BootService");
    }

    @Override
    public void onStart(Intent intent, int startId){
        super.onStart(intent, startId);
        Log.d("BootService", "onStart");
        resetAlarms();
    }

    @Override
    public IBinder onBind(Intent intent){
        Log.d("BootService", "onBind");
        return null;
    }

    private void resetAlarms(){
        MessengerDatabaseHelper mdb = MessengerDatabaseHelper.getInstance(this);
        Cursor cursor = mdb.getAllScheduledMessages();

        while (cursor.moveToNext()) {
            String dateTime = cursor.getString(cursor.getColumnIndex("DATETIME"));
            long id = cursor.getLong(cursor.getColumnIndex("_id"));
            setAlarm(id, dateTime);
            Log.d("BootService", dateTime);
        }
        mdb.close();
    }


    /**
     * Sets an alarm for a particular message at a certain time.
     * @param id, the ID of the message
     * @param date, starts in YYYY-MM-DD HH:MM:SS format
     */
    private void setAlarm(long id, String date){
        int year = Integer.parseInt(date.substring(0,4));
        int month = Integer.parseInt(date.substring(5,7)) - 1;//minus once since Jan = 0
        int day = Integer.parseInt(date.substring(8,10));
        int hour = Integer.parseInt(date.substring(11,13));
        int minute = Integer.parseInt(date.substring(14,16));

        /* Set the alarm with the selected parameters */
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putLong("message_id", id);
        alarmIntent.putExtras(bundle);

        PendingIntent pendingIntent = PendingIntent.getService(this,
                (int) id, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        /* Set calendar dates */
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Log.d("resetAlarm", "Message id = " + Long.toString(id));
    }
}
