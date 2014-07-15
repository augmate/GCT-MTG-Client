package com.augmate.gct_mtg_client.app.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.augmate.gct_mtg_client.R;
import com.augmate.gct_mtg_client.app.OnHeadStateReceiver;
import com.google.inject.Inject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.segment.android.Analytics;
import com.segment.android.models.Props;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.welcome_screen)
public class WelcomeActivity extends TrackedGuiceActivity {
    public static final String TAG = "WelcomeActivity";

    @InjectView(R.id.wifi_missing)
    TextView wifiMissingView;

    private long mLoginStartTime = 0;

    @Inject
    ConnectivityManager connectivityManager;

    public static WelcomeActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        instance = this;

        Log.d(TAG, "onCreate");

        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if(!networkInfo.isConnected()){
            Log.d(TAG, "Wifi is not turned on, preventing user from progressing");
            wifiMissingView.setVisibility(View.VISIBLE);
        }else if(savedInstanceState == null) {
            Log.d(TAG, "Skipping scanner, recreated activity instance");
            launchScanner(1000);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if(resultCode == RESULT_OK) {

            Analytics.track("GCT Login Scan Time Success", new Props(
                    "value", SystemClock.uptimeMillis() - mLoginStartTime
            ));

            String companyName = intentResult.getContents();

            Intent i = new Intent(this, RoomSelectionActivity.class);
            i.putExtra(RoomSelectionActivity.COMPANY_NAME_EXTRA, companyName);

            startActivity(i);
            finish();

            Log.d(TAG, "Scan completed successfully & activity finished");
        }
        else if(resultCode == RESULT_CANCELED) {
            Analytics.track("GCT Login Scan Time Unsuccessful", new Props(
                    "value", SystemClock.uptimeMillis() - mLoginStartTime
            ));
            launchScanner(1000);
        }
    }

    public void onResume()
    {
        ComponentName component=new ComponentName(this, OnHeadStateReceiver.class);
        getPackageManager()
                .setComponentEnabledSetting(component,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
        super.onResume();
    }

    public void onPause()
    {
        ComponentName component=new ComponentName(this, OnHeadStateReceiver.class);
        getPackageManager()
                .setComponentEnabledSetting(component,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
        super.onPause();
    }

    public void onNewIntent(Intent intent){
        if(intent.getBooleanExtra("Close", false))
            finish();
    }

    private void launchScanner(long delay){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mLoginStartTime = SystemClock.uptimeMillis();
                Log.d(TAG, "Starting a new scanner");
                new IntentIntegrator(WelcomeActivity.this).initiateScan(IntentIntegrator.QR_CODE_TYPES);
            }
        }, delay);
    }
}
