package com.augmate.gct_mtg_client.app;

import android.util.Log;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MeetingBooker {
    
    private static Map<String,String> CALENDAR_IDS = new HashMap<String,String>();

    static {
        CALENDAR_IDS.put("Room 1", "nexweb.com_tkselniqr1e6sgn207optnhil0@group.calendar.google.com");
        CALENDAR_IDS.put("Room 2", "nexweb.com_k3mj5av5j3mn2pcop96piklcbk@group.calendar.google.com");
        CALENDAR_IDS.put("Garage", "nexweb.com_t7rme89uccrnu2see0m6km8qk0@group.calendar.google.com");
        CALENDAR_IDS.put("Room 4", "nexweb.com_a4jmm6foedk1r8rmjtf8v29e6k@group.calendar.google.com");
        CALENDAR_IDS.put("Room 5", "nexweb.com_8ql9i1k0o7042omr7c2e3lpboo@group.calendar.google.com");
    }

    private Calendar calendarService;

    public MeetingBooker(Calendar calendarService) {
        this.calendarService = calendarService;
    }

    public boolean bookNow(String roomNumber) {
        boolean wasSuccess = true;

        org.joda.time.DateTime roundedStartTime = getRoundedStartTime();

        EventDateTime startTime = getEventDateTime(roundedStartTime);
        EventDateTime endTime = getEventDateTime(roundedStartTime.plusMinutes(30));

        Event event = new Event()
                .setSummary("Booking")
                .setStart(startTime)
                .setEnd(endTime);

        try {
            calendarService.events().insert(CALENDAR_IDS.get(roomNumber), event).execute();
            Log.d("com.augmate.booking", String.format("Meeting room %s booked for %s", roomNumber, event.getStart()));
        } catch (IOException e) {
            e.printStackTrace();
            wasSuccess = false;
        } catch(IndexOutOfBoundsException e){
            e.printStackTrace();
            wasSuccess = false;
        }

        return wasSuccess;
    }

    private EventDateTime getEventDateTime(org.joda.time.DateTime roundedStartTime) {
        return new EventDateTime().setDateTime(new DateTime(roundedStartTime.toDate()));
    }

    private org.joda.time.DateTime getRoundedStartTime() {
        org.joda.time.DateTime now = new org.joda.time.DateTime().now();
        return now.minusMinutes(now.getMinuteOfHour() % 30);
    }
}
