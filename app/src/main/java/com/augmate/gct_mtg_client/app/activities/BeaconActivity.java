package com.augmate.gct_mtg_client.app.activities;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import com.augmate.gct_mtg_client.R;
import com.augmate.gct_mtg_client.app.Beaconizer;
import com.augmate.gct_mtg_client.app.Log;
import com.augmate.gct_mtg_client.app.tasks.IReceiveRoomsCallbacks;
import com.augmate.gct_mtg_client.app.RoomOption;
import com.segment.android.TrackedActivity;

import java.util.List;

public class BeaconActivity extends TrackedActivity implements IReceiveRoomsCallbacks {
    Beaconizer newBeaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_nearest_room);

        newBeaconManager = new Beaconizer(this, this);
        Log.debug("BeaconManager configured.");
    }

    public void onReceiveNearbyRooms(List<RoomOption> rooms) {
        if (rooms.size() < 1) {
            ((TextView) findViewById(R.id.room_name)).setText("No rooms near by");
            ((TextView) findViewById(R.id.distance)).setText("Go for a walk");
            return;
        }

        ((TextView) findViewById(R.id.room_name)).setText("Near: " + rooms.get(0).name);
        ((TextView) findViewById(R.id.distance)).setText("Distance = " + String.format("%.2f", rooms.get(0).distance) + "magical units");

        // always take the first two rooms
        ((TextView) findViewById(R.id.beacon1)).setText(
                String.format("room %s confidence = %.2f distance = %.2f",
                        rooms.get(0).name, rooms.get(0).confidence, rooms.get(0).distance
                ));

        if (rooms.size() > 1)
            ((TextView) findViewById(R.id.beacon2)).setText(
                    String.format("room %s confidence = %.2f distance = %.2f",
                            rooms.get(1).name, rooms.get(1).confidence, rooms.get(1).distance
                    ));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        newBeaconManager.destroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        newBeaconManager.startScanning();
    }

    @Override
    protected void onStop() {
        super.onStop();
        newBeaconManager.stopScanning();
    }
}
