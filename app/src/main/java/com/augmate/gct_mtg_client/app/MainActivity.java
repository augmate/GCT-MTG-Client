package com.augmate.gct_mtg_client.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.augmate.gct_mtg_client.R;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.segment.android.Analytics;
import com.segment.android.TrackedActivity;
import com.segment.android.models.Props;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends TrackedActivity {

    public static final String AUTHORIZATION_CODE = "4/C11Z3U0yuhg2QJWaStdR2b7MIYzA.UrlwXH6vLfQb3oEBd8DOtNCfVtf0jQI";
    long mScanStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        InputStream inputStream = getResources().openRawResource(R.raw.secret);
                        OAuth.setReader(new InputStreamReader(inputStream));
                        OAuth.setContext(MainActivity.this);

                        //String authorizationUrl = OAuth.getAuthorizationUrl("johny@augmate.com", "");
                        //Log.d("com.augmate.auth2", authorizationUrl);

                        Credential credential = OAuth.getCredentials(AUTHORIZATION_CODE);

                        String accessToken = credential.getAccessToken();
                        Log.d("com.augmate.auth2", accessToken);

                        Calendar service = new Calendar.Builder(
                                AndroidHttp.newCompatibleTransport(), new JacksonFactory(), credential).setApplicationName("Google-CalendarAndroidSample/1.0")
                                .build();

                        CalendarList calendarList = service.calendarList().list().execute();
                        Log.d("com.augmate.calendar", calendarList.getItems().get(0).getDescription());


                    } catch (OAuth.CodeExchangeException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();

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

                ((TextView) findViewById(R.id.room_number)).setText("" + roomNumber);
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.invalid_room_error, intentResult.getContents()), Toast.LENGTH_LONG).show();
                finish();
            }

            Analytics.track("QR Code Scan", new Props(
                    "value", SystemClock.uptimeMillis() - mScanStart
            ));
        }
    }
}
