package ita.util;

import java.util.Calendar;

public final class CalendarUtil {

    private CalendarUtil() {
        throw new IllegalStateException("Utility class");
    }
    public static Calendar getCalendarInstance(Long millis) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(millis);

        return calendar;
    }
}
