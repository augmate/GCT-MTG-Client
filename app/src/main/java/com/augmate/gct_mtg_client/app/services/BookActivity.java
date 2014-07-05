package com.augmate.gct_mtg_client.app.services;

import android.app.Activity;
import android.os.Bundle;
import com.augmate.gct_mtg_client.R;
import com.augmate.gct_mtg_client.app.CredentialGen;
import com.augmate.gct_mtg_client.app.MeetingBooker;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;

public class BookActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking);

        new Thread(new Runnable() {
                @Override
                public void run() {
                    Credential credential = new CredentialGen(BookActivity.this).getCreditials() ;

                    Calendar calendarService = new Calendar.Builder(
                            AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                            .setApplicationName("Google-CalendarAndroidSample/1.0")
                            .build();

                    new MeetingBooker(calendarService).bookNow();
                }
            }).start();

    }
}
