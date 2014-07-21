package com.augmate.gct_mtg_client.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;
import com.augmate.gct_mtg_client.R;
import com.augmate.gct_mtg_client.app.Beaconizer;
import com.augmate.gct_mtg_client.app.Room;
import com.augmate.gct_mtg_client.app.RoomOption;
import com.augmate.gct_mtg_client.app.tasks.IReceiveRoomsCallbacks;
import com.augmate.gct_mtg_client.app.utils.Log;
import roboguice.inject.ContentView;
import roboguice.inject.InjectExtra;

import java.util.List;

@ContentView(R.layout.activity_nearest_room)
public class BeaconRoomSelectionActivity extends TrackedGuiceActivity implements IReceiveRoomsCallbacks {
    Beaconizer newBeaconManager;

    @InjectExtra(RoomSelectionActivity.COMPANY_NAME_EXTRA)
    String companyName;

    private Room matchedRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        newBeaconManager = new Beaconizer(this, this);
        Log.debug("BeaconManager configured.");
    }

    public void onReceiveNearbyRooms(List<RoomOption> rooms) {
        if (rooms.size() > 0) {
            matchedRoom = Room.valueOf(rooms.get(0).name);

            ((TextView) findViewById(R.id.room_name)).setText("Near: " + matchedRoom.displayName);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && matchedRoom != null) {

            Intent intent = new Intent(this, VoiceTimeSelectActivity.class)
                    .putExtra(VoiceTimeSelectActivity.ROOM_NAME_EXTRA, matchedRoom)
                    .putExtra(VoiceTimeSelectActivity.COMPANY_NAME_EXTRA, companyName);

            startActivity(intent);
            finish();
        }

        return super.onKeyDown(keyCode, event);
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
