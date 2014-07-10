package com.augmate.gct_mtg_client.app;

import android.util.Log;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

public class MeetingBooker {

    public static final String TAG = "MeetingBooker";
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
        return now.minusMinutes(now.getMinuteOfHour());
    }

    private org.joda.time.DateTime getLastBookingTime() {
        org.joda.time.DateTime now = new org.joda.time.DateTime().now();
        return now.withHourOfDay(19);
    }


    public List<String> getAvailability(Room requestedRoom) {

        FreeBusyRequest freeBusyRequest = new FreeBusyRequest();
        freeBusyRequest.setTimeMin(getDT(getRoundedStartTime().minusMinutes(1)));
        freeBusyRequest.setTimeMax(getDT(getLastBookingTime().plusHours(4)));

        FreeBusyRequestItem calendarItem = new FreeBusyRequestItem().setId(CALENDAR_IDS.get(requestedRoom));
        freeBusyRequest.setItems(newArrayList(calendarItem));


        // 9am-6pm
        // for each hour, check if there is a timeperiod which claims it
        //   if no time period claims it, then the hour is available
        //   if any time period claims it, the hour is unavailable

        try {
            FreeBusyResponse freeBusyResponse = calendarService.freebusy().query(freeBusyRequest).execute();
            List<TimePeriod> busyTimes = freeBusyResponse.getCalendars().get(CALENDAR_IDS.get(requestedRoom)).getBusy();

            for(int i = 9; i <= 18; i ++) {
                for(TimePeriod busyPeriod : busyTimes) {
                    long startTimestamp = busyPeriod.getStart().getValue();
                    long endTimestamp = busyPeriod.getEnd().getValue();

                    org.joda.time.DateTime start = new org.joda.time.DateTime(startTimestamp);
                    org.joda.time.DateTime end = new org.joda.time.DateTime(endTimestamp);

                    Log.d(TAG, "busy period: " + start + " to " + end);

                    int startHour = start.getHourOfDay();
                    int endHour = end.getHourOfDay();

                    if(startHour <= i && i < endHour) {
                        Log.d(TAG, "  room is busy at " + i + ":00");
                    }
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "Could not get availability of room " + requestedRoom.displayName, e);
        }



        return null;
    }

    private DateTime getDT(org.joda.time.DateTime jodaTime){
        return new DateTime(jodaTime.toDate());
    }
}
