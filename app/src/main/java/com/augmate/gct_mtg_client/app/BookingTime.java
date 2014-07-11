package com.augmate.gct_mtg_client.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

public enum BookingTime {

    TIME_9AM("9am", 9),
    TIME_10AM("10am", 10),
    TIME_11AM("11am", 11),
    TIME_12PM("12pm", 12 ),
    TIME_1PM("1pm", 13),
    TIME_2PM("2pm", 14),
    TIME_3PM("3pm", 15),
    TIME_4PM("4pm", 16),
    TIME_5PM("5pm", 17),
    TIME_6PM("6pm", 18),
    NOW("now", 0),
    NONE("none", -1);

    public final String displayName;
    public final int hour;

    BookingTime(String displayName, int hour) {
        this.displayName = displayName;
        this.hour = hour;
    }

    public static Map<BookingTime, List<String>> asStringList() {
        Map<BookingTime, List<String>> map = new HashMap<BookingTime, List<String>>();

        map.put(TIME_9AM, newArrayList("9", "nine"));
        map.put(TIME_10AM, newArrayList("10", "ten"));
        map.put(TIME_11AM, newArrayList("11", "eleven"));
        map.put(TIME_12PM, newArrayList("12", "twelve", "noon"));
        map.put(TIME_1PM, newArrayList("1", "one", "won"));
        map.put(TIME_2PM, newArrayList("2", "two", "too", "to"));
        map.put(TIME_3PM, newArrayList("3", "three"));
        map.put(TIME_4PM, newArrayList("4", "four", "for"));
        map.put(TIME_5PM, newArrayList("5", "five"));
        map.put(TIME_6PM, newArrayList("6", "six"));

        return map;
    }
}
