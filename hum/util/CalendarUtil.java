package ita.util;

import java.util.Calendar;
import java.util.TimeZone;

public class CalendarUtil {

    public static Calendar getCalendarInstance(Long millis) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(millis);

        return calendar;
    }

//    public static Integer getDay() {
//        Calendar calendar = getCalendarInstance();
//
//        return calendar.get(Calendar.DAY_OF_WEEK);
//    }
//
//    public static Integer getMonth() {
//        Calendar calendar = getCalendarInstance();
//
//        return calendar.get(Calendar.MONTH);
//    }
//
//    public static Integer getYear() {
//        Calendar calendar = getCalendarInstance();
//
//        return calendar.get(Calendar.YEAR);
//    }

}
