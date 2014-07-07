package com.augmate.gct_mtg_client.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.augmate.gct_mtg_client.R;

public class DeviceAuthInfoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authorize_device);

        ((TextView)findViewById(R.id.device_auth_url)).setText(getIntent().getStringExtra("verification_url"));
        ((TextView)findViewById(R.id.user_code)).setText(getIntent().getStringExtra("user_code"));
    }
}
