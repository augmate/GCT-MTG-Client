package com.augmate.gct_mtg_client.app;

import android.content.Context;
import android.os.RemoteException;
import com.augmate.gct_mtg_client.app.tasks.IReceiveRoomsCallbacks;
import com.augmate.gct_mtg_client.app.utils.Log;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Beaconizer {

    private static final Region BEACON_SEARCH_MASK = new Region("rid", null, null, null);
    private BeaconManager beaconManager;
    boolean isRunning = false;

    public Beaconizer(Context context, final IReceiveRoomsCallbacks receiver) {
        Log.debug("Beacon Service is ready. Starting ranging scan.");

        beaconManager = new BeaconManager(context);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override public void onBeaconsDiscovered(Region region, List<Beacon> rawBeacons) {
                Log.debug("BLE-scan found ranged beacons: " + rawBeacons);

                if(!isRunning) {
                    Log.debug("got results after stopping beacon manager");
                    return;
                }

                // cut-off point for beacons
                // when at 15% broadcast power, 4 seems to be far enough to ignore
                // TODO: does the reported beacon distance change with power?
                double beaconCutoffDist = 2;

                List<BeaconData> processedBeacons = new ArrayList<BeaconData>();

                for(Beacon beacon : rawBeacons) {
                    BeaconData processedBeacon = getBeaconData(beacon);

                    if(processedBeacon.distance < beaconCutoffDist) {
                        processedBeacons.add(processedBeacon);
                    }
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

                receiver.onReceiveNearbyRooms(rooms);
            }
        });
    }

    public void destroy() {
        Log.debug("Destroying manager..");
        isRunning = false;
        beaconManager.disconnect();
    }

    public void startScanning() {
        // TODO: handle bluetooth errors
        Log.debug("Starting beacon manager..");
        assert(isRunning);
        isRunning = true;

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override public void onServiceReady() {
                Log.debug("Beacon Service is ready. Starting ranging scan.");

                try {
                    beaconManager.startRanging(BEACON_SEARCH_MASK);
                } catch (RemoteException e) {
                    Log.error("BeaconManager couldn't start ranging.", e);
                }
            }
        });
    }

    public void stopScanning() {
        Log.debug("Stopping scanner");

        if(!isRunning)
            return;

        isRunning = false;
        try {
            beaconManager.stopRanging(BEACON_SEARCH_MASK);
        } catch (RemoteException e) {
            Log.error("Can't stop Beacon Manager", e);
        }
    }

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
        float distance = (float) Utils.computeAccuracy(beacon);

        Log.debug("  Beacon " + beaconName + " accuracy=" + String.format("%.2f", distance) + " power=" + beacon.getMeasuredPower() + " rssi=" + beacon.getRssi());

        // beacon data could be stored in the beacon, or pulled from a web-service
        BeaconData data = new BeaconData();
        data.distance = distance;
        data.confidence = 1;

        if (beaconName.equals("purple")) {
            data.rooms.add("Room 1");
            data.rooms.add("Room 2");
        } else if (beaconName.equals("light-blue")) {
            data.rooms.add("Garage");
            data.rooms.add("Room 3");
        } else if (beaconName.equals("light-green")) {
            data.rooms.add("Room 4");
            data.rooms.add("Room 5");
        }

        return data;
    }
}