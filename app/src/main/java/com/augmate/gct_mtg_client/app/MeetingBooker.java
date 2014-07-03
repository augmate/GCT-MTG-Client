package com.augmate.gct_mtg_client.app;

import android.util.Log;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;

public class MeetingBooker {


    private Calendar calendarService;

    public MeetingBooker(Calendar calendarService) {

        this.calendarService = calendarService;
    }

    public boolean bookNow() {
        boolean wasSuccess = true;

        DateTime start = DateTime.parseRfc3339("2014-07-03T12:00:00.000Z");
        DateTime end = DateTime.parseRfc3339("2014-07-03T12:25:00.000Z");

        Event event = new Event()
                .setSummary("Booking")
                .setStart(new EventDateTime().setDateTime(start))
                .setEnd(new EventDateTime().setDateTime(end));

        try {
            calendarService.events().insert("nexweb.com_tkselniqr1e6sgn207optnhil0@group.calendar.google.com", event).execute();

            Log.d("com.augmate.booking", "Meeting booked for " + event.getOriginalStartTime());
        } catch (IOException e) {
            e.printStackTrace();
            wasSuccess = false;
        }

        return wasSuccess;
    }
}
