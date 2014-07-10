package com.augmate.gct_mtg_client.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.TextView;
import com.augmate.gct_mtg_client.R;
import com.augmate.gct_mtg_client.app.Beaconizer;
import com.augmate.gct_mtg_client.app.IReceiveRooms;
import com.augmate.gct_mtg_client.app.RoomOption;
import roboguice.inject.ContentView;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import java.util.ArrayList;
import java.util.List;

@ContentView(R.layout.walking_instructions)
public class WalkingActivity extends TrackedGuiceActivity implements IReceiveRooms {
    public static final String COMPANY_NAME_EXTRA = "COMPANY_NAME_EXTRA";
    public static final String TAG = "WalkingActivity";
    public static final int VOICE_RECOGNIZER_REQUEST_CODE = 101;

    @InjectView(R.id.walking_instructions)
    TextView walkingInstructionsView;
    @InjectResource(R.string.walking_instructions)
    String walkingInstructionsTemplate;
    @InjectExtra(COMPANY_NAME_EXTRA)
    String companyName;

    Beaconizer beaconizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String walkingInstructions = String.format(walkingInstructionsTemplate, companyName);
        walkingInstructionsView.setText(walkingInstructions);

        Log.d(TAG, "Creating beaconizer..");
        beaconizer = new Beaconizer(this, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        beaconizer.startScanning();
    }

    @Override
    protected void onStop() {
        super.onStop();
        beaconizer.stopScanning();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconizer.destroy();
    }

    @Override
    public void onReceiveNearbyRooms(List<RoomOption> rooms) {
        Log.d(TAG, "Got " + rooms.size() + " rooms");

        if (rooms.size() < 2)
            return;

        beaconizer.stopScanning();

        String roomPrompt = String.format("Would you like to book %s?", rooms.get(0).name);

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, roomPrompt);

        Log.d(TAG, "Starting speech request");
        startActivityForResult(intent, VOICE_RECOGNIZER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        float[] confidence = data.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES);

        for (int i=0; i<results.size(); ++i){
            Log.d(TAG, "Voice result: " + results.get(i) + "   confidence: " + confidence[i]);
        }
    }
}
