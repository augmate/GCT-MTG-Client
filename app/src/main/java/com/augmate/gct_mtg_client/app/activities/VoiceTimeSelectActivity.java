package com.augmate.gct_mtg_client.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import com.augmate.gct_mtg_client.R;
import com.augmate.gct_mtg_client.app.BookingTime;
import com.augmate.gct_mtg_client.app.Room;
import com.augmate.gct_mtg_client.app.tasks.CheckRoomAvailabilityTask;
import com.augmate.gct_mtg_client.app.tasks.VoiceTimeSelectActivityCallbacks;
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
    public static final String TAG = VoiceTimeSelectActivity.class.getName();
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

        if(!availabilities.isEmpty()) {
            String availableTimesString = Joiner.on(", ").join(availabilities);

            String roomPrompt = String.format("Room %s is available. Book now? or %s", requestedRoom.displayName, availableTimesString);

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, roomPrompt);

            Log.d(TAG, "Requesting time select via speech-recognition..");

            // we track a new voice input step every time
            voiceTimeRawInputStart = SystemClock.uptimeMillis();
            startActivityForResult(intent, VOICE_RECOGNIZER_REQUEST_CODE);
        }else{
            Toast.makeText(this, "Cannot fetch room availability", Toast.LENGTH_LONG);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Analytics.track("GCT - Voice Raw Result Time", new Props(
                "value", SystemClock.uptimeMillis() - voiceTimeRawInputStart
        ));

        if (resultCode == RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Log.d(TAG, "Got " + results.size() + " results from Speech API");

            String voiceString = results.get(0);

            BookingTime bookingTime = VoiceTimeDisambiguator.match(voiceString, BookingTime.asStringList());

            Log.d(TAG, "Booking time recognized as: " + bookingTime);


            if (bookingTime != BookingTime.NONE) {

                if (availabilities.contains(bookingTime)) {
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
                }
            } else {
                // speech-api recognized something that isn't a timeslot. restart.
                Toast.makeText(this, "Time not recognized, try again", Toast.LENGTH_LONG).show();
            }
        } else {
            // speech-api didn't recognize anything. restart
            Log.e(TAG, "Voice recognition failed with result code = " + resultCode);
        }

        // user said None, or whatever they said wasn't recognized
        Log.d(TAG, "Voice finished without positive user response (yes, etc)");
        onRecieveAvailabilities(availabilities);
    }
}
