package com.augmate.gct_mtg_client.app.tasks;

import android.content.Context;
import android.os.AsyncTask;
import com.augmate.gct_mtg_client.app.CredentialGen;
import com.augmate.gct_mtg_client.app.MeetingBooker;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;

public class BookAsyncTask extends AsyncTask<Void, Void, Boolean>{

    private Context context;

    public BookAsyncTask(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Credential credential = new CredentialGen(context).getCreditials() ;

        Calendar calendarService = new Calendar.Builder(
                AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                .setApplicationName("Google-CalendarAndroidSample/1.0")
                .build();

        return new MeetingBooker(calendarService).bookNow();
    }
}
