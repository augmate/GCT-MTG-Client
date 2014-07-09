package com.augmate.gct_mtg_client.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.augmate.gct_mtg_client.R;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.estimote.sdk.utils.L;
import com.segment.android.TrackedActivity;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;

public class BeaconActivity extends TrackedActivity
{
    //private static final String TAG = "Beaconizer";
    private static final Region BEACON_SEARCH_MASK = new Region("rid", null, null, null);
            
    private BeaconManager beaconManager;
    
    private class BeaconData {
        public List<String> rooms = new ArrayList<String>(); // rooms/resources this beacon represents
        public float confidence; // confidence of distance
        public float distance; // distance to user
    }
    
    
    /*
        beacon identities
        -------------------------------------------------
        color           major:minor
        purple          40125:2233
        light-blue      1:9
        light-green     1:7                  
        
        once we can modify beacon data, this won't be necessary
     */
    protected String getBeaconName(Beacon beacon) {
        String name = beacon.getMajor() + ":" + beacon.getMinor();
        
        if(name.equals("40125:2233"))
            return "purple";
        if(name.equals("1:9"))
            return "light-blue";
        if(name.equals("1:7"))
            return "light-green";
        
        return "(unknown beacon)";
    }
    
    protected BeaconData getBeaconData(Beacon beacon) {
        String beaconName = getBeaconName(beacon);
        float distance = (float)Utils.computeAccuracy(beacon);

        Log.d("BeaconActivity::getBeaconData()", "  Beacon " + beaconName + " accuracy=" + String.format("%.2f", distance) + " power=" + beacon.getMeasuredPower() + " rssi=" + beacon.getRssi());

        // beacon data could be stored in the beacon, or pulled from a web-service
        BeaconData data = new BeaconData();
        data.distance = distance;
        data.confidence = 1;
        
        if (beaconName.equals("purple")) {
            data.rooms.add("Room 1");
            data.rooms.add("Room 2");
        } else if (beaconName.equals("light-blue")) {
            data.rooms.add("Garage");
        } else if (beaconName.equals("light-green")) {
            data.rooms.add("Room 4");
        }
        
        return data;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_nearest_room);

        L.enableDebugLogging(true);

        beaconManager = new BeaconManager(this);
        //beaconManager.setForegroundScanPeriod(1000, 5000);
        //beaconManager.setBackgroundScanPeriod(1000, 5000);
        
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override public void onBeaconsDiscovered(Region region, List<Beacon> rawBeacons) {
                Log.d("BeaconActivity::onCreate()::onBeaconsDiscovered()", "BLE-scan found ranged beacons: " + rawBeacons);
                
                // cut-off point for beacons
                // when at 15% broadcast power, 4 seems to be far enough to ignore
                // TODO: does the reported beacon distance change with power?
                double beaconCutoffDist = 3;
                BeaconData nearestBeaconData = null;
                
                List<BeaconData> processedBeacons = new ArrayList<BeaconData>();
                
                for(Beacon beacon : rawBeacons) {
                    
                    BeaconData processedBeacon = getBeaconData(beacon);
                    
                    if(processedBeacon.distance < beaconCutoffDist) {
                        beaconCutoffDist = processedBeacon.distance;
                        nearestBeaconData = processedBeacon;
                    }

                    processedBeacons.add(processedBeacon);
                }
                
                Collections.sort(processedBeacons, new Comparator<BeaconData>() {
                    @Override
                    public int compare(BeaconData lhs, BeaconData rhs) {
                        // assuming precision down to 0.01 units ("meters")
                        return (int) (100 * (lhs.distance - rhs.distance));
                    }
                });
                
                
                List<RoomOption> rooms = new ArrayList<RoomOption>();
                for (BeaconData processedBeacon : processedBeacons) {
                    for(String roomName : processedBeacon.rooms)
                        rooms.add(new RoomOption(roomName, processedBeacon.confidence, processedBeacon.distance));
                }

                Bundle bundle = new Bundle();
                bundle.putSerializable("rooms", rooms.toArray());
                
                Message msg = onBeaconFoundHandler.obtainMessage();
                msg.setData(bundle);
                onBeaconFoundHandler.sendMessage(msg);
            }
        });
        
        Log.d("BeaconActivity::onCreate()", "BeaconManager configured.");
    }
    
    Handler onBeaconFoundHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Object[] roomsArr = (Object[]) msg.getData().getSerializable("rooms");
            RoomOption[] rooms = Arrays.copyOf(roomsArr, roomsArr.length, RoomOption[].class);

            if (rooms.length < 1) {
                ((TextView) findViewById(R.id.room_name)).setText("No rooms near by");
                ((TextView) findViewById(R.id.distance)).setText("Go for a walk");
                return;
            }

            ((TextView) findViewById(R.id.room_name)).setText("Near: " + rooms[0].name);
            ((TextView) findViewById(R.id.distance)).setText("Distance = " + String.format("%.2f", rooms[0].distance) + "magical units");

            // always take the first two rooms
            ((TextView) findViewById(R.id.beacon1)).setText(
                    String.format("room %s confidence = %.2f distance = %.2f",
                            rooms[0].name, rooms[0].confidence, rooms[0].distance
                    ));

            if (rooms.length > 1)
                ((TextView) findViewById(R.id.beacon2)).setText(
                        String.format("room %s confidence = %.2f distance = %.2f",
                                rooms[1].name, rooms[1].confidence, rooms[1].distance
                        ));

            onBeaconSuccess(msg);
        }
    };

    /**
     * Shutdown beacon scan, set beacon data as a result, pop activity
     * @param msg RoomOption array serialized inside a bundle
     */
    private void onBeaconSuccess(Message msg) {
        try {
            beaconManager.stopRanging(BEACON_SEARCH_MASK);
        } catch (RemoteException e) {
            Log.e("BeaconActivity::onBeaconSuccess()", "Can't stop Beacon Manager", e);
        }
        setResult(Activity.RESULT_OK, new Intent().putExtras(msg.getData()));
        finish();
    }
    
    

    @Override
    protected void onDestroy() {
        super.onDestroy();

        beaconManager.disconnect();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!beaconManager.hasBluetooth()) {
            Toast.makeText(this, "Device does not have Bluetooth Low Energy", Toast.LENGTH_LONG).show();
            return;
        }

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override public void onServiceReady() {
                Log.d("BeaconActivity::onStart()", "Beacon Service is ready. Starting ranging scan.");
                
                try {
                    beaconManager.startRanging(BEACON_SEARCH_MASK);
                } catch (RemoteException e) {
                    Log.e("BeaconActivity::onStart()", "BeaconManager couldn't start ranging.", e);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d("BeaconActivity::onStop()", "Starting ranging scan.");

        try {
            beaconManager.stopRanging(BEACON_SEARCH_MASK);
        } catch (RemoteException e) {
            Log.e("BeaconActivity::onStop()", "BeaconManager couldn't stop ranging.", e);
        }
    }
}
