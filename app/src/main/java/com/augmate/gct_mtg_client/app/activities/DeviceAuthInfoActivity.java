package com.augmate.gct_mtg_client.app.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import com.augmate.gct_mtg_client.R;

public class DeviceAuthInfoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.authorize_device);

        ((TextView)findViewById(R.id.device_auth_url)).setText(getIntent().getStringExtra("verification_url"));
        ((TextView)findViewById(R.id.user_code)).setText(getIntent().getStringExtra("user_code"));
    }
}
