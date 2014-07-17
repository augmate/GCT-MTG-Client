package com.augmate.gct_mtg_client.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.augmate.gct_mtg_client.R;
import com.augmate.gct_mtg_client.app.Room;
import com.augmate.gct_mtg_client.app.utils.Log;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import roboguice.inject.ContentView;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

@ContentView(R.layout.room_selection)
public class RoomSelectionActivity extends TrackedGuiceActivity {
    public static final String COMPANY_NAME_EXTRA = "COMPANY_NAME_EXTRA";
    public static final String TAG = "RoomSelectionActivity";

    @InjectView(R.id.walking_instructions)
    TextView walkingInstructionsView;
    @InjectResource(R.string.walking_instructions)
    String walkingInstructionsTemplate;
    @InjectExtra(COMPANY_NAME_EXTRA)
    String companyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String walkingInstructions = String.format(walkingInstructionsTemplate, companyName);
        walkingInstructionsView.setText(walkingInstructions);

        if (savedInstanceState == null)
            launchScanner(6000);
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
                finish();

            } catch (Exception e) {
                Log.error("Invalid room qr code scanned", e);
                Toast.makeText(this, "This QR code is not a room", Toast.LENGTH_LONG).show();

                IntentIntegrator scanner = new IntentIntegrator(RoomSelectionActivity.this);
                scanner.initiateScan();
            }

        } else if (resultCode == RESULT_CANCELED) {
            //launchScanner(3000);
            finish();
        } else {
            Log.debug("Match not found for any result");
            Toast.makeText(this, "Room not found", Toast.LENGTH_LONG).show();
        }
    }

    private void launchScanner(long delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                IntentIntegrator scanner = new IntentIntegrator(RoomSelectionActivity.this);
                scanner.initiateScan();
            }
        }, delay);
    }
}
