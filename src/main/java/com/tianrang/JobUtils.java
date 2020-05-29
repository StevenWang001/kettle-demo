package com.tianrang;

import javafx.util.Pair;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class JobUtils {

    public static String getRandomTableName(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "");
    }

    public static Pair<Integer, TimeUnit> parseInterval(String interval) {
        String timeUnit = interval.substring(interval.length() - 1);
        TimeUnit t = TimeUnit.MINUTES;
        switch (timeUnit) {
            case "s":
                t = TimeUnit.SECONDS;
                break;
            case "m":
                t = TimeUnit.MINUTES;
                break;
            case "h":
                t = TimeUnit.HOURS;
                break;
            case "d":
                t = TimeUnit.DAYS;
                break;
            default:
                break;
        }
        Integer time = Integer.valueOf(interval.substring(0, interval.length() - 1));
        return new Pair<>(time, t);
    }
}
