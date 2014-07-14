package com.augmate.gct_mtg_client.app;

import android.util.Log;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import org.joda.time.DateTime;

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

    public boolean bookNow(Room roomNumber, BookingTime bookingTime, String companyName) {
        
        if(bookingTime == BookingTime.NONE) {
            Log.d(TAG, "Booking time is None. Not booking.");
            return false;
        }
        
        // either book now or for a specific hour
        DateTime bookingStartTime = getCurrentHour();
        
        if(bookingTime != BookingTime.NOW) {
            bookingStartTime = bookingStartTime.withHourOfDay(bookingTime.hour);
        }

        EventDateTime startTime = toCalendarEventTime(bookingStartTime);
        EventDateTime endTime = toCalendarEventTime(bookingStartTime.plusHours(1));

        Event event = new Event()
                .setSummary("Booking")
                .setStart(startTime)
                .setEnd(endTime);

        boolean roomBooked = true;

        Log.d(TAG, String.format("Attempting to book room '%s' at %s for %s", roomNumber, event.getStart(), companyName));
        
        try {
            calendarService.events().insert(CALENDAR_IDS.get(roomNumber), event).execute();
            Log.d(TAG, String.format("Meeting room '%s' booked for %s", roomNumber, event.getStart()));
        } catch(com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
            Log.e(TAG, "Google Calendar problem", e);
        } catch (Exception e) {
            Log.e(TAG, "Failed to insert event into google calendar", e);
            e.printStackTrace();
            roomBooked = false;
        }

        return roomBooked;
    }

    /**
     * joda time -> gcal time -> gcal event time
     * @param jodaTime DateTime from Joda
     * @return EventDateTime for google calendar events
     */
    private EventDateTime toCalendarEventTime(DateTime jodaTime) {
        return new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(jodaTime.toDate()));
    }

    private com.google.api.client.util.DateTime toCalenderTime(DateTime jodaTime){
        return new com.google.api.client.util.DateTime(jodaTime.toDate());
    }

    private DateTime getCurrentHour() {
        return DateTime.now().withMinuteOfHour(0).withSecondOfMinute(0);
    }

    private com.google.api.client.util.DateTime getMaxTimeForCalendar() {
        return toCalenderTime(DateTime.now().withHourOfDay(LAST_BOOKABLE_SLOT + 1));
    }

    public List<BookingTime> getAvailability(Room requestedRoom) {

        // requests busy periods from google calendar
        FreeBusyRequest freeBusyRequest = new FreeBusyRequest();
        freeBusyRequest.setTimeMin(toCalenderTime(getCurrentHour().minusMinutes(1)));
        freeBusyRequest.setTimeMax(getMaxTimeForCalendar());

        FreeBusyRequestItem calendarItem = new FreeBusyRequestItem().setId(CALENDAR_IDS.get(requestedRoom));
        freeBusyRequest.setItems(newArrayList(calendarItem));

        // build up available hours
        List<BookingTime> availableSlots = newArrayList();


        try {
            FreeBusyResponse freeBusyResponse = calendarService.freebusy().query(freeBusyRequest).execute();

            List<TimePeriod> busyTimes = freeBusyResponse.getCalendars().get(CALENDAR_IDS.get(requestedRoom)).getBusy();
            Log.d(TAG, "Found " + busyTimes.size() + " busy slots for " + requestedRoom.displayName);

            // from current hour until the hour we would want to book
            //   check if hour is inside one of the busy periods
            //     if it isn't, add it to the available hours list
            for(int hourSlot = getCurrentHour().getHourOfDay(); hourSlot <= LAST_BOOKABLE_SLOT; hourSlot ++) {
                DateTime timeSlot = new DateTime().withHourOfDay(hourSlot).withMinuteOfHour(30);

                Boolean isBusy = false;

                for(TimePeriod busyPeriod : busyTimes) {
                    DateTime busyStart = new DateTime(busyPeriod.getStart().getValue());
                    DateTime busyEnd = new DateTime(busyPeriod.getEnd().getValue());

                    if(timeSlot.isAfter(busyStart) && timeSlot.isBefore(busyEnd)) {
                        Log.d(TAG, "Room is busy at " + hourSlot);
                        isBusy = true;
                        break;
                    }
                }

                if(!isBusy)
                    availableSlots.add(BookingTime.fromHour(hourSlot));
            }

        } catch (IOException e) {
            Log.e(TAG, "Could not get availability of room " + requestedRoom.displayName, e);
        }

        return availableSlots;
    }
}
