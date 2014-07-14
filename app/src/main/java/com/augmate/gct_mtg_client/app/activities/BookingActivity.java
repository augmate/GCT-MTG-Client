package com.augmate.gct_mtg_client.app.activities;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.augmate.gct_mtg_client.R;
import com.augmate.gct_mtg_client.app.BookingTime;
import com.augmate.gct_mtg_client.app.Room;
import com.augmate.gct_mtg_client.app.tasks.ActivityCallbacks;
import com.augmate.gct_mtg_client.app.tasks.BookAsyncTask;
import roboguice.inject.ContentView;
import roboguice.inject.InjectExtra;

@ContentView(R.layout.booking)
public class BookingActivity extends TrackedGuiceActivity implements ActivityCallbacks {

    public static final String ROOM_NUMBER_EXTRA = "room_number_extra";
    public static final String BOOKING_TIME_EXTRA = "booking_time_extra";
    public static final String COMPANY_NAME_EXTRA = "COMPANY_NAME_EXTRA";

    @InjectExtra(BOOKING_TIME_EXTRA)
    private BookingTime bookingTime;

    @InjectExtra(ROOM_NUMBER_EXTRA)
    private Room roomNumber;

    @InjectExtra(COMPANY_NAME_EXTRA)
    private String companyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        new BookAsyncTask(this, this, roomNumber, bookingTime, companyName).execute();
    }

    @Override
    public void onTaskSuccess() {
        ((TextView) findViewById(R.id.booking_view)).setText(companyName + " booked " + roomNumber.displayName + " for " + bookingTime.displayName);
    }

    @Override
    public void onTaskFailed() {
        Toast.makeText(this, "Could not book room!", Toast.LENGTH_LONG).show();
        finish();
    }
}
