package owpk.ezqrtz.core.utils;

import lombok.experimental.UtilityClass;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@UtilityClass
public class CronExpressions {

    public static String daily(LocalTime time) {
        return "%d %d %d * * ?".formatted(
                time.getSecond(),
                time.getMinute(),
                time.getHour()
        );
    }

    public static String weekdays(LocalTime time) {
        return "%d %d %d ? * MON-FRI".formatted(
                time.getSecond(),
                time.getMinute(),
                time.getHour()
        );
    }

    public static String weekends(LocalTime time) {
        return "%d %d %d ? * SAT,SUN".formatted(
                time.getSecond(),
                time.getMinute(),
                time.getHour()
        );
    }

    public static String weekly(LocalTime time, DayOfWeek... days) {
        var expression = Arrays.stream(days)
                .map(CronExpressions::day)
                .collect(Collectors.joining(","));

        return "%d %d %d ? * %s".formatted(
                time.getSecond(),
                time.getMinute(),
                time.getHour(),
                expression
        );
    }

    public static String monthly(int dayOfMonth, LocalTime time) {
        return "%d %d %d %d * ?".formatted(
                time.getSecond(),
                time.getMinute(),
                time.getHour(),
                dayOfMonth
        );
    }

    public static String day(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "MON";
            case TUESDAY -> "TUE";
            case WEDNESDAY -> "WED";
            case THURSDAY -> "THU";
            case FRIDAY -> "FRI";
            case SATURDAY -> "SAT";
            case SUNDAY -> "SUN";
        };
    }

}