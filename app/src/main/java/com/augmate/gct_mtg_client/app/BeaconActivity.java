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

import java.util.List;

public class BeaconActivity extends TrackedActivity
{
    private static final String TAG = "Beaconizer";
    private static final Region BEACON_SEARCH_MASK = new Region("rid", null, null, null);
            
    private BeaconManager beaconManager;
    
    
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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_nearest_room);

        L.enableDebugLogging(true);

        beaconManager = new BeaconManager(this);
        
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                Log.d(TAG, "Ranged beacons: " + beacons);
                
                // cut-off point for beacons
                // when at 15% broadcast power, 4 seems to be far enough to ignore
                // TODO: does the reported beacon distance change with power?
                double nearestBeaconDist = 4;
                Beacon nearestBeacon = null;
                
                for(Beacon beacon : beacons) {
                    Utils.Proximity proximity = Utils.computeProximity(beacon);
                    double distance = Utils.computeAccuracy(beacon);
                    String color = getBeaconName(beacon);
                    
                    Log.d(TAG, "  Beacon " + color + " accuracy=" + String.format("%.2f", distance) + " proximity=" + proximity);
                    
                    if(distance < nearestBeaconDist) {
                        nearestBeaconDist = distance;
                        nearestBeacon = beacon;
                    }
                }
                
                // if we have a near-by beacon, send its id to a handler that will update UI
                if(nearestBeacon != null) {
                    Bundle data = new Bundle();
                    data.putString("beaconId", getBeaconName(nearestBeacon));
                    data.putDouble("distance", nearestBeaconDist);
                    Message msg = onBeaconFoundHandler.obtainMessage();
                    msg.setData(data);
                    onBeaconFoundHandler.sendMessage(msg);
                }
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

            try {
                beaconManager.stopRanging(BEACON_SEARCH_MASK);
            } catch (RemoteException e) {
                Log.e(TAG, "Can't stop Beacon Manager", e);
            }

            afterBeaconResult(msg);
        }
    };

    private void afterBeaconResult(Message msg) {
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
