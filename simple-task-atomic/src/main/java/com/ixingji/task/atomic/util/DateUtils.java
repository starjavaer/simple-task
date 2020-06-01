package com.ixingji.task.atomic.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");

    public static String nowInMs() {
        ZonedDateTime zdt = ZonedDateTime.now();
        return dtf.format(zdt);
    }

}
