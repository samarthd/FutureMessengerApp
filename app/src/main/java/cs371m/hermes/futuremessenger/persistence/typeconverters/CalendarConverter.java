package cs371m.hermes.futuremessenger.persistence.typeconverters;

import android.arch.persistence.room.TypeConverter;

import java.util.Calendar;

/**
 * Converts between a Calendar object and its value in milliseconds.
 */
public class CalendarConverter {
    @TypeConverter
    public Calendar millisToCalendar(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        return calendar;
    }

    @TypeConverter
    public long calendarToMillis(Calendar calendar) {
        return calendar.getTimeInMillis();
    }
}
