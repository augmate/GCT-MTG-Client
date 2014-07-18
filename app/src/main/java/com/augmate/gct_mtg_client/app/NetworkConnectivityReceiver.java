package com.augmate.gct_mtg_client.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import com.augmate.gct_mtg_client.app.utils.Log;
import com.segment.android.Analytics;

public class NetworkConnectivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        NetworkInfo networkInfo =
                intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

        Log.debug("=======================================");
        Log.debug("State: " + networkInfo.getState());
        Log.debug("Detailed State: " + networkInfo.getDetailedState());
        Log.debug("Is Connected?: " + networkInfo.isConnected());
        Log.debug("Is Available?: " + networkInfo.isAvailable());
        Log.debug("Is Connecting?: " + networkInfo.isConnectedOrConnecting());
        Log.debug("Is failover?: " + networkInfo.isFailover());
        Log.debug("Type name: " + networkInfo.getTypeName());
        Log.debug("Extra info: " + networkInfo.getExtraInfo());
        Log.debug("=======================================");

        Analytics.track("GCT - Network State " + networkInfo.getState());
    }
}
