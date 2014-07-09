package com.augmate.gct_mtg_client.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import com.augmate.gct_mtg_client.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if(resultCode == RESULT_OK) {
            String companyName = intentResult.getContents();

            Intent i = new Intent(this, WalkInstructionActivity.class);
            i.putExtra(WalkInstructionActivity.COMPANY_NAME_EXTRA, companyName);

            startActivity(i);
        }
    }
}
