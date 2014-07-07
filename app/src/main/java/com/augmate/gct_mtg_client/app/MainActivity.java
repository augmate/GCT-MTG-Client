package com.augmate.gct_mtg_client.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Toast;
import com.augmate.gct_mtg_client.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.segment.android.Analytics;
import com.segment.android.TrackedActivity;
import com.segment.android.models.Props;

public class MainActivity extends TrackedActivity {

    long mScanStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScanStart = SystemClock.uptimeMillis();

        new IntentIntegrator(this).initiateScan(IntentIntegrator.QR_CODE_TYPES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == RESULT_OK) {
            IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            try {
                Integer roomNumber = Integer.valueOf(intentResult.getContents());

                if (roomNumber < 1 || roomNumber > 10) {
                    throw new IndexOutOfBoundsException();
                }

                startActivity(new Intent(this, BookActivity.class).putExtra(BookActivity.ROOM_NUMBER,roomNumber));

            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.invalid_room_error, intentResult.getContents()), Toast.LENGTH_LONG).show();
            }

            Analytics.track("QR Code Scan", new Props(
                    "value", SystemClock.uptimeMillis() - mScanStart
            ));

            finish();
        }
    }
}
