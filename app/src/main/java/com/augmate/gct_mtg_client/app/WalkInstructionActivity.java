package com.augmate.gct_mtg_client.app;

import android.os.Bundle;
import android.widget.TextView;
import com.augmate.gct_mtg_client.R;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class WalkInstructionActivity extends RoboActivity {
    public static final String COMPANY_NAME_EXTRA = "COMPANY_NAME_EXTRA";

    @InjectView(R.id.company_name)
    TextView companyNameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.walking_instructions);

        String companyName = getIntent().getStringExtra(COMPANY_NAME_EXTRA);

        companyNameView.setText(companyName);
    }
}
