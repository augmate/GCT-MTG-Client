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

    private static Map<Room,String> CALENDAR_IDS = new HashMap<Room,String>();

    static {
        CALENDAR_IDS.put(Room.ROOM_1, "augmate.com_3r68n4o9i492n2ls5o43po776s@group.calendar.google.com");
        CALENDAR_IDS.put(Room.ROOM_2, "augmate.com_ikbbr12a63rnsvh6it0lp72j64@group.calendar.google.com");
        CALENDAR_IDS.put(Room.ROOM_3, "augmate.com_gg1vm8pdabdjrlinvtcn7ufu4g@group.calendar.google.com");
        CALENDAR_IDS.put(Room.ROOM_4, "augmate.com_ktsqh7qjbqdi3rpe3nebesn240@group.calendar.google.com");
        CALENDAR_IDS.put(Room.ROOM_5, "augmate.com_9qf9h796j2urm8ks1j1sg45p2o@group.calendar.google.com");
        CALENDAR_IDS.put(Room.GARAGE, "augmate.com_ndigrv91nr0baictn6e1llu5qk@group.calendar.google.com");
    }

    private Calendar calendarService;

    public MeetingBooker(Calendar calendarService) {
        this.calendarService = calendarService;
    }

    public boolean bookNow(Room roomNumber) {
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
