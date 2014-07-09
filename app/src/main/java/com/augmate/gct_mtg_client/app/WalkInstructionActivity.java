package com.augmate.gct_mtg_client.app;

import android.os.Bundle;
import android.widget.TextView;
import com.augmate.gct_mtg_client.R;
import roboguice.inject.ContentView;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

@ContentView(R.layout.walking_instructions)
public class WalkInstructionActivity extends TrackedGuiceActivity {
    public static final String COMPANY_NAME_EXTRA = "COMPANY_NAME_EXTRA";

    @InjectView(R.id.walking_instructions)
    TextView walkingInstructionsView;
    @InjectResource(R.string.walking_instructions)
    String walkingInstructionsTemplate;
    @InjectExtra(COMPANY_NAME_EXTRA)
    String companyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String walkingInstructions = String.format(walkingInstructionsTemplate, companyName);

        walkingInstructionsView.setText(walkingInstructions);
    }
}
