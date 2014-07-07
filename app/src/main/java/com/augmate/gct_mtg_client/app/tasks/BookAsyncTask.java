package com.augmate.gct_mtg_client.app.tasks;

import android.content.Context;
import android.os.AsyncTask;
import com.augmate.gct_mtg_client.app.CredentialGen;
import com.augmate.gct_mtg_client.app.MeetingBooker;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;

public class BookAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private Context context;
    private ActivityCallbacks activityCallbacks;
    private int roomNumber;

    public BookAsyncTask(Context context, ActivityCallbacks activityCallbacks, int roomNumber) {
        this.context = context;
        this.activityCallbacks = activityCallbacks;
        this.roomNumber = roomNumber;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);

        if (success) {
            activityCallbacks.onTaskSuccess();
        } else {
            activityCallbacks.onTaskFailed();
        }
    }


    @Override
    protected Boolean doInBackground(Void... params) {
        GoogleCredential credentials = new CredentialGen(context).getCreditials();

        Calendar calendarService = buildCalendarService(credentials);

        return new MeetingBooker(calendarService).bookNow(roomNumber);
    }

    private Calendar buildCalendarService(Credential credential) {
        return new Calendar.Builder(
                AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                .setApplicationName("GCTMeetingClient/1.0")
                .build();
    }
}
