package com.augmate.gct_mtg_client.app;

import com.augmate.gct_mtg_client.app.utils.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

public enum BookingTime {

    TIME_9AM("9am", 9),
    TIME_10AM("10am", 10),
    TIME_11AM("11am", 11),
    TIME_12PM("12pm", 12),
    TIME_1PM("1pm", 13),
    TIME_2PM("2pm", 14),
    TIME_3PM("3pm", 15),
    TIME_4PM("4pm", 16),
    TIME_5PM("5pm", 17),
    TIME_6PM("6pm", 18),
    TIME_7PM("7pm", 19),
    TIME_8PM("8pm", 20),
    TIME_9PM("9pm", 21),
    TIME_10PM("10pm", 22),
    TIME_11PM("11pm", 23),
    NOW("now", 0),
    NONE("none",-1),
    INVALID("invalid", -2);

    public final String displayName;
    public final int hour;

    BookingTime(String displayName, int hour) {
        this.displayName = displayName;
        this.hour = hour;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static Map<BookingTime, List<String>> asStringList() {
        Map<BookingTime, List<String>> map = new HashMap<BookingTime, List<String>>();

        map.put(NOW, newArrayList("now", "book now"));
        map.put(NONE, newArrayList("none"));
        map.put(INVALID, newArrayList("invalid"));
        map.put(TIME_9AM, newArrayList("9", "9 am", "book 9 am", "nine"));
        map.put(TIME_10AM, newArrayList("10", "10 am", "book 10 am", "ten"));
        map.put(TIME_11AM, newArrayList("11", "11 am" , "book 11 am",  "eleven"));
        map.put(TIME_12PM, newArrayList("12","12 pm", "book 12 pm", "twelve", "noon"));
        map.put(TIME_1PM, newArrayList("1", "1 pm", "book 1 pm", "13", "one", "won"));
        map.put(TIME_2PM, newArrayList("2", "2 pm", "book 2 pm", "14", "two", "too", "to"));
        map.put(TIME_3PM, newArrayList("3", "3 pm", "book 3 pm", "15", "three"));
        map.put(TIME_4PM, newArrayList("4", "4 pm", "book 4 pm", "16", "four", "for"));
        map.put(TIME_5PM, newArrayList("5", "5 pm", "book 5 pm",  "17", "five", "spy cam"));
        map.put(TIME_6PM, newArrayList("6", "6 pm", "book 6 pm",  "18", "six"));
        map.put(TIME_7PM, newArrayList("7", "7 pm"));
        map.put(TIME_8PM, newArrayList("8", "8 pm"));
        map.put(TIME_9PM, newArrayList("9", "9 pm"));
        map.put(TIME_10PM, newArrayList("10", "10 pm"));
        map.put(TIME_11PM, newArrayList("11", "11 pm"));

        return map;
    }


    public static BookingTime fromHour(int hour, int currentHour){
        if(hour == currentHour){
            return NOW;
        }

        for(BookingTime bookingTime : BookingTime.values()){
            if (bookingTime.hour == hour){
                return bookingTime;
            }
        }

        Log.debug("Could not map hour " + hour + " into BookingTime");
        return BookingTime.INVALID;
    }
}
