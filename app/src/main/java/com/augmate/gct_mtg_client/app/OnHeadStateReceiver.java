package com.augmate.gct_mtg_client.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.augmate.gct_mtg_client.app.utils.Log;

public class OnHeadStateReceiver extends BroadcastReceiver {
    public static Boolean IsOnHead = true;

    @Override
    public void onReceive(Context context, Intent intent) {

        Boolean is_on_head = intent.getBooleanExtra("is_on_head", true);
        Log.debug("Is on head: " + is_on_head);
        IsOnHead = is_on_head;
        if (!IsOnHead) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("Close", true); // an extra which says to finish the activity.
            context.startActivity(intent);
            //System.exit(0);
        }
    }

}
