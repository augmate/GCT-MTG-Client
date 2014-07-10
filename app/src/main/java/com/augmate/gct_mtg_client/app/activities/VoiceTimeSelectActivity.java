package com.augmate.gct_mtg_client.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import com.augmate.gct_mtg_client.app.Rooms;
import roboguice.inject.InjectExtra;

import java.util.ArrayList;

public class VoiceTimeSelectActivity extends TrackedGuiceActivity {

    public static final String ROOM_NUMBER_EXTRA = "ROOM_NUMBER_EXTRA";
    public static final String TAG = VoiceTimeSelectActivity.class.getName();
    public static final int VOICE_RECOGNIZER_REQUEST_CODE = 101;

    @InjectExtra(ROOM_NUMBER_EXTRA)
    Rooms requestedRoomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String roomPrompt = String.format("Room %s is available. Book now?", requestedRoomName);

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, roomPrompt);

        Log.d(TAG, "Requesting time select via speech-recognition..");
        startActivityForResult(intent, VOICE_RECOGNIZER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            float[] confidence = data.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES);

            boolean userSaidYes = true;

            for (int i=0; i<results.size(); ++i){
                Log.d(TAG, "Voice result: " + results.get(i) + "   confidence: " + confidence[i]);

                if(results.get(i).equals("yes")) {
                    userSaidYes = true;
                    break;
                }
            }

            if(userSaidYes) {
                startActivity(new Intent(this, BookActivity.class).putExtra(BookActivity.ROOM_NUMBER_EXTRA, requestedRoomName));
                return;
            }

        }

        // user said no, or whatever they said wasn't recognized
        Log.d(TAG, "Voice finished without positive user response (yes, etc)");

        // TODO: pop back to walking screen
    }
}
