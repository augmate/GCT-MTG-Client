package com.augmate.gct_mtg_client.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class NetworkConnectivityReceiver extends BroadcastReceiver {

    public static final String TAG = "NetworkConnectivityReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        NetworkInfo networkInfo =
                intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

        Log.i(TAG, "=======================================");
        Log.i(TAG, "State: " + networkInfo.getState());
        Log.i(TAG, "Detailed State: " + networkInfo.getDetailedState());
        Log.i(TAG, "Is Connected?: " + networkInfo.isConnected());
        Log.i(TAG, "Is Available?: " + networkInfo.isAvailable());
        Log.i(TAG, "Is Connecting?: " + networkInfo.isConnectedOrConnecting());
        Log.i(TAG, "Is failover?: " + networkInfo.isFailover());
        Log.i(TAG, "Type name: " + networkInfo.getTypeName());
        Log.i(TAG, "Extra info: " + networkInfo.getExtraInfo());
        Log.i(TAG, "=======================================");
    }
}
