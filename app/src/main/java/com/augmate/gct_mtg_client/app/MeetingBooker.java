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
    public static final int LAST_BOOKABLE_SLOT = 18;
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

    // TODO: handle BookingTime.NONE
    public boolean bookNow(Room roomNumber, BookingTime bookingTime) {
        boolean wasSuccess = true;

        org.joda.time.DateTime bookingStartTime = org.joda.time.DateTime.now();
        bookingStartTime = bookingStartTime.withMinuteOfHour(0).withSecondOfMinute(0);

        if(bookingTime != BookingTime.NOW) {
            bookingStartTime.withHourOfDay(bookingTime.hour);
        }

        EventDateTime startTime = convertJodaToCalendarEventTime(bookingStartTime);
        EventDateTime endTime = convertJodaToCalendarEventTime(bookingStartTime.plusHours(1));

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

    private EventDateTime convertJodaToCalendarEventTime(org.joda.time.DateTime roundedStartTime) {
        return new EventDateTime().setDateTime(new DateTime(roundedStartTime.toDate()));
    }

    private org.joda.time.DateTime getRoundedStartTime() {
        return org.joda.time.DateTime.now().withMinuteOfHour(0).withSecondOfMinute(0);
    }

    private DateTime getMaxTimeForCalendar() {
        return getDT(org.joda.time.DateTime.now().withHourOfDay(LAST_BOOKABLE_SLOT + 1));
    }


    public List<Integer> getAvailability(Room requestedRoom) {

        // requests busy periods from google calendar
        FreeBusyRequest freeBusyRequest = new FreeBusyRequest();
        freeBusyRequest.setTimeMin(getDT(getRoundedStartTime().minusMinutes(1)));
        freeBusyRequest.setTimeMax(getMaxTimeForCalendar());

        FreeBusyRequestItem calendarItem = new FreeBusyRequestItem().setId(CALENDAR_IDS.get(requestedRoom));
        freeBusyRequest.setItems(newArrayList(calendarItem));

        // build up available hours
        List<Integer> availableSlots = newArrayList();


        try {
            FreeBusyResponse freeBusyResponse = calendarService.freebusy().query(freeBusyRequest).execute();

            List<TimePeriod> busyTimes = freeBusyResponse.getCalendars().get(CALENDAR_IDS.get(requestedRoom)).getBusy();
            Log.d(TAG, "Found " + busyTimes.size() + " busy slots for " + requestedRoom.displayName);

            // from current hour until the hour we would want to book
            //   check if hour is inside one of the busy periods
            //     if it isn't, add it to the available hours list
            for(int hourSlot = getRoundedStartTime().getHourOfDay(); hourSlot <= LAST_BOOKABLE_SLOT; hourSlot ++) {
                org.joda.time.DateTime timeSlot = new org.joda.time.DateTime().withHourOfDay(hourSlot).withMinuteOfHour(30);

                Boolean isBusy = false;

                for(TimePeriod busyPeriod : busyTimes) {
                    long startTimestamp = busyPeriod.getStart().getValue();
                    long endTimestamp = busyPeriod.getEnd().getValue();

                    org.joda.time.DateTime busyStart = new org.joda.time.DateTime(startTimestamp);
                    org.joda.time.DateTime busyEnd = new org.joda.time.DateTime(endTimestamp);

                    if(timeSlot.isAfter(busyStart) && timeSlot.isBefore(busyEnd)) {
                        Log.d(TAG, "Room is busy at " + hourSlot);
                        isBusy = true;
                    }
                }

                if(!isBusy)
                    availableSlots.add(hourSlot);
            }

        } catch (IOException e) {
            Log.e(TAG, "Could not get availability of room " + requestedRoom.displayName, e);
        }

        return availableSlots;
    }

    private DateTime getDT(org.joda.time.DateTime jodaTime){
        return new DateTime(jodaTime.toDate());
    }
}
