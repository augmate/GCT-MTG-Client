package com.augmate.gct_mtg_client.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import com.augmate.gct_mtg_client.app.activities.WelcomeActivity;
import com.segment.android.Analytics;

public class OnHeadStateReceiver extends BroadcastReceiver {

    public static final String TAG = "OnHeadStateReceiver";

    public static Boolean IsOnHead = true;

    @Override
    public void onReceive(Context context, Intent intent) {

        Boolean is_on_head = intent.getBooleanExtra("is_on_head", true);
        Log.d(TAG, "Is on head: " + is_on_head);

        IsOnHead = is_on_head;
    }
}
