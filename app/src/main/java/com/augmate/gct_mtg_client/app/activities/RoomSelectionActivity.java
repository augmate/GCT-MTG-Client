package com.augmate.gct_mtg_client.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.augmate.gct_mtg_client.R;
import com.augmate.gct_mtg_client.app.Room;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import roboguice.inject.ContentView;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

@ContentView(R.layout.walking_instructions)
public class RoomSelectionActivity extends TrackedGuiceActivity {
    public static final String COMPANY_NAME_EXTRA = "COMPANY_NAME_EXTRA";
    public static final String TAG = "WalkingActivity";

    @InjectView(R.id.walking_instructions)
    TextView walkingInstructionsView;
    @InjectResource(R.string.walking_instructions)
    String walkingInstructionsTemplate;
    @InjectExtra(COMPANY_NAME_EXTRA)
    String companyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String walkingInstructions = String.format(walkingInstructionsTemplate, companyName);
        walkingInstructionsView.setText(walkingInstructions);

        if (savedInstanceState == null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    IntentIntegrator scanner = new IntentIntegrator(RoomSelectionActivity.this);
                    scanner.initiateScan();
                }
            }, 3000);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);

        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, dataIntent);

        if (resultCode == RESULT_OK && intentResult != null) {

            try {
                Room matchedRoom = Room.valueOf(intentResult.getContents());

                Intent intent = new Intent(this, VoiceTimeSelectActivity.class)
                        .putExtra(VoiceTimeSelectActivity.ROOM_NAME_EXTRA, matchedRoom)
                        .putExtra(VoiceTimeSelectActivity.COMPANY_NAME_EXTRA, companyName);

                startActivity(intent);

            } catch (Exception e) {
                Log.e(TAG, "Invalid room qr code scanned");
                Toast.makeText(this, "This QR code is not a room", Toast.LENGTH_LONG).show();

                IntentIntegrator scanner = new IntentIntegrator(RoomSelectionActivity.this);
                scanner.initiateScan();
            }

        } else {
            Log.d(TAG, "Match not found for any result");
            Toast.makeText(this, "Room not found", Toast.LENGTH_LONG).show();
        }

    }
}
