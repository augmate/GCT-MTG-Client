package com.augmate.gct_mtg_client.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;
import com.augmate.gct_mtg_client.R;
import com.augmate.gct_mtg_client.app.RoomOption;
import com.augmate.gct_mtg_client.app.activities.BeaconActivity;
import com.augmate.gct_mtg_client.app.activities.BookActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.segment.android.Analytics;
import com.segment.android.TrackedActivity;
import com.segment.android.models.Props;

import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

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

    /**
     * Catches results from QRCode scanner or Beacon scanner
     * RESULT_OK guarantees that at least one room was detected
     * @param requestCode BeaconActivityRequest (Beacon Scanner) or IntentIntegrator.REQUEST_CODE (QR-Code Scanner)
     * @param resultCode RESULT_OK when at least one room was detected
     * @param data RoomOption[] of length > 0 when resultCode == RESULT_OK
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG, "requestCode: " + requestCode + " resultCode: " + resultCode);

        if(requestCode == BeaconActivityRequest) {
            if (resultCode == RESULT_OK) { // beacon located, found room-name, proceed to booking
                Analytics.track("Beacon Scan", new Props(
                        "value", SystemClock.uptimeMillis() - mScanStart
                ));

                Object[] roomsArr = (Object[]) data.getExtras().getSerializable("rooms");
                RoomOption[] rooms = Arrays.copyOf(roomsArr, roomsArr.length, RoomOption[].class);

                // RESULT_OK implies rooms.length > 0
                assert(rooms.length > 0);

                Log.d(TAG, "onActivityResult(); Beacon Scan returned " + rooms.length + " nearest rooms");
                startBooking(newArrayList(rooms));

            } else {
                // no beacon located, launch qr-code scanner

                mScanStart = SystemClock.uptimeMillis();
                new IntentIntegrator(this).initiateScan(IntentIntegrator.QR_CODE_TYPES);
            }
        }

        // qr-code scanner found room-name, proceed to boking
        else if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == RESULT_OK) {
            Analytics.track("QR Code Scan", new Props(
                    "value", SystemClock.uptimeMillis() - mScanStart
            ));

            IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            Log.d(TAG, "onActivityResult(); QR Code Scan returned: " + intentResult.getContents());

            Integer roomNumber = Integer.valueOf(intentResult.getContents());
            startBooking(newArrayList(new RoomOption("Room " + roomNumber, 1.0f, 0.1f)));
            finish();
        }
    }

    private void startBooking(List<RoomOption> roomOptions) {
        try {
            RoomOption roomOption = roomOptions.get(0);

            if(roomOptions.size() == 1) {
                Log.d(TAG, "startBooking(); roomOptions contains exactly one option! Book it!");
                startActivity(new Intent(this, BookActivity.class).putExtra(BookActivity.ROOM_NUMBER_EXTRA, roomOption.name));
            } else if(roomOptions.size() > 1) {
                Log.d(TAG, "startBooking(); roomOptions contains more than one option, must disambiguate");
                Toast.makeText(this, "Need to disambiguate rooms", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.invalid_room_error, roomOptions), Toast.LENGTH_LONG).show();
        }

        Analytics.track("QR Code Scan", new Props(
                "value", SystemClock.uptimeMillis() - mScanStart
        ));
    }
}
