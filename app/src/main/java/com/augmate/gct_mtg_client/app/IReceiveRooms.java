package com.augmate.gct_mtg_client.app;

import java.util.List;

public interface IReceiveRooms {
    void onReceiveNearbyRooms(List<RoomOption> rooms);
}
