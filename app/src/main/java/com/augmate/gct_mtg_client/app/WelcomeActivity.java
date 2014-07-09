package com.augmate.gct_mtg_client.app;

import android.os.Bundle;
import android.view.WindowManager;
import com.augmate.gct_mtg_client.R;
import roboguice.activity.RoboActivity;

public class WelcomeActivity extends RoboActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


    }
}
