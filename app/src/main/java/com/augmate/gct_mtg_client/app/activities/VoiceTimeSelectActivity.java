package com.augmate.gct_mtg_client.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.view.WindowManager;
import android.widget.Toast;
import com.augmate.gct_mtg_client.R;
import com.augmate.gct_mtg_client.app.BookingTime;
import com.augmate.gct_mtg_client.app.Room;
import com.augmate.gct_mtg_client.app.tasks.CheckRoomAvailabilityTask;
import com.augmate.gct_mtg_client.app.tasks.VoiceTimeSelectActivityCallbacks;
import com.augmate.gct_mtg_client.app.utils.Log;
import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.segment.android.Analytics;
import com.segment.android.models.Props;
import roboguice.inject.ContentView;
import roboguice.inject.InjectExtra;

import java.util.ArrayList;
import java.util.List;

@ContentView(R.layout.loading_room_availabilities)
public class VoiceTimeSelectActivity extends TrackedGuiceActivity implements VoiceTimeSelectActivityCallbacks {

    public static final String ROOM_NAME_EXTRA = "ROOM_NAME_EXTRA";
    public static final int VOICE_RECOGNIZER_REQUEST_CODE = 101;
    public static final String COMPANY_NAME_EXTRA = "COMPANY_NAME_EXTRA";

    @InjectExtra(ROOM_NAME_EXTRA)
    Room requestedRoom;
    @InjectExtra(COMPANY_NAME_EXTRA)
    String companyName;

    private List<BookingTime> availabilities;
    private long fetchRoomAvailabilityStart;
    private long voiceTimeSelectionStart;
    private long voiceTimeRawInputStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        fetchRoomAvailabilityStart = SystemClock.uptimeMillis();

        new CheckRoomAvailabilityTask(this, this, requestedRoom).execute();
    }

    @Override
    public void onRecieveAvailabilities(List<BookingTime> availabilities) {

        if (this.availabilities == null) {
            Analytics.track("GCT - Fetch Room Availability", new Props(
                    "value", SystemClock.uptimeMillis() - fetchRoomAvailabilityStart
            ));

            // first time after fetching available rooms, we start tracking the overall voice process
            voiceTimeSelectionStart = SystemClock.uptimeMillis();
        }

        //TODO: Fix this cache
        this.availabilities = availabilities;
        Log.debug("Received " + availabilities.size() + " availabilities");
        if (!availabilities.isEmpty()) {
            //String availableTimesString = Joiner.on(", ").join(availabilities);
            //String roomPrompt = String.format("%s is available for:\n\n%s\n\n(say 'none' to go back)", requestedRoom.displayName, availableTimesString);
            String roomPrompt = processAvailabilities(availabilities);
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, roomPrompt);

            // we track a new voice input step every time
            voiceTimeRawInputStart = SystemClock.uptimeMillis();
            startActivityForResult(intent, VOICE_RECOGNIZER_REQUEST_CODE);
        } else {
            Log.debug("No times left for this room");
            Toast.makeText(this, "No times left for this room", Toast.LENGTH_LONG).show();
            Intent i = new Intent(this, RoomSelectionActivity.class);
            i.putExtra(RoomSelectionActivity.COMPANY_NAME_EXTRA, companyName);
            startActivity(i);
            finish();
        }
    }

    private String processAvailabilities(List<BookingTime> availabilities) {
        String pmTimes = null;
        String amTimes = null;
        String returnString = "None";
        int spacer = 0;
        for(BookingTime bt : availabilities){
            if(bt.toString().contains("pm"))
                pmTimes = (pmTimes == null) ? bt.toString() : pmTimes.concat(" " + bt.toString());
            else if(bt.toString().contains("am"))
                amTimes = (amTimes == null) ? bt.toString() : amTimes.concat(" " + bt.toString());
        }
        if(pmTimes != null) {
            spacer++;
            returnString = returnString.concat("\n" + pmTimes);
        }
        if(amTimes != null) {
            spacer++;
            returnString = returnString.concat("\n" + amTimes);
        }
        if(availabilities.contains(BookingTime.NOW))
            returnString = returnString.concat("\n" + "Now");
        returnString = (spacer == 2) ? returnString.concat("\n\n") : returnString.concat("\n\n\n");
        return returnString.concat("\t\t\t\t\t\t\t"+"Choose a time");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Analytics.track("GCT - Voice Raw Result Time", new Props(
                "value", SystemClock.uptimeMillis() - voiceTimeRawInputStart
        ));

        Log.debug("resultCode = " + resultCode);

        if (resultCode == RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Log.debug("Got " + results.size() + " results from Speech API");

            String voiceString = results.get(0);

            BookingTime bookingTime = VoiceTimeDisambiguator.match(voiceString, BookingTime.asStringList());

            Log.debug("Booking time recognized as: " + bookingTime);
            if (bookingTime != BookingTime.INVALID) {
                if(bookingTime == BookingTime.NONE) {
                    Log.debug("NONE recognized, going back to room selection");
                    launchRoomSelectionActivity();
                    finish();
                    return;
                }else if (availabilities.contains(bookingTime)) {
                    // we have selected a real time
                    Analytics.track("GCT - Voice Selection Completed", new Props(
                            "value", SystemClock.uptimeMillis() - voiceTimeSelectionStart
                    ));

                    Intent intent = new Intent(this, BookingActivity.class);
                    intent.putExtra(BookingActivity.ROOM_NUMBER_EXTRA, requestedRoom);
                    intent.putExtra(BookingActivity.BOOKING_TIME_EXTRA, bookingTime);
                    intent.putExtra(BookingActivity.COMPANY_NAME_EXTRA, companyName);
                    startActivity(intent);
                    finish();
                    return;

                } else {
                    // requested booking time isn't available. restart.
                    Toast.makeText(this, "Timeslot is unavailable", Toast.LENGTH_LONG).show();
                    onRecieveAvailabilities(availabilities);
                }
            } else {
                // speech-api recognized something that isn't a timeslot. restart.
                Toast.makeText(this, "Time not recognized, try again", Toast.LENGTH_SHORT).show();
                onRecieveAvailabilities(availabilities);
            }
        }
        else if (resultCode == RESULT_CANCELED) {
            launchRoomSelectionActivity();
            finish();
        } else {
            // speech-api didn't recognize anything. restart
            Log.error("Voice recognition failed with result code = " + resultCode);


            // user said None, or whatever they said wasn't recognized
            Log.debug("Voice finished without positive user response (yes, etc)");
            onRecieveAvailabilities(availabilities);
        }
    }

    private void launchRoomSelectionActivity() {
        Intent i = new Intent(this, RoomSelectionActivity.class);
        i.putExtra(RoomSelectionActivity.COMPANY_NAME_EXTRA, companyName);
        startActivity(i);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.debug("Activity being destroyed");
    }
}