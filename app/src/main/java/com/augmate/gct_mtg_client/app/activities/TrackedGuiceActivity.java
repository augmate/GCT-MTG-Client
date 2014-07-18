package com.augmate.gct_mtg_client.app.activities;

import android.os.Bundle;
import com.segment.android.Analytics;
import roboguice.activity.RoboActivity;

/**
 * TrackedActivity for Segment.io analytics to work with Guice
 */
public abstract class TrackedGuiceActivity extends RoboActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Analytics.onCreate(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Analytics.activityStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Analytics.activityResume(this);
    }

    @Override
    protected void onPause() {
        Analytics.activityPause(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Analytics.activityStop(this);
    }
}