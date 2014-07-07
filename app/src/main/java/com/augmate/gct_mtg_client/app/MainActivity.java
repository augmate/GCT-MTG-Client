package com.augmate.gct_mtg_client.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;
import com.augmate.gct_mtg_client.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.segment.android.Analytics;
import com.segment.android.TrackedActivity;
import com.segment.android.models.Props;

public class MainActivity extends TrackedActivity {
    final static int BeaconActivityRequest = 2;
    private static final String TAG = "MainActivity";
    
    long mScanStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScanStart = SystemClock.uptimeMillis();
        startActivityForResult(new Intent(this, BeaconActivity.class), BeaconActivityRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        Log.d(TAG, "Committing seppuko");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        Log.d(TAG, "request code: " + requestCode + " resultCode: " + resultCode);
        
        if(requestCode == BeaconActivityRequest) {
            if (resultCode == RESULT_OK) { // beacon located, found room-number, proceed to booking
                Analytics.track("Beacon Scan", new Props(
                        "value", SystemClock.uptimeMillis() - mScanStart
                ));

                String beaconId = data.getExtras().getString("beaconId");
                Double distance = data.getExtras().getDouble("distance");

                int roomNumber = 0;

                if (beaconId.equals("purple"))
                    roomNumber = 1;
                else if (beaconId.equals("light-blue"))
                    roomNumber = 2;
                else if (beaconId.equals("light-green"))
                    roomNumber = 3;

                Log.d(TAG, "onActivityResult(); Beacon Scan returned room " + roomNumber + " at distance " + String.format("%.2f", distance));

                startBooking(roomNumber);

            } else {
                // no beacon located, launch qr-code scanner

                mScanStart = SystemClock.uptimeMillis();
                new IntentIntegrator(this).initiateScan(IntentIntegrator.QR_CODE_TYPES);
            }
        }        
        
        // qr-code scanner found room-number, proceed to boking
        else if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == RESULT_OK) {
            Analytics.track("QR Code Scan", new Props(
                    "value", SystemClock.uptimeMillis() - mScanStart
            ));

            IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            Log.d(TAG, "onActivityResult(); QR Code Scan returned: " + intentResult.getContents());

            Integer roomNumber = Integer.valueOf(intentResult.getContents());
            startBooking(roomNumber);
            finish();
        }
    }

    private void startBooking(int roomNumber) {
        try {

            if (roomNumber < 1 || roomNumber > 10) {
                throw new IndexOutOfBoundsException();
            }

            startActivity(new Intent(this, BookActivity.class).putExtra(BookActivity.ROOM_NUMBER,roomNumber));

        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.invalid_room_error, roomNumber), Toast.LENGTH_LONG).show();
        }

        Analytics.track("QR Code Scan", new Props(
                "value", SystemClock.uptimeMillis() - mScanStart
        ));
    }
}
