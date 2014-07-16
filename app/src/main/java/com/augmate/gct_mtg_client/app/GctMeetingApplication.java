package com.augmate.gct_mtg_client.app;

import android.app.Application;
import android.util.Log;

public class GctMeetingApplication extends Application{

    public static final String TAG = "GctMeetingApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application created");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "Application terminating");
    }
}
