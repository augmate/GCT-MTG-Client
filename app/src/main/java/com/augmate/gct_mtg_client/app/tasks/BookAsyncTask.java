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
        Boolean success = false;

        Credential credential = new CredentialGen(context).getCreditials();

        if(credential != null) {
            Calendar calendarService = new Calendar.Builder(
                    AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                    .setApplicationName("GCTMeetingClient/1.0")
                    .build();

            success = new MeetingBooker(calendarService).bookNow();
        }

        return success;
    }
}
