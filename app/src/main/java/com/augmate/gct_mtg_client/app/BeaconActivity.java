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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class BeaconActivity extends TrackedActivity
{
    private static final String TAG = "Beaconizer";
    private static final Region BEACON_SEARCH_MASK = new Region("rid", null, null, null);
            
    private BeaconManager beaconManager;
    
    private class BeaconData {
        public List<Integer> rooms; // rooms/resources this beacon represents
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

        Log.d(TAG, "  Beacon " + beaconName + " accuracy=" + String.format("%.2f", distance) + " power=" + beacon.getMeasuredPower() + " rssi=" + beacon.getRssi());

        // beacon data could be stored in the beacon, or pulled from a web-service
        BeaconData data = new BeaconData();
        data.distance = distance;
        data.confidence = 1;
        
        if (beaconName.equals("purple")) {
            data.rooms.add(1);
            data.rooms.add(2);
        } else if (beaconName.equals("light-blue")) {
            data.rooms.add(3);
        } else if (beaconName.equals("light-green")) {
            data.rooms.add(4);
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
        beaconManager.setForegroundScanPeriod(1000, 1000);
        beaconManager.setBackgroundScanPeriod(1000, 1000);
        
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override public void onBeaconsDiscovered(Region region, List<Beacon> rawBeacons) {
                Log.d(TAG, "BLE-scan found ranged beacons: " + rawBeacons);
                
                // cut-off point for beacons
                // when at 15% broadcast power, 4 seems to be far enough to ignore
                // TODO: does the reported beacon distance change with power?
                double beaconCutoffDist = 4;
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
                    for(Integer roomNumber : processedBeacon.rooms)
                        rooms.add(new RoomOption(roomNumber, processedBeacon.confidence));
                }

                Bundle bundle = new Bundle();
                bundle.putSerializable("rooms", rooms.toArray());
                
                Message msg = onBeaconFoundHandler.obtainMessage();
                msg.setData(bundle);
                onBeaconFoundHandler.sendMessage(msg);
            }
        });
        
        Log.d(TAG, "Started Beacon Manager");
    }
    
    Handler onBeaconFoundHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String beaconId = msg.getData().getString("beaconId");
            Double distance = msg.getData().getDouble("distance");
            
            int roomNumber = 0;
            
            if(beaconId.equals("purple"))
                roomNumber = 1;
            else if(beaconId.equals("light-blue"))
                roomNumber = 2;
            else if(beaconId.equals("light-green"))
                roomNumber = 3;

            ((TextView) findViewById(R.id.room_number)).setText("Room " + roomNumber);
            ((TextView) findViewById(R.id.distance)).setText(beaconId + " beacon is " + String.format("%.2f", distance) + " units away");

            ((TextView) findViewById(R.id.beacon1)).setText("purple beacon = " + String.format("%.2f", msg.getData().getDouble("beacon-purple")) + " units away");
            ((TextView) findViewById(R.id.beacon2)).setText("blue beacon = " + String.format("%.2f", msg.getData().getDouble("beacon-light-blue")) + " units away");
            ((TextView) findViewById(R.id.beacon3)).setText("green beacon = " + String.format("%.2f", msg.getData().getDouble("beacon-light-green")) + " units away");
            
            //afterBeaconResult(msg);
        }
    };

    /**
     * Shutdown beacon scan, set beacon data as a result, pop activity
     * @param msg
     */
    private void afterBeaconResult(Message msg) {
        try {
            beaconManager.stopRanging(BEACON_SEARCH_MASK);
        } catch (RemoteException e) {
            Log.e(TAG, "Can't stop Beacon Manager", e);
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

        Log.d(TAG, "Started!");

        if (!beaconManager.hasBluetooth()) {
            Toast.makeText(this, "Device does not have Bluetooth Low Energy", Toast.LENGTH_LONG).show();
            return;
        }

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override public void onServiceReady() {
                Log.d(TAG, "Beacon Service is ready.");
                
                try {
                    beaconManager.startRanging(BEACON_SEARCH_MASK);
                } catch (RemoteException e) {
                    Log.e(TAG, "Cannot start ranging", e);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            beaconManager.stopRanging(BEACON_SEARCH_MASK);
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot stop but it does not matter now", e);
        }
    }
}
