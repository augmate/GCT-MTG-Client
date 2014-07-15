package com.augmate.gct_mtg_client.app.tasks;

import com.augmate.gct_mtg_client.app.RoomOption;

import java.util.List;

public interface IReceiveRoomsCallbacks {
    void onReceiveNearbyRooms(List<RoomOption> rooms);
}
