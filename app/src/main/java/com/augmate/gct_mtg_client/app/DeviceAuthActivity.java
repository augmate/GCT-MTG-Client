package com.augmate.gct_mtg_client.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.augmate.gct_mtg_client.R;

public class DeviceAuthActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authorize_device);

        ((TextView)findViewById(R.id.device_auth_url)).setText("https://www.google.com/device");
        ((TextView)findViewById(R.id.user_code)).setText("8fz6yyuw");
    }
}
