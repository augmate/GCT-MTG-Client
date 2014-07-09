package com.augmate.gct_mtg_client.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import com.augmate.gct_mtg_client.R;
import com.google.zxing.integration.android.IntentIntegrator;
import roboguice.activity.RoboActivity;

public class WelcomeActivity extends RoboActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new IntentIntegrator(WelcomeActivity.this).initiateScan(IntentIntegrator.QR_CODE_TYPES);
            }
       }, 5000);

    }
}
