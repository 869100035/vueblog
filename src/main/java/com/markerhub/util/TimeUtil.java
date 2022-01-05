package com.markerhub.util;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static DateTimeFormatter dtfAll = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
    public static LocalDate getTodayTime() {
        return LocalDate.now();
    }

    public static String getTodayTimeForMat() {

        return LocalDateTime.now().format(dtfAll);
    }
    public static LocalDateTime getLocalDateTime() {
        return LocalDateTime.now();
    }
    public static LocalDate getTheDayAfterSomeDays(long days){
        //LocalDateTime tor = LocalDateTime.parse(getTodayTime(), dtf);
        return getTodayTime().plusDays(days);//.format(dtf);
    }

    public static LocalDate getTheDayBeforeSomeDays(long days){
        //LocalDateTime tor = LocalDateTime.parse(getTodayTime(), dtf);
        return getTodayTime().minusDays(days);//.format(dtf);
    }
}
