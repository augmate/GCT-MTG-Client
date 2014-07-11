package com.augmate.gct_mtg_client.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import com.augmate.gct_mtg_client.app.BookingTime;
import com.augmate.gct_mtg_client.app.Room;
import com.augmate.gct_mtg_client.app.tasks.CheckRoomAvailabilityTask;
import com.augmate.gct_mtg_client.app.tasks.VoiceTimeSelectActivityCallbacks;
import com.google.api.client.repackaged.com.google.common.base.Joiner;
import roboguice.inject.InjectExtra;

import java.util.ArrayList;
import java.util.List;

public class VoiceTimeSelectActivity extends TrackedGuiceActivity implements VoiceTimeSelectActivityCallbacks {

    public static final String ROOM_NUMBER_EXTRA = "ROOM_NUMBER_EXTRA";
    public static final String TAG = VoiceTimeSelectActivity.class.getName();
    public static final int VOICE_RECOGNIZER_REQUEST_CODE = 101;

    @InjectExtra(ROOM_NUMBER_EXTRA)
    Room requestedRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new CheckRoomAvailabilityTask(this, this, requestedRoom).execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Log.d(TAG, "Got " + results.size() + " results from Speech API");

            String voiceString = results.get(0);

            BookingTime bookingTime = VoiceTimeDisambiguator.match(voiceString, BookingTime.asStringList());

            Log.d(TAG, "Booking time recognized as: " + bookingTime);
            
            if(bookingTime != BookingTime.NONE) {

                Intent intent = new Intent(this, BookingActivity.class);
                intent.putExtra(BookingActivity.ROOM_NUMBER_EXTRA, requestedRoom);
                intent.putExtra(BookingActivity.BOOKING_TIME_EXTRA, bookingTime);

                startActivity(intent);
                return;
            }
        }
        
        // user said None, or whatever they said wasn't recognized
        Log.d(TAG, "Voice finished without positive user response (yes, etc)");
    }

    @Override
    public void onTaskSuccess(List<Integer> availabilities) {

        String availableTimesString = Joiner.on(", ").join(availabilities);

        String roomPrompt = String.format("Room %s is available. Book now? or " + availableTimesString, requestedRoom);

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, roomPrompt);

        Log.d(TAG, "Requesting time select via speech-recognition..");
        startActivityForResult(intent, VOICE_RECOGNIZER_REQUEST_CODE);
    }

    @Override
    public void onTaskFailed() {

    }
}
