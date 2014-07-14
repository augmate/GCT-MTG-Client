package com.augmate.gct_mtg_client.app;

import android.util.Log;

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
    NOW("now", 0),
    NONE("none", -1);
    public static final String TAG = "BookingTime";

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

        map.put(TIME_9AM, newArrayList("9", "9 am", "nine"));
        map.put(TIME_10AM, newArrayList("10", "10 am", "ten"));
        map.put(TIME_11AM, newArrayList("11", "11 am" , "eleven"));
        map.put(TIME_12PM, newArrayList("12","12 pm", "twelve", "noon"));
        map.put(TIME_1PM, newArrayList("1", "1 pm", "13", "one", "won"));
        map.put(TIME_2PM, newArrayList("2", "2 pm", "14", "two", "too", "to"));
        map.put(TIME_3PM, newArrayList("3", "3 pm", "15", "three"));
        map.put(TIME_4PM, newArrayList("4", "4 pm", "16", "four", "for"));
        map.put(TIME_5PM, newArrayList("5", "5 pm", "17", "five", "spy cam"));
        map.put(TIME_6PM, newArrayList("6", "6 pm", "18", "six"));

        return map;
    }


    public static BookingTime fromHour(int hour){

        for(BookingTime bookingTime : BookingTime.values()){
            if (bookingTime.hour == hour){
                return bookingTime;
            }
        }

        Log.d(TAG, "Could not map hour " + hour + " into BookingTime");
        return BookingTime.NONE;
    }
}
